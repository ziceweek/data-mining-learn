// NLP Programming Assignment #3
// NaiveBayes
// 2012

//
// Things for you to implement are marked with TODO!
// Generally, you should not need to touch things *not* marked TODO
//
// Remember that when you submit your code, it is not run from the command line 
// and your main() will *not* be run. To be safest, restrict your changes to
// addExample() and classify() and anything you further invoke from there.
//

import java.util.*;
import java.io.*;

public class NaiveBayes {

    public static boolean FILTER_STOP_WORDS = false; // this gets set in main()
    private static List<String> stopList = readFile(new File("../data/english.stop"));

    public  HashMap<String, Integer> Example_pos = new HashMap<String, Integer>();
    public  HashMap<String, Integer> Example_neg = new HashMap<String, Integer>();
    //pos和neg文档计数
    public HashMap<String,Integer> klass_stat = new HashMap<String, Integer>();

    public int pos_words_count = 0;
    public int neg_words_count = 0;
    
    //TODO

    /**
     * Put your code for adding information to your NB classifier here
     */
    public void  addExample(String klass, List<String> words) {
    	
        //统计pos类和neg类分别有多少
        if(klass_stat.containsKey(klass)){
        	int count = klass_stat.get(klass);
        	count++;
        	klass_stat.put(klass, count);
        }else{
        	klass_stat.put(klass, 1);
        }     
    	
        int count_pos;
        int count_neg;                      //计算出每个词出现的次数并存入哈希图
        for (String word : words ) {
            if (klass.equals("pos"))
            {
            	pos_words_count++;
            	if(Example_pos.containsKey(word))
                {
            		count_pos = Example_pos.get(word);
                    count_pos++;
                    Example_pos.put(word, count_pos);
                }else {
                    Example_pos.put(word, 1);
                }
            }
            else
            {
            	    neg_words_count++;
                	if(Example_neg.containsKey(word)){
	                    count_neg = Example_neg.get(word);
	                    count_neg++;
	                    Example_neg.put(word, count_neg);
                	}
                    else {
                        Example_neg.put(word, 1);
                      }
                }
            
        }
}

    


    //TODO

    /**
     * Put your code here for deciding the class of the input file.
     * Currently, it just randomly chooses "pos" or "negative"
     */
    public String classify(List<String> words) {
    	
    	//先验概率
    	int total = klass_stat.get("pos")+klass_stat.get("neg");
    	double pos_pref = (klass_stat.get("pos")+0.00)/(total+0.00);
    	double neg_pref = (klass_stat.get("neg")+0.00)/(total+0.00);
    	
    	//拉普拉斯平滑系数
    	double laplace = 1;
    	
    	//取得训练集中词的种类数
    	int n_pos =  Example_pos.size();
    	int n_neg =  Example_neg.size();  
    	
    	//每个单词的条件概率中间变量
    	double f_pos=0.00;
    	double f_neg=0.00;

    	//条件概率的乘积
    	double s_pos =1.00;
    	double s_neg =1.00;
    	
    	for(String word:words){
    		
    		//计算单词在积极文档中的条件概率
    		//如果该单词未在积极文档训练集中出现，就默认频率为 0.01
    		if(Example_pos.containsKey(word)){
//    			System.out.println(Example_pos.get(word)+"/"+(pos_words_count+n_pos));
    			f_pos = (Example_pos.get(word)+laplace)/(pos_words_count+n_pos*laplace+0.00);
    		}else{
    			f_pos = laplace /(pos_words_count+n_pos*laplace+0.00) ;
    		}
    		
    	    s_pos=s_pos*f_pos*10000;
    	    System.out.println(s_pos+"*");
    	    
    		
    	    //计算单词在消极文档中的条件概率
    		//如果该单词未在消极文档训练集中出现，就默认频率为 0.01
    		if(Example_neg.containsKey(word)){
//    			System.out.println(Example_neg.get(word)+"/"+(neg_words_count+n_neg));
    			f_neg = (Example_neg.get(word)+laplace)/(neg_words_count+n_neg*laplace+0.00);
    		}else{
    			 f_neg= laplace /(neg_words_count+n_neg*laplace+0.00);
    		}
    		
    		s_neg=s_neg*f_neg*10000;
    		System.out.println(s_neg+"*");
    	}
    	
    	//计算后验概率
    	double pos_lagf = s_pos*pos_pref;
    	double neg_lagf = s_neg*neg_pref;
    	
    	System.out.println("pos:"+pos_lagf+"  neg:"+neg_lagf);
    	
        if (pos_lagf>neg_lagf) {
            return "pos";
        } else {
            return "neg";
        }
    }


