package cn.edu.hust.mit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * 向量空间模型的实现
 * @author kaisheng
 算法描述：
将查询表示成tf-idf权重向量
将每篇文档表示成同一空间下的 tf-idf权重向量
计算两个向量之间的某种相似度(如余弦相似度)
按照相似度大小将文档排序
将前K（如K=10）篇文档返回给用户

 */
public class VSM {
	
	//使用TreeSet去重且有序，用来存储文档集的所有词项
	private TreeSet<String> ts = new TreeSet<>();
	
	//文档集，String表示文档编号，Map<String,Integer>表示每个文档
	private static Map<String,Map<String,Integer>>  docList = new HashMap<>();
	
	public static void main(String[] args) {
		
		//1.构造VSM模型
		VSM vsm = new VSM();
		
		//2.读取文本集
		String filepath = VSM.class.getResource("/dataset").getPath();
		vsm.loadData(new File(filepath));
		System.out.println("Data loaded ...");
		
		Scanner scan = new Scanner(System.in);
		System.out.println("请输入查询文本：");
		String queryDoc = scan.nextLine();
		//3.将查询文本转换成Map集合(可以看成向量)
		Map<String,Integer> query = vsm.str2map(queryDoc);
		System.out.println("请输入需返回的文档集数K:");
		int K = Integer.parseInt(scan.nextLine());
		
		//4.遍历整个文档集，计算出查询与每个文档的相似度
		Set<String> docSet = docList.keySet();
		
		//5.存放每篇文档文件名和该文档与查询的相似度
		Map<String,Double> finalDoc = new HashMap<String,Double>();
		for(String docName: docSet) {
			Map<String,Integer> doc = docList.get(docName);
			double similarity = vsm.calSimilarity(query, doc);
			finalDoc.put(docName, similarity);
		}
		
		//6.根据相似度大小，对finalDoc集合进行降序排序
		List<Map.Entry<String, Double>> results = new ArrayList<Map.Entry<String, Double>>(finalDoc.entrySet()); 
        Collections.sort(results, new Comparator<Map.Entry<String, Double>>() { 
	             public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) { 
	            	 double temp = o2.getValue() - o1.getValue();
	            	 if(temp == 0) {
	            		 return 0;
	            	 }else if(temp > 0) {
	            		 return 1;
	            	 }else {
	            		 return -1;
	            	 }
	            	 //注意：o2.getValue() - o1.getValue()返回的结果0-1之间的小数，经过(int)后得到为0
	            	 //return (int)(o2.getValue() - o1.getValue()); 
             } 
       });
        
       //7.输出得分最高的前K篇文档及其的的得分值
        System.out.println("输出得分最高的前K篇文档名称及其对应的得分值");
        System.out.println("文件名"+"\t"+"得分值");
       for(int i = 0; i < K ;i++) {
    	   Entry<String, Double> entry = results.get(i);
    	   System.out.println(entry.getKey()+"\t"+entry.getValue());
       }
		
	}
	
	//计算查询中词项的权重，返回是一个存储Wtq数组，大小是词项集的大小，这里的权重采用idf表示
	public double[] calQueryWeight(Map<String,Integer> query) {
		//词项集大小
		int termNum = ts.size();
		//词频
		int[] tf = new int[termNum];
		//文档频率
		int[] df = new int[termNum];
		double[] idf = new double[termNum]; 
		double[] Wtq = new double[termNum];
		
		int index = 0;
		for (String s : ts) {
			tf[index] = query.get(s)==null ? 0 :query.get(s);
			df[index] = getDfOfterm(s);
			idf[index] = Math.log10(Double.valueOf(docList.size())/df[index]);
			Wtq[index] = tf[index]*idf[index];		
			index++;
		}
		return Wtq;
	}
	
	//计算每篇文档中的词项权重，这里的权重采用的是tf
	public double[] calDocWeight(Map<String,Integer> doc) {
		int termNum = ts.size();
		int[] tf = new int[termNum];
		int[] wf = tf; 
		double[] Wtd = new double[termNum];
		int index = 0;
		for(String s : ts) {
			tf[index]=doc.get(s)==null ? 0 : doc.get(s);
			index++;
		}
		
		//将wf进行归一化
		double sum = 0;
		for(int i = 0; i < wf.length; i++) {
			sum += Math.pow(wf[i], 2);
		}
		sum = Math.sqrt(sum);
		for(int j =0; j < wf.length; j++) {
			Wtd[j] = wf[j]/sum;
		}
		
		return Wtd;
	}
	
	//计算查询和文档的相似度，即求出Wtq和Wtd的内积
	public double calSimilarity(Map<String,Integer> query,Map<String,Integer> doc) {
		double d = 0.0;
		double[] Wtq = calQueryWeight(query);
		double[] Wtd = calDocWeight(doc);
		for(int i = 0; i < Wtq.length; i++) {
			d+=Wtq[i]*Wtd[i];
		}
		return d;
	} 
	
	
	
	
	//将字符串转换成Map<String,Integer>集合
	public Map<String,Integer> str2map(String s){
		String[] words = s.split(" ");
		Map<String,Integer> map = new HashMap<>();
		for (String string : words) {
			if(map.containsKey(string)) {
				map.put(string, map.get(string)+1);
			}else {
				map.put(string, 1);
			}
		}
		return map;
	}
	
	//返回词项term所在的文档频率
	public int getDfOfterm(String term) {
		int num = 0;
		for(Map<String,Integer> map :docList.values()) {
			if(map.containsKey(term)) {
				num++;
			}
		}
		return num;
	}
	
	
	//加载数据，完整文档，文档集的初始化
	public void loadData(File file) {
		
		if(file.isFile()) {
			try {
				//临时存放每篇文档的词项
				String[] words = null;
				String s = null;
				String temp = "";
				BufferedReader br = new BufferedReader(new FileReader(file));
				while((s=br.readLine())!=null) {
					temp = s+" ";
				}
				words = temp.split(" ");
				
				//存放文档，String表示该文档中的词项，Integer表示每个词项对应的数量
				Map<String,Integer> doc = new HashMap<>();
				
				for (String string : words) {
					//将每个文档中的每个词项添加到词项集TreeSet(去重有序)中
					ts.add(string);
					
					if(doc.containsKey(string)) {
						int count = doc.get(string);
						count++;
						doc.put(string, count);
					}else {
						doc.put(string, 1);
					}
				}
				
				String fname = file.getName();
				String docName = fname.substring(0,fname.indexOf(".txt") );
				
				//将文档编号及对应文档文档放到文档集中
				docList.put(docName, doc);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				loadData(f);
			}
		}
		
	}
	
}
