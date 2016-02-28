from guineapig import *
import sys
import math
import re

# supporting routines can go here
def tokenize(doc):
	# Google History Stopwords from http://www.ranks.nl/stopwords
	stopWords = set(['a', 'able', 'about', 'above', 'abst', 'accordance', 'according', 'accordingly', 'across', 'act', 'actually', 'added', 'adj', 
		'affected', 'affecting', 'affects', 'after', 'afterwards', 'again', 'against', 'ah', 'all', 'almost', 'alone', 'along', 'already', 'also', 
		'although', 'always', 'am', 'among', 'amongst', 'an', 'and', 'announce', 'another', 'any', 'anybody', 'anyhow', 'anymore', 'anyone', 'anything', 
		'anyway', 'anyways', 'anywhere', 'apparently', 'approximately', 'are', 'aren', 'arent', 'arise', 'around', 'as', 'aside', 'ask', 'asking', 'at', 
		'auth', 'available', 'away', 'awfully', 'b', 'back', 'be', 'became', 'because', 'become', 'becomes', 'becoming', 'been', 'before', 'beforehand', 
		'begin', 'beginning', 'beginnings', 'begins', 'behind', 'being', 'believe', 'below', 'beside', 'besides', 'between', 'beyond', 'biol', 'both', 
		'brief', 'briefly', 'but', 'by', 'c', 'ca', 'came', 'can', 'cannot', "can't", 'cause', 'causes', 'certain', 'certainly', 'co', 'com', 'come', 
		'comes', 'contain', 'containing', 'contains', 'could', 'couldnt', 'd', 'date', 'did', "didn't", 'different', 'do', 'does', "doesn't", 'doing', 
		'done', "don't", 'down', 'downwards', 'due', 'during', 'e', 'each', 'ed', 'edu', 'effect', 'eg', 'eight', 'eighty', 'either', 'else', 'elsewhere', 
		'end', 'ending', 'enough', 'especially', 'et', 'et-al', 'etc', 'even', 'ever', 'every', 'everybody', 'everyone', 'everything', 'everywhere', 'ex', 
		'except', 'f', 'far', 'few', 'ff', 'fifth', 'first', 'five', 'fix', 'followed', 'following', 'follows', 'for', 'former', 'formerly', 'forth', 
		'found', 'four', 'from', 'further', 'furthermore', 'g', 'gave', 'get', 'gets', 'getting', 'give', 'given', 'gives', 'giving', 'go', 'goes', 
		'gone', 'got', 'gotten', 'h', 'had', 'happens', 'hardly', 'has', "hasn't", 'have', "haven't", 'having', 'he', 'hed', 'hence', 'her', 'here', 
		'hereafter', 'hereby', 'herein', 'heres', 'hereupon', 'hers', 'herself', 'hes', 'hi', 'hid', 'him', 'himself', 'his', 'hither', 'home', 'how', 
		'howbeit', 'however', 'hundred', 'i', 'id', 'ie', 'if', "i'll", 'im', 'immediate', 'immediately', 'importance', 'important', 'in', 'inc', 'indeed', 
		'index', 'information', 'instead', 'into', 'invention', 'inward', 'is', "isn't", 'it', 'itd', "it'll", 'its', 'itself', "i've", 'j', 'just', 'k', 
		'keep', 'keeps', 'kept', 'kg', 'km', 'know', 'known', 'knows', 'l', 'largely', 'last', 'lately', 'later', 'latter', 'latterly', 'least', 'less', 
		'lest', 'let', 'lets', 'like', 'liked', 'likely', 'line', 'little', "'ll", 'look', 'looking', 'looks', 'ltd', 'm', 'made', 'mainly', 'make', 
		'makes', 'many', 'may', 'maybe', 'me', 'mean', 'means', 'meantime', 'meanwhile', 'merely', 'mg', 'might', 'million', 'miss', 'ml', 'more', 
		'moreover', 'most', 'mostly', 'mr', 'mrs', 'much', 'mug', 'must', 'my', 'myself', 'n', 'na', 'name', 'namely', 'nay', 'nd', 'near', 'nearly', 
		'necessarily', 'necessary', 'need', 'needs', 'neither', 'never', 'nevertheless', 'new', 'next', 'nine', 'ninety', 'no', 'nobody', 'non', 'none', 
		'nonetheless', 'noone', 'nor', 'normally', 'nos', 'not', 'noted', 'nothing', 'now', 'nowhere', 'o', 'obtain', 'obtained', 'obviously', 'of', 'off', 
		'often', 'oh', 'ok', 'okay', 'old', 'omitted', 'on', 'once', 'one', 'ones', 'only', 'onto', 'or', 'ord', 'other', 'others', 'otherwise', 'ought', 
		'our', 'ours', 'ourselves', 'out', 'outside', 'over', 'overall', 'owing', 'own', 'p', 'page', 'pages', 'part', 'particular', 'particularly', 'past', 
		'per', 'perhaps', 'placed', 'please', 'plus', 'poorly', 'possible', 'possibly', 'potentially', 'pp', 'predominantly', 'present', 'previously', 
		'primarily', 'probably', 'promptly', 'proud', 'provides', 'put', 'q', 'que', 'quickly', 'quite', 'qv', 'r', 'ran', 'rather', 'rd', 're', 'readily', 
		'really', 'recent', 'recently', 'ref', 'refs', 'regarding', 'regardless', 'regards', 'related', 'relatively', 'research', 'respectively', 'resulted', 
		'resulting', 'results', 'right', 'run', 's', 'said', 'same', 'saw', 'say', 'saying', 'says', 'sec', 'section', 'see', 'seeing', 'seem', 'seemed', 
		'seeming', 'seems', 'seen', 'self', 'selves', 'sent', 'seven', 'several', 'shall', 'she', 'shed', "she'll", 'shes', 'should', "shouldn't", 'show', 
		'showed', 'shown', 'showns', 'shows', 'significant', 'significantly', 'similar', 'similarly', 'since', 'six', 'slightly', 'so', 'some', 'somebody', 
		'somehow', 'someone', 'somethan', 'something', 'sometime', 'sometimes', 'somewhat', 'somewhere', 'soon', 'sorry', 'specifically', 'specified', 
		'specify', 'specifying', 'still', 'stop', 'strongly', 'sub', 'substantially', 'successfully', 'such', 'sufficiently', 'suggest', 'sup', 'sure', 
		't', 'take', 'taken', 'taking', 'tell', 'tends', 'th', 'than', 'thank', 'thanks', 'thanx', 'that', "that'll", 'thats', "that've", 'the', 'their', 
		'theirs', 'them', 'themselves', 'then', 'thence', 'there', 'thereafter', 'thereby', 'thered', 'therefore', 'therein', "there'll", 'thereof', 
		'therere', 'theres', 'thereto', 'thereupon', "there've", 'these', 'they', 'theyd', "they'll", 'theyre', "they've", 'think', 'this', 'those', 
		'thou', 'though', 'thoughh', 'thousand', 'throug', 'through', 'throughout', 'thru', 'thus', 'til', 'tip', 'to', 'together', 'too', 'took', 'toward',
		 'towards', 'tried', 'tries', 'truly', 'try', 'trying', 'ts', 'twice', 'two', 'u', 'un', 'under', 'unfortunately', 'unless', 'unlike', 'unlikely', 
		 'until', 'unto', 'up', 'upon', 'ups', 'us', 'use', 'used', 'useful', 'usefully', 'usefulness', 'uses', 'using', 'usually', 'v', 'value', 'various', 
		 "'ve", 'very', 'via', 'viz', 'vol', 'vols', 'vs', 'w', 'want', 'wants', 'was', 'wasnt', 'way', 'we', 'wed', 'welcome', "we'll", 'went', 'were', 
		 'werent', "we've", 'what', 'whatever', "what'll", 'whats', 'when', 'whence', 'whenever', 'where', 'whereafter', 'whereas', 'whereby', 'wherein', 
		 'wheres', 'whereupon', 'wherever', 'whether', 'which', 'while', 'whim', 'whither', 'who', 'whod', 'whoever', 'whole', "who'll", 'whom', 'whomever', 
		 'whos', 'whose', 'why', 'widely', 'willing', 'wish', 'with', 'within', 'without', 'wont', 'words', 'world', 'would', 'wouldnt', 'www', 'x', 'y', 
		 'yes', 'yet', 'you', 'youd', "you'll", 'your', "you're", 'yours', 'yourself', 'yourselves', "you've", 'z', 'zero'])
	HELPER = re.compile(r"""(?:[a-z][a-z'\-_]+[a-z]) | (?:[A-Za-z_]+)""", re.X | re.I | re.U)
	data = HELPER.findall(doc)
	# keep words with/without apostrophes or dashes, remove numbers and other simbols
	for token in data:
		if token.lower() not in stopWords:
			yield token.lower()
		else:
			continue

