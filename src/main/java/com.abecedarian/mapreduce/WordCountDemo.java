package com.abecedarian.mapreduce;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.io.compress.Lz4Codec;
import org.apache.hadoop.mapred.JobPriority;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

/**
 * Created by tianle.li on 2018/6/4.
 * MapReduce wordCount 入门demo
 */
public class WordCountDemo extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        int status = ToolRunner.run(new WordCountDemo(), args);
        System.exit(status);
    }

    @Override
    public int run(String[] args) throws Exception {
        String inputPath = args[0];
        String outputPath = args[1];
        int numReduceTasks = Integer.parseInt(args[2]);
        Configuration conf = this.getConf();

        conf.set("mapred.job.queue.name", "wirelessdev");
        conf.set("mapreduce.job.name", "WordCountDemo_tianle.li");
        conf.set("mapreduce.map.memory.mb", "8192");
        conf.set("mapred.child.map.java.opts", "-Xmx8192m");
        conf.set("mapreduce.map.java.opts", "-Xmx8192m");
        conf.set("mapreduce.reduce.memory.mb", "8192");
        conf.set("mapred.child.reduce.java.opts", "-Xmx8192m");
        conf.set("mapreduce.reduce.java.opts", "-Xmx8192m");
        conf.set("mapred.job.priority", JobPriority.VERY_HIGH.name());
        conf.setClass("mapred.map.output.compression.codec", Lz4Codec.class, CompressionCodec.class);
        conf.setBoolean("mapred.output.compress", true);
        conf.setClass("mapred.output.compression.codec", GzipCodec.class, CompressionCodec.class);

        Job job = Job.getInstance(conf);
        job.setJarByClass(WordCountDemo.class);

        job.setMapperClass(WordCountDemoMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setReducerClass(WordCountDemoReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.setInputPaths(job, inputPath);
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        job.setNumReduceTasks(numReduceTasks);
        boolean success = job.waitForCompletion(true);
        return success ? 0 : 1;
    }

    private static class WordCountDemoMapper extends Mapper<Object, Text, Text, Text> {
        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] words = line.split(" ");
            for (String word : words) {
                if (StringUtils.isNotBlank(word)) {
                    context.write(new Text(word), new Text("1"));
                }
            }

        }

    }

    private static class WordCountDemoReducer extends Reducer<Text, Text, Text, Text> {

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            int count = 0;
            for (Text value : values) {
                count++;
            }
            context.write(key, new Text(String.valueOf(count)));
        }

    }

}
