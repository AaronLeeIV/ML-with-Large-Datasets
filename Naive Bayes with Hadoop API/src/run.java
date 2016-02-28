import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * class run is a warpper for hadoop infrastructure
 * 
 */
public class run {
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			throw new IllegalArgumentException("Illegal or Inappropriate Arguments.");
		}

		Configuration config = new Configuration();
		/*
		 * When tested in aws, I didn't set the config.
		 * To ensure the consistence, I committed out the config.set here.
		 */
		config.set("mapred.reduce.slowstart.completed.maps", "1.0");
		config.set("mapreduce.reduce.shuffle.input.buffer.percent", "0.3");

		Job job = new Job(config, "Hadoop NB Train");
		job.setJarByClass(NB_train_hadoop.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(NB_train_hadoop.Map.class);
		job.setReducerClass(NB_train_hadoop.Reduce.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		job.setNumReduceTasks(Integer.parseInt(args[2]));

		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		job.waitForCompletion(true);
	}	
}
