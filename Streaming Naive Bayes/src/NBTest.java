import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

/**
 * class NBTest represent a tester for Naive Bayes
 * 
 * @author aaronleeiv
 *
 */
public class NBTest {
	private final String DEFAULT_START = "Y=*";

	private Map<String, Double> buffer;

	private List<Double> probs;
	private List<String> cls;

	private String[] article;
	private String line;
	private String testFile;
	private String current;

	private double articleCount;
	private double curArticleCount;
	private double qx;
	private double qy;

	private Set<String> vocabulary;

	public NBTest(String fileName) {
		this.testFile = fileName;
		this.buffer = new HashMap<String, Double>();
		this.probs = new ArrayList<Double>();
		this.cls = new ArrayList<String>();
		this.vocabulary = new HashSet<String>();

		this.articleCount = 0.0;
		this.curArticleCount = 0.0;
		this.qx = 0.0;
		this.qy = 0.0;
	}

	private void readCountAndClassify() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			qy = Double.parseDouble(br.readLine());
			qx = Double.parseDouble(br.readLine());
			// qx = 1.0 / (double) vocabulary.size();
			// local vocabularysize
			line = br.readLine();
			article = line.split("\t");
			if (!article[0].substring(0, DEFAULT_START.length()).equals(DEFAULT_START)) {
				throw new IllegalArgumentException("Wrong input! Expected input should start with \"Y=*\"");
			}
			setArticleCount(Double.parseDouble(article[1]));
			line = br.readLine();
			article = line.split("\t");
			current = article[0].substring(2);
			setCurrentArticleCount(Double.parseDouble(article[1]));
			while ((line = br.readLine()) != null) {
				article = line.split("\t");
				double count = Double.parseDouble(article[1]);
				int commaIndex = article[0].indexOf(',');
				if (commaIndex == -1) {
					classify(current);
					clearBuffer();
					current = article[0].substring(2);
					setCurrentArticleCount(count);
				} else {
					int lastEqualIndex = article[0].lastIndexOf('=');
					buffer.put(article[0].substring(lastEqualIndex + 1), count);
				}
			}
			classify(current);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < cls.size(); i++) {
			System.out.println(cls.get(i) + "\t" + String.format("%.4f", probs.get(i)));
		}

	}

	// private void getVocabulary() {
	// 	try {
	// 		BufferedReader fileReader = new BufferedReader(new FileReader(testFile));
	// 		String[] article;
	// 		String line;
	// 		while ((line = fileReader.readLine()) != null) {
	// 			article = line.split("\t");
	// 			Vector<String> tokens = tokenizeDoc(article[1]);
	// 			for (String word : tokens) {
	// 				if (!vocabulary.contains(word)) {
	// 					vocabulary.add(word);
	// 				}
	// 			}
	// 		}
	// 		fileReader.close();
	// 	} catch (IOException e) {
	// 		e.printStackTrace();
	// 	}
	// }

	private void classify(String cl) {
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(testFile));
			String[] article;
			String line;
			int k = 0;
			while ((line = fileReader.readLine()) != null) {
				article = line.split("\t");
				Vector<String> tokens = tokenizeDoc(article[1]);
				double prob = 0.0;
				for (String word : tokens) {
					// if (vocabulary.contains(word)) {
					// 	Double temp = buffer.get(word);
					// 	double num = temp == null ? 0 : temp;
					// 	prob += ( Math.log(num + qx) - Math.log(curArticleCount + 1) );
					// }
					Double temp = buffer.get(word);
					double num = temp == null ? 0 : temp;
					prob += ( Math.log(num + 1) - Math.log(curArticleCount + qx) );
				}
				prob += ( Math.log(curArticleCount + 1) - Math.log(articleCount + qy) );
				if (probs.size() == k) {
					probs.add(prob);
					cls.add(cl);
				} else if (prob > probs.get(k)) {
					probs.set(k, prob);
					cls.set(k, cl);
				}
				k++;
			}
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setArticleCount(double articleCount) {
		this.articleCount = articleCount;
	}

	private void setCurrentArticleCount(double curArticleCount) {
		this.curArticleCount = curArticleCount;
	}

	private void clearBuffer() {
		this.buffer.clear();
	}

	private static Vector<String> tokenizeDoc(String cur_doc) {
		String[] words = cur_doc.split("\\s+");
		Vector<String> tokens = new Vector<String>();
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].replaceAll("\\W", "");
			if (words[i].length() > 0) {
				tokens.add(words[i]);
			}
		}
		return tokens;
	}

	/**
	 * main method of class NBTest
	 * 
	 * @param args
	 *            system input of filename
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("IllegalArgument!");
			System.exit(-1);
		}
		NBTest test = new NBTest(args[0]);
		// test.getVocabulary();
		test.readCountAndClassify();
	}
}
