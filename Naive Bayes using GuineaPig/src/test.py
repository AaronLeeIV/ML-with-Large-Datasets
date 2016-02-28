from guineapig import *

# compute TFIDF in Guineapig

import sys
import math

class TEST(Planner):
    def flat1(line):
        for i in xrange(0,len(line),2):
            yield(line[i])
    def flat2(line):
        for i in xrange(1,len(line),2):
            yield(line[i])

    params = GPig.getArgvParams()
    trainFile = params['trainFile']
    doc = ReadLines(trainFile) | Map(by = lambda line:line.strip().split(" "))
    t1 = Flatten(doc, flat1)
    t2 = Flatten(doc, flat2)
    t3 = Join(Jin(t1),Jin(t2)) | ReplaceEach(by=lambda(w1,w2):(w1+w2, len(w2)))
   
# always end like this
if __name__ == "__main__":
    TEST().main(sys.argv)