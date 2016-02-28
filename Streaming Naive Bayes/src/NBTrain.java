import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

/**
 * class NBTrain represents a trainner for Naive Bayes
 * 
 * @author aaronleeiv
 *
 */
public class NBTrain {
	private static final int BUFFER_SIZE_LIMIT = 50;
	private static final int TOKEN_INDEX = 7;
	private static final String DEFAULT_FORMAT = "Y= ,W= ";
	
	private BufferedWriter bw;
	private BufferedReader br;
	private Map<String, Integer> buffer;
	private Map<String, Integer> labelCount;
	
	private StringBuffer sb;
	private String line;
	private String[] article;
	private String[] labels;
	private Vector<String> tokens;

	private int articleTotal;
	private int bufferSize;

	public NBTrain() {
		this.br = new BufferedReader(new InputStreamReader(System.in));
		this.bw = new BufferedWriter(new OutputStreamWriter(System.out));
//		try {
//			this.br = new BufferedReader(new FileReader("src/resource/tiny.test.old"));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		this.buffer = new HashMap<String, Integer>();
		this.labelCount = new HashMap<String, Integer>();
		this.sb = new StringBuffer();
		
		this.articleTotal = 0;
		this.bufferSize = 0;
	}

	/**
	 * method to count labels and tokens
	 */
	public void count() {
		try {
			while ((line = br.readLine()) != null) {
				if (bufferSize > BUFFER_SIZE_LIMIT) {
					writeBuffer();
					clearBuffer();
				}
				article = line.split("\\t");
				labels = article[0].split(",");
				tokens = tokenizeDoc(article[1]);
				Integer count;
				String key;
				for (String label : labels) {
					if ((count = labelCount.get(label)) == null) {
						labelCount.put(label, 1);
					} else {
						labelCount.put(label, count + 1);
					}
					articleTotal++;
				}
				resetStringBuffer(DEFAULT_FORMAT);
				for (String label : labels) {
					int i = sb.indexOf(",");
					sb.replace(2, i, label);
					i = sb.indexOf(",") + 3;
					for (String token : tokens) {
						sb.replace(i, sb.length(), token);
						key = sb.toString();
						count = buffer.get(key);
						if (count == null) {
							buffer.put(key, 1);
						} else {
							buffer.put(key, count + 1);
						}
					}
				}
				bufferSize++;
			}
			writeBuffer();
			String labels = new String("Labels:\t");
			for (Entry<String, Integer> e : labelCount.entrySet()) {
				bw.write("Y=" + e.getKey() + "\t+=\t" + e.getValue() + "\n");
				labels += (e.getKey() + ",");
			}
			labels += "\n";
			bw.write(labels);
			bw.write("Y=*\t+=\t" + articleTotal + "\n");
			br.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	private void writeBuffer() {
		for (Entry<String, Integer> e : buffer.entrySet()) {
			String key = e.getKey();
			try {
				bw.write(key + "\t+=\t" + e.getValue() + "\n");
				bw.write("W=" + key.substring(TOKEN_INDEX) + "\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
		
	private void clearBuffer() {
		buffer.clear();
		bufferSize = 0;
	}
	
	private void resetStringBuffer(String str) {
		sb.replace(0, sb.length(), str);
	}
	
	/**
	 * main method of class NBTrain
	 * 
	 * @param args
	 *            not used in here
	 */
	public static void main(String[] args) {
		NBTrain train = new NBTrain();
		train.count();
	}
}