def loadDictView(view):
	# (key, val) dictionary wrapper from https://github.com/TeamCohen/GuineaPig/blob/master/tutorial/smallvoc-tfidf.py
	result = dict()
	for (key, val) in GPig.rowsOf(view):
		result[key] = val
	return result

def compose(mList):
	result = dict()
	for (label, count) in mList:
		if not label in result:
			result[label] = [count]
		else:
			result[label].append(count)
	return result

def reduceToProb(docId, dict, dummyList):
	[(domY, sizeY, domX, labeldict)] = dummyList
	result = ('label', 0.0)
	for label in dict:
		labelcount, temp = labeldict[label], 0.0
		for count in dict[label]:
			temp += math.log(count + 1.0) - math.log(labelcount + domX)
		temp += math.log(labelcount + 1.0) - math.log(sizeY + domY)
		if temp < result[1]:
			result = (label, temp)
	return (docId, result[0], result[1])

#always subclass Planner
class NB(Planner):
	# params is a dictionary of params given on the command line. 
	params = GPig.getArgvParams()
	trainFile = params['trainFile']
	testFile = params['testFile']

	labelsDoc = ReadLines( trainFile ) | Map( by = lambda line : line.strip().split("\t")[1:] ) #| Distinct()
	labelsCount = Map( labelsDoc, by = lambda (labels, doc) : labels.split(",") ) |\
				  Flatten( by = lambda labels : map (lambda label : label, labels) ) |\
				  Group( reducingTo = ReduceToCount() )
	'''
	labels is a row filled with all the labels and it occurance time in the training dataset
	('Agent', 1)\n('Animal', 2)\n ...
	'''
	domY = Group( labelsCount, by = lambda row : 'domY', reducingTo = ReduceToCount() )
	'''domY is the size of possible labels ('domY', |domY|)'''
	sizeY = Group( labelsCount, by = lambda row : 'Y=ANY', reducingTo = ReduceTo(int, by = lambda accum, (label, count) : accum + count) )
	'''sizeY is the number of different Y, i.e. ('Y=ANY', |Y=ANY|)'''
	wordLabelCount = Flatten( labelsDoc, by = lambda (labels, doc) : map (lambda l : (l, tokenize(doc)), labels.split(",")) ) |\
					 Flatten( by = lambda (label, words) : map (lambda w : (label, w), words) ) |\
					 Group( reducingTo = ReduceToCount() ) |\
					 Group( by = lambda ((label, word), count) : word, retaining = lambda ((label, word), count) : (label, count), reducingTo = ReduceToList() )
	'''
	wordLabelCount is a row filled with (word1, [(label1, count1), (label2, count2), ...])
	'''
	domX = Map( wordLabelCount, by = lambda (word, dummy) : word ) | Distinct() | Group( by = lambda row : 'domX', reducingTo = ReduceToCount() )
	'''domX is the size of the vocabulary ('domX', |V|)'''
	
	testDoc = ReadLines( testFile ) | Map( by = lambda line : line.strip().split("\t")[::2] ) #| Distinct()
	testIdWord = Map( testDoc, by = lambda (docId, doc) : (docId, tokenize(doc)) ) |\
				 Flatten( by = lambda (docId, words) : map (lambda w : (docId, w), words) )
	temp = Join( Jin(testIdWord, by = lambda (docId, word) : word), Jin(wordLabelCount, by = lambda (word, list) : word) ) |\
		   Group( by = lambda ((docId, w), (w1, list)) : docId, retaining = lambda ((docId, w), (w1, list)) : list, reducingTo = ReduceTo(list, by=lambda accum, val : accum + val) ) |\
		   Map( by = lambda (docId, list) : (docId, compose(list)) )
	'''temp is a row filled with (docId, {label1: [count11, count12, ...], label2 : [count21, count22, ...], ...}'''
	dummyJoin = Join( Jin(domY, by = lambda row : 'const'), Jin(sizeY, by = lambda row : 'const'), Jin(domX, by = lambda row : 'const') ) |\
				Augment( sideview = labelsCount, loadedBy = loadDictView ) |\
				Map( by = lambda ((domY, sizeY, domX), labeldict) : (float(domY[-1]), float(sizeY[-1]), float(domX[-1]), labeldict) )
	'''dummyJoin is a row filled with (domY, sizeY, domX, labelcount dictionary'''
	output = Augment( temp, sideview = dummyJoin ) |\
			 Map( by = lambda ((docId, dict), dummyList) : reduceToProb(docId, dict, dummyList) )

# always end like this
if __name__ == "__main__":
	NB().main(sys.argv)

# supporting routines can go here
