import collections
import os
import sys
import numpy as np

from numpy.random import rand
from pyspark import SparkContext
import time
import pyspark

MIN_EPS  = 0.005
TINY_EPS = 0.0001

'''
word, h - col
doc,  w - row
'''

def map_line(line):
	temp = line.replace(' ', '').split(',')
	return (int(temp[0]), int(temp[1]), float(temp[2])) 
	#	   (word_id     , doc_id      , tfidf)

def calculate_loss(pred_matrix, true_matrix):
	# Note: pred_matrix is a matrix of num_docs x num_words
	# Note: true_matrix is the result of ratings.collect()
	error = 0.0
	for (col_block, row_block), tuples in true_matrix:
		for word_id, doc_id, tfidf_score in tuples:
			error += ( tfidf_score - pred_matrix[doc_id, word_id] ) ** 2
	num_nonzero_entries = num_nonzero_entries_bc.value

	# calculate RMSE from error and num_nonzero_entries
	rmse = np.sqrt(error / num_nonzero_entries)
	print('loss: %f, RMSE: %f' % (error, rmse))

def get_worker_id_for_position(word_id, doc_id):
	srs = strata_row_size_bc.value
	return doc_id / srs

def blockify_matrix(worker_id, partition):
	blocks = collections.defaultdict(list)
	srs, scs = strata_row_size_bc.value, strata_col_size_bc.value
	for w_id, score in partition:
		blocks[(score[0]/scs, score[1]/srs)].append(score)
	for item in blocks.items():
		yield item

def filter_block_for_iteration(num_iteration, col_block_index, row_block_index):
	num_workers = num_workers_bc.value
	return ( ((col_block_index - row_block_index) % num_workers) == (num_iteration % num_workers) )

def perform_sgd(num_old_updates, block):
	(col_block, row_block), tuples = block

	srs = strata_row_size_bc.value
	scs = strata_col_size_bc.value

	row_start = row_block * srs
	col_start = col_block * scs
	w_mat_block = w_mat_bc.value[row_start:row_start + srs, :]
	h_mat_block = h_mat_bc.value[col_start:col_start + scs, :]

	tau_0 = 100.0

	num_updated = 0
	for word_id, doc_id, tfidf_score in tuples:

		learning_rate = pow(tau_0 + num_old_updates + num_updated, -beta_value_bc.value)
		if learning_rate < MIN_EPS:
			learning_rate = MIN_EPS

		num_updated += 1
		
		word_index, doc_index = word_id % scs, doc_id % srs

		rating_diff = tfidf_score - np.dot(w_mat_block[doc_index, :], h_mat_block[word_index, :])

		temp = (-2) * learning_rate * rating_diff

		w_gradient = temp * h_mat_block[word_index, :]
		h_gradient = temp * w_mat_block[doc_index, :]

		w_mat_block[doc_index,  :] -= w_gradient
		h_mat_block[word_index, :] -= h_gradient

	return col_block, row_block, w_mat_block, h_mat_block, num_updated

