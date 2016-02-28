import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * class LR represents a classifier for Logistic Regression
 * @author aaronleeiv
 */
public class LR {

	private final String[] allLabels = { "nl", "el", "ru", "sl", "pl", "ca", "fr", "tr", "hu", "de", "hr", "es", "ga", "pt" };
	private int labelNum = 14;

	private final int vocSize; 		// vocabulary size
	private double ita; 			// learning rate
	private double miu; 			// regularization coefficient
	private final int T; 			// max number of iterations
	private final int size; 		// size of training dataset
	private final String testData; 	// input file name

	private HashMap<String, Integer> labelIndices;
	private double[][] B;
	private int[][] A;

	/**
	 *	Constructor for class LR
	 */
	public LR(int vocSize, double ita, double miu, int T, int size, String testData) {
		this.vocSize = vocSize;
		this.ita = ita;
		this.miu = miu;
		// this.miu = 2 * miu;
		this.T = T;
		this.size = size;
		this.testData = testData;

		this.labelIndices = new HashMap<String, Integer>();
		for (int i = 0; i < labelNum; i++) {
			labelIndices.put(allLabels[i], i);
		}
		this.B = new double[labelNum][vocSize];
		this.A = new int[labelNum][vocSize];
	}
	
	private double sigmoid(double score) {
		if (score > 20) 
			score = 20;
		else if (score < -20) 
			score = -20;
		double exp = Math.exp(score);
		return exp / (1 + exp);
	}
	
	public void train() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line;
		String[] parts;
		Set<Integer> labels = new HashSet<Integer>();
		int k = 0;				// example count
		int t = 1; 				// iteration count
		double lambda = ita; 	// learning rate
		double dotProduct, p, y;
		int[] features;

		// double LCL = 0.0;
		
		while ((line = br.readLine()) != null) {
			k++;
			parts = line.split("\t");
			for (String s : parts[0].split(",")) {
				labels.add(labelIndices.get(s));
			}
			features = tokenizeDoc(parts[1]);
			for(int i = 0; i < labelNum; i++) {
				dotProduct = 0.0;
				for (int j : features) {
					dotProduct += B[i][j];
				}
				p = sigmoid(dotProduct);
				// can be improved
				y = labels.contains(i) ? 1.0 : 0.0;
				
				// if (y == 1.0) {
				// 	LCL += Math.log(p);
				// } else {
				// 	LCL += Math.log(1-p);
				// }
				
				for (int j : features) {
					B[i][j] *= Math.pow(1.0 - lambda * miu, (double)(k - A[i][j]));
					B[i][j] += lambda * (y - p);
					A[i][j] = k;
				}
			}
			labels.clear();
			if (k % size == 0) {
				
				// System.out.println("Iteration " + String.valueOf(t) + "- LCL: " + LCL);
				// LCL = 0.0;

				if (t == T) {
					for(int i = 0; i < labelNum; i++) {
						for (int j = 0; j < vocSize; j++) {
							B[i][j] *= Math.pow(1.0 - lambda * miu, (double)(k - A[i][j]));
						}
					}
					break;
				}
				else {
					t++;
					lambda = ita / t / t;
				}
			}
		}
		br.close();
	}
	
	// public void test() throws IOException {
	// 	int correct = 0;
	// 	int count = 0;
	// 	BufferedReader br = new BufferedReader(new FileReader(testData));
	// 	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
	// 	String line;
	// 	String[] parts;
	// 	double dotProduct, p;
	// 	int[] features;
	// 	HashSet<Integer> labels = new HashSet<Integer>();
	// 	while ((line = br.readLine()) != null) {
	// 		count++;
	// 		parts = line.split("\t");
	// 		for (String s : parts[1].split("\t")) {
	// 			labels.add(labelIndices.get(s));
	// 		}
	// 		features = tokenizeDoc(parts[1]);
	// 		//compute score for each label
	// 		for(Integer i : labelIndices.values()){
	// 			//compute p
	// 			dotProduct = 0.0;
	// 			for (int j : features) {
	// 				dotProduct += B[i][j];
	// 			}
	// 			p = sigmoid(dotProduct);
	// 			if (p >= 0.5) {
	// 				if (labels.contains(i)) {
	// 					correct++;
	// 				}
	// 			} else {
	// 				if (!labels.contains(i)) {
	// 					correct++;
	// 				}
	// 			}
	// 		}
	// 		labels.clear();
	// 	}
	// 	double correctness = (double)correct / (double)(count * 14);
	// 	br.close();
	// 	bw.write(String.valueOf(correctness));
	// 	bw.close();
	// }
	
	public void test() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(testData));
		String line;
		String[] parts;
		double dotProduct, p;
		int[] features;
		int temp = labelNum - 1;
		while ((line = br.readLine()) != null) {
			parts = line.split("\t");
			features = tokenizeDoc(parts[1]);
			//compute score for each label
			for(int i = 0; i < labelNum; i++) {
				//compute p
				dotProduct = 0.0;
				for (int j : features) {
					dotProduct += B[i][j];
				}
				p = sigmoid(dotProduct);
				System.out.format("%s\t%f", allLabels[i], p);
				if ( i < temp ) {
					System.out.print(",");
				}
			}
			System.out.print("\n");
		}
		br.close();
	}

	private int[] tokenizeDoc(String cur_doc) {
		String[] words = cur_doc.split("\\s+");
		int temp = words.length;
		int[] features = new int[temp];
		int wordHash;
		for (int i = 0; i < temp; i++) {
			wordHash = words[i].hashCode() % vocSize;
			features[i] = wordHash < 0 ? wordHash + vocSize : wordHash;
		}
		return features;
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 6) {
			throw new Exception("Wrong number of arguments.");
		}
		LR lr = new LR(Integer.parseInt(args[0]), 		// vocabulary size
					   Double.parseDouble(args[1]),		// learning rate
					   Double.parseDouble(args[2]),		// regularization coefficient
					   Integer.parseInt(args[3]), 		// max number of iterations
					   Integer.parseInt(args[4]), 		// size of training dataset
					   args[5]);						// input file name
		lr.train();
		lr.test();
	}

}