    public void train(String trainPath) {
        File trainDir = new File(trainPath);
        if (!trainDir.isDirectory()) {
            System.err.println("[ERROR]\tinvalid training directory specified.  ");
        }

        TrainSplit split = new TrainSplit();
        for (File dir : trainDir.listFiles()) {
            if (!dir.getName().startsWith(".")) {
                List<File> dirList = Arrays.asList(dir.listFiles());
                for (File f : dirList) {
                    split.train.add(f);
                }
            }
        }
        for (File file : split.train) {
            String klass = file.getParentFile().getName();
            List<String> words = readFile(file);
            if (FILTER_STOP_WORDS) {
                words = filterStopWords(words);
            }
            addExample(klass, words);
        }
        return;
    }

    public List<List<String>> readTest(String ch_aux) {
        List<List<String>> data = new ArrayList<List<String>>();
        String[] docs = ch_aux.split("###");
        TrainSplit split = new TrainSplit();
        for (String doc : docs) {
            List<String> words = segmentWords(doc);
            if (FILTER_STOP_WORDS) {
                words = filterStopWords(words);
            }
            data.add(words);
        }
        return data;
    }


    /**
     * This class holds the list of train and test files for a given CV fold
     * constructed in getFolds()
     */
    public static class TrainSplit {
        // training files for this split
        List<File> train = new ArrayList<File>();
        // test files for this split;
        List<File> test = new ArrayList<File>();
    }

    public static int numFolds = 10;

    /**
     * This creates train/test splits for each of the numFold folds.
     * 根据某个规则创建训练集和测试集
     */
    static public List<TrainSplit> getFolds(List<File> files) {
        List<TrainSplit> splits = new ArrayList<TrainSplit>();

        for (Integer fold = 0; fold < numFolds; fold++) {
            TrainSplit split = new TrainSplit();
            for (File file : files) {
                if (file.getName().subSequence(2, 3).equals(fold.toString())) {
                    split.test.add(file);
                } else {
                    split.train.add(file);
                }
            }

            splits.add(split);
        }
        return splits;
    }

    // returns accuracy
    public double evaluate(TrainSplit split) {
        int numCorrect = 0;
        for (File file : split.test) {
            String klass = file.getParentFile().getName();
            List<String> words = readFile(file);
            if (FILTER_STOP_WORDS) {
                words = filterStopWords(words);
            }
            String guess = classify(words);
            if (klass.equals(guess)) {
                numCorrect++;
            }
        }
        return ((double) numCorrect) / split.test.size();
    }


    /**
     * Remove any stop words or punctuation from a list of words.
     */
    public static List<String> filterStopWords(List<String> words) {
        List<String> filtered = new ArrayList<String>();
        for (String word : words) {
            if (!stopList.contains(word) && !word.matches(".*\\W+.*")) {
                filtered.add(word);/**/
            }
        }
        return filtered;
    }