if __name__ == '__main__':
	# read command line arguments
	num_factors, num_workers, num_iterations = map(int, sys.argv[1:4])
	beta_value = float(sys.argv[4])
	inputV_filepath, outputW_filepath, outputH_filepath = sys.argv[5:]

	# create spark context
	conf = pyspark.SparkConf().setAppName("SGD").setMaster("local[{0}]".format(num_workers))
	sc = pyspark.SparkContext(conf=conf)

	# measure time starting here
	start_time = time.time()

	# get tfidf_scores RDD from data
	if os.path.isfile(inputV_filepath):
		# local file
		tfidf_scores = sc.textFile(inputV_filepath).map(map_line)
	else:
		# directory, or on HDFS
		rating_files = sc.wholeTextFiles(inputV_filepath)
		tfidf_scores = rating_files.flatMap(lambda pair: map_line(pair[1]))

	num_workers_bc = sc.broadcast(num_workers)
	beta_value_bc = sc.broadcast(beta_value)

	num_nonzero_entries = tfidf_scores.count()
	num_nonzero_entries_bc = sc.broadcast(num_nonzero_entries)

	# get the max_word_id and max_doc_id.
	max_word_id = tfidf_scores.map(lambda x: x[0]).max()
	max_doc_id  = tfidf_scores.map(lambda x: x[1]).max()

	# build W and H as numpy matrices, initialized randomly with [0,1] values
	w_mat = rand(max_doc_id  + 1, num_factors) + TINY_EPS
	h_mat = rand(max_word_id + 1, num_factors) + TINY_EPS
	w_mat = w_mat.astype(np.float32, copy=False)
	h_mat = h_mat.astype(np.float32, copy=False)

	# print "size W : %d by %d, size H : %d by %d" % (max_doc_id+1, num_factors, max_word_id+1, num_factors)

	# determine strata block size.
	strata_col_size = (max_word_id + 1) / num_workers + ((max_word_id + 1) % num_workers != 0)
	strata_col_size_bc = sc.broadcast(strata_col_size)

	strata_row_size = (max_doc_id  + 1) / num_workers + ((max_doc_id  + 1) % num_workers != 0)
	strata_row_size_bc = sc.broadcast(strata_row_size)

	# print "strata row size %d, strata col size %d" % (strata_row_size, strata_col_size)

	tfidf_scores = tfidf_scores.map(lambda score: (
		get_worker_id_for_position(score[0], score[1]),
		(
			score[0], # word_id
			score[1], # doc_id
			score[2]  # tf_idf_score
		)
		# distribute the partitions of the RDD to all of the workers. 
		# each worker gets one partition.
		# make sure partitioning is preserved for correctness and parallelism efficiency
	)).partitionBy(num_workers) \
	  .mapPartitionsWithIndex(blockify_matrix, preservesPartitioning=True) \
	  .cache()

	# run sgd
	num_old_updates = 0
	for current_iteration in range(num_iterations):

		# broadcast factor matrices to workers
		w_mat_bc = sc.broadcast(w_mat)
		h_mat_bc = sc.broadcast(h_mat)

		# perform_sgd should return a tuple consisting of:
		# (col block index, row block index, updated w block, updated h block, number of updates done)
		# s[0][0] is the col block index, s[0][1] is the row block index
		updated = tfidf_scores \
			.filter(lambda s: filter_block_for_iteration(current_iteration, s[0][0], s[0][1])) \
			.map(lambda block: perform_sgd(num_old_updates, block), preservesPartitioning=True) \
			.collect()

		# unpersist outdated old factor matrices
		w_mat_bc.unpersist()
		h_mat_bc.unpersist()
		# aggregate the updates, update the local w_mat and h_mat
		for block_col, block_row, updated_w, updated_h, num_updates in updated:
			# TODO: update w_mat and h_mat matrices

			# map block_row block_col to real indexes (words and doc ids)
			w_update_start = block_row * strata_row_size
			w_mat[w_update_start:w_update_start+strata_row_size, :] = updated_w

			h_update_start = block_col * strata_col_size
			h_mat[h_update_start:h_update_start+strata_col_size, :] = updated_h

			num_old_updates += num_updates

		# call calculate_loss here for your experiments
		# calculate_loss(np.dot(w_mat, h_mat.transpose()), tfidf_scores.collect())
		
	# print running time
	print( "--- Running time %s seconds ---" % (time.time() - start_time) )

	calculate_loss(np.dot(w_mat, h_mat.transpose()), tfidf_scores.collect())

	# Stop spark
	sc.stop()

	# print w_mat and h_mat to outputW_filepath and outputH_filepath

	np.savetxt(outputW_filepath, h_mat, delimiter=",", fmt="%.12f")
	w_transpose = w_mat.transpose()
	np.savetxt(outputH_filepath, w_transpose, delimiter=",", fmt="%.12f")