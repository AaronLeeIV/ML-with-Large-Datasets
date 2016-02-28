import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.HashSet;

/**
 * class MergeCounts represents an container to merge multiple counts
 * 
 * @author aaronleeiv
 *
 */
public class MergeCounts {

	private String line;
	private String current;
	private String[] article;

	private Set<String> labels = new HashSet<String>();
	private String[] labelList;

	/**
	 * empty constructor
	 */
	public MergeCounts() {}

	/**
	 * method to merge count
	 */
	public void mergeCount() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			line = br.readLine();
			while (line.charAt(0) != 'W') {
				article = line.split("\t");
				labelList = article[1].split(",");
				for (String label : labelList) {
					if (!labels.contains(label)) {
						labels.add(label);
					}
				}
				line = br.readLine();
			}
			System.out.println(labels.size()); // output dom(Y)
			current = line;
			int voc = 1;
			while (line.charAt(0) != 'Y') {
				if (!line.equals(current)) {
					current = line;
					voc++;
				}
				line = br.readLine();
			}
			System.out.println(voc);			// output |V|
			article = line.split("\t");
			current = article[0];
			int sum = Integer.parseInt(article[2]);
			while ((line = br.readLine()) != null) {
				article = line.split("\t");
				if (current.equals(article[0])) {
					sum += Integer.parseInt(article[2]);
				} else {
					System.out.println(current + "\t" + sum);
					current = article[0];
					sum = Integer.parseInt(article[2]);
				}
			}
			System.out.println(current + "\t" + sum);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * main method of class MergeCounts
	 * 
	 * @param args
	 *            not used in here
	 */
	public static void main(String[] args) {
		MergeCounts mc = new MergeCounts();
		mc.mergeCount();
	}
}