    /**
     * Code for reading a file.  you probably don't want to modify anything here,
     * unless you don't like the way we segment files.
     */
    private static List<String> readFile(File f) {
        try {
            StringBuilder contents = new StringBuilder();

            BufferedReader input = new BufferedReader(new FileReader(f));
            for (String line = input.readLine(); line != null; line = input.readLine()) {
                contents.append(line);
                contents.append("\n");
            }
            input.close();

            return segmentWords(contents.toString());

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    /**
     * Splits lines on whitespace for file reading
     */
    private static List<String> segmentWords(String s) {
        List<String> ret = new ArrayList<String>();

        for (String word : s.split("\\s")) {
            if (word.length() > 0) {
                ret.add(word);
            }
        }
        return ret;
    }

    public List<TrainSplit> getTrainSplits(String trainPath) {
        File trainDir = new File(trainPath);
        if (!trainDir.isDirectory()) {
            System.err.println("[ERROR]\tinvalid training directory specified.  ");
        }
        List<TrainSplit> splits = new ArrayList<TrainSplit>();
        List<File> files = new ArrayList<File>();
        for (File dir : trainDir.listFiles()) {
            if (!dir.getName().startsWith(".")) {
                List<File> dirList = Arrays.asList(dir.listFiles());
                for (File f : dirList) {
                    files.add(f);
                }
            }
        }
        splits = getFolds(files);
        return splits;
    }


    /**
     * build splits according to command line args.  If args.length==1
     * do 10-fold cross validation, if args.length==2 create one TrainSplit
     * with all files from the train_dir and all files from the test_dir
     */
    private static List<TrainSplit> buildSplits(List<String> args) {
        File trainDir = new File(args.get(0));
        if (!trainDir.isDirectory()) {
            System.err.println("[ERROR]\tinvalid training directory specified.  ");
        }

        List<TrainSplit> splits = new ArrayList<TrainSplit>();

        //如果只有一个训练集的目录参数，那就使用10个文件夹交叉验证来区分训练集和测试集
        if (args.size() == 1) {

            System.out.println("[INFO]\tPerforming 10-fold cross-validation on data set:\t" + args.get(0));
            List<File> files = new ArrayList<File>();

            //循环训练目录下的所有文件目录，把训练目录下的所有文件都添加到 files里
            for (File dir : trainDir.listFiles()) {
                if (!dir.getName().startsWith(".")) {
                    //循环遍历目录下得文件
                    List<File> dirList = Arrays.asList(dir.listFiles());
                    for (File f : dirList) {
                        files.add(f);
                    }
                }
            }
            splits = getFolds(files);

            //如果指定了两个参数，那就使用第一个作为训练集，第二个作为测试集
        } else if (args.size() == 2) {
            // testing/training on two different data sets is treated like a single fold
            System.out.println("[INFO]\tTraining on data set:\t" + args.get(0) + " testing on data set:\t" + args.get(1));
            TrainSplit split = new TrainSplit();

            //把第一个参数当成训练集，循环遍历文件放入 split里
            for (File dir : trainDir.listFiles()) {
                if (!dir.getName().startsWith(".")) {
                    List<File> dirList = Arrays.asList(dir.listFiles());
                    for (File f : dirList) {
                        split.train.add(f);
                    }
                }
            }

            //根据参数2新建测试集
            File testDir = new File(args.get(1));

            if (!testDir.isDirectory()) {
                System.err.println("[ERROR]\tinvalid testing directory specified.  ");
            }
            for (File dir : testDir.listFiles()) {
                if (!dir.getName().startsWith(".")) {
                    List<File> dirList = Arrays.asList(dir.listFiles());
                    for (File f : dirList) {
                        split.test.add(f);
                    }
                }
            }
            splits.add(split);
        }
        return splits;
    }

    public void train(TrainSplit split) {
        for (File file : split.train) {
            String klass = file.getParentFile().getName();
            List<String> words = readFile(file);
            if (FILTER_STOP_WORDS) {
                words = filterStopWords(words);
            }
            addExample(klass, words);
        }
    }


    public static void main(String[] args) {

        //读取命令行的参数
        List<String> otherArgs = Arrays.asList(args);
        if (args.length > 0 && args[0].equals("-f")) {
            FILTER_STOP_WORDS = true;
            otherArgs = otherArgs.subList(1, otherArgs.size());
        }
        if (otherArgs.size() < 1 || otherArgs.size() > 2) {
            System.out.println("[ERROR]\tInvalid number of arguments");
            System.out.println("\tUsage: java -cp [-f] trainDir [testDir]");
            System.out.println("\tWith -f flag implements stop word removal.");
            System.out.println("\tIf testDir is omitted, 10-fold cross validation is used for evaluation");
            return;
        }
        System.out.println("[INFO]\tFILTER_STOP_WORDS=" + FILTER_STOP_WORDS);

        //分出训练集和测试集
        List<TrainSplit> splits = buildSplits(otherArgs);

        double avgAccuracy = 0.0;
        int fold = 0;
        
        for (TrainSplit split : splits) {
            NaiveBayes classifier = new NaiveBayes();
            double accuracy = 0.0;

            //遍历训练集
            for (File file : split.train) {
                //读取文件的单词和分类
                String klass = file.getParentFile().getName();
                List<String> words = readFile(file);
               
                if (FILTER_STOP_WORDS) {
                    words = filterStopWords(words);
                }
                //训练方法
                classifier.addExample(klass, words);
            }
            
//            Iterator iter_pos = classifier.Example_pos.entrySet().iterator(); 
//            while (iter_pos.hasNext()) { 
//            	Map.Entry<String,Integer> entry = (Map.Entry<String,Integer>) iter_pos.next();
//                String key = entry.getKey();
//                int val = entry.getValue();
//                System.out.println("pos:"+key+":"+val);
//            } 
//           
//            Iterator iter_neg = classifier.Example_pos.entrySet().iterator(); 
//            while (iter_neg.hasNext()) { 
//            	Map.Entry<String,Integer> entry = (Map.Entry<String,Integer>) iter_neg.next();
//                String key = entry.getKey();
//                int val = entry.getValue();
//                System.out.println("neg:"+key+":"+val);
//            } 

            //遍历测试集
            for (File file : split.test) {
                String klass = file.getParentFile().getName();
                List<String> words = readFile(file);
                if (FILTER_STOP_WORDS) {
                    words = filterStopWords(words);
                }
                String guess = classifier.classify(words);
                if (klass.equals(guess)) {
                    accuracy++;
                }
            }
            accuracy = accuracy / split.test.size();
            avgAccuracy += accuracy;
            System.out.println("[INFO]\tFold " + fold + " Accuracy: " + accuracy);
            fold += 1;
        }
        avgAccuracy = avgAccuracy / numFolds;
        System.out.println("[INFO]\tAccuracy: " + avgAccuracy);
    }
}
