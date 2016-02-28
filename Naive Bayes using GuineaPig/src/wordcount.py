# always start like this
from guineapig import *
import sys

# supporting routines can go here
def tokens(line):
	for tok in line.split():
		yield tok.lower()

#always subclass Planner
class WordCount(Planner):
	params = GPig.getArgvParams()
	trainFile = params['trainFile']
	wc = ReadLines(trainFile) | Flatten( by = tokens ) | Group( by = lambda x : x, reducingTo = ReduceToCount() )

# always end like this
if __name__ == "__main__":
	WordCount().main(sys.argv)