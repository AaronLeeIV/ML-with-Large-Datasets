import java.io.IOException;
import java.util.*;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;

/**
 * class NB_train_hadoop represents a trainer for Naive Bayes
 * 
 */

public class NB_train_hadoop {

	/**
	 * class Map represents the mapper of this trainer 
	 *
	 */
	public static class Map extends Mapper<Object, Text, Text, IntWritable> {
		private Text outKey = new Text();
		private IntWritable outVal = new IntWritable();		
		
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			String[] articles = value.toString().split("\\t");
			ArrayList<String> labels = new ArrayList<String>();
			for(String label: articles[1].split(",")) {
				labels.add(label);
			}
			Vector<String> tokens = tokenizeDoc(articles[2]);
			
			for (String label : labels) {
				HashMap<String, Integer> mMap = new HashMap<String, Integer>();
				for (String token : tokens) {
					if (mMap.containsKey(token)) {
						int currentCount = mMap.get(token);
						mMap.put(token, currentCount + 1);
					}
					else {
						mMap.put(token, 1);
					}
				}
				outputLabel(label, 1, context);
				outputLabel("*", 1, context);
				
				Iterator<String> iterator = mMap.keySet().iterator();
			    while (iterator.hasNext()) {
			        String token = iterator.next();
			        outputPair(label, token, mMap.get(token), context);
			    }
			    outputPair(label, "*", tokens.size(), context);
			}
		}

		public void outputLabel(String label, int count, Context context) throws IOException, InterruptedException {
			outKey.set("Y=" + label);
			outVal.set(count);
			context.write(outKey, outVal);
		}		
		
		public void outputPair(String label, String token, int count, Context context) throws IOException, InterruptedException {
			outKey.set("Y=" + label + "," + "W=" + token);
			outVal.set(count);
			context.write(outKey, outVal);
		}
	
	}
	
	/**
	 * class Reduce represents the reducer of this trainer 
	 *
	 */
	public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable count = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable value : values) {
				sum += value.get();
			}
			count.set(sum);
			context.write(key, count);
		}
	}
	
	/**
	 * method tokenizeDoc is a helper function to parse the input
	 *
	 */
	public static Vector<String> tokenizeDoc(String cur_doc) {
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
}