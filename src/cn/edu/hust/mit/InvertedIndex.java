package cn.edu.hust.mit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * 建立倒排索引 实现布尔查询A and B和A or B的处理
 * @author kaisheng
 */
public class InvertedIndex {
	// 存放词项以及该此项所在的文档集
	private Map<String, ArrayList<String>> map = new HashMap<>();
	// 存放所有的文档名称
	private ArrayList<String> list;

	public static void main(String[] args) {
		// 1.初始化倒排索引对象
		InvertedIndex index = new InvertedIndex();

		// 2.获取读取文件
		String filepath = InvertedIndex.class.getResource("/dataset").getPath();
		File f = new File(filepath);

		// 3.构建倒排索引
		index.CreateIndex(f);

		// 4.根据key值对倒排索引排序
		index.map = sortMapByKey(index.map);
		System.out.println("打印倒排索引表：");
		for (Map.Entry<String, ArrayList<String>> map : index.map.entrySet()) {
			System.out.println(
					"词项：" + map.getKey() + "文档频率:" + map.getValue().size() + "-->倒排索引表：" + map.getValue());
		}

		Scanner scan = new Scanner(System.in);
		// 5.实现布尔查询A and B
		System.out.println("请输入查询词项A:");
		String A = scan.nextLine();
		System.out.println("请输入查询词项B:");
		String B = scan.nextLine();
		ArrayList<String> Alist = index.map.get(A);

		System.out.print(A + "-->");
		int Alen = Alist.size();
		for (int i = 0; i < Alen - 1; i++) {
			System.out.print(Alist.get(i) + "-->");
		}
		System.out.println(Alist.get(Alen - 1));

		ArrayList<String> Blist = index.map.get(B);

		System.out.print(B + "-->");
		int Blen = Blist.size();
		for (int j = 0; j < Blen - 1; j++) {
			System.out.print(Blist.get(j) + "-->");
		}
		System.out.println(Blist.get(Blen - 1));

		ArrayList<String> Atemp = new ArrayList<String>();
		ArrayList<String> Atemp2 = new ArrayList<String>();
		Atemp.addAll(Alist);
		Atemp2.addAll(Alist);

		ArrayList<String> Btemp = new ArrayList<String>();
		ArrayList<String> Btemp2 = new ArrayList<String>();
		Btemp.addAll(Blist);
		Btemp2.addAll(Blist);

		// 6.实现布尔查询A and B
		Atemp.retainAll(Btemp);
		int Clist = Atemp.size();
		System.out.print("交集==>");
		for (int i = 0; i < Clist - 1; i++) {
			System.out.print(Atemp.get(i) + "-->");
		}
		System.out.println(Atemp.get(Clist - 1));

		// 实现布尔查询AorB的处理
		ArrayList<String> finalList = new ArrayList<>();
		for (String str : Atemp2) {
			if (Btemp2.contains(str)) {
				Btemp2.remove(str);
			}
			finalList.add(str);
		}
		if (Btemp2.size() > 0) {
			for (String str : Btemp2) {
				finalList.add(str);
			}
		}
		int Clist2 = finalList.size();
		System.out.print("并集==>");
		for (int i = 0; i < Clist2 - 1; i++) {
			System.out.print(finalList.get(i) + "-->");
		}
		System.out.println(finalList.get(Clist2 - 1));

	}

	/**
	 * 构建倒排索引
	 * @param f
	 */
	public void CreateIndex(File f) {
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			for (File file : files) {
				CreateIndex(file);
			}
		}

		if (f.isFile()) {
			String[] words = null;
			try {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String s = null;
				String temp = "";
				while ((s = reader.readLine()) != null) {
					// 获取单词
					temp += s + " ";
				}
				//获取每篇文档的名称，即文档号
				String filepath = f.getName().substring(0, f.getName().indexOf(".txt"));
				//System.out.println("文档" + filepath + "中的内容为：");
				//输出每篇文档的内容
				//System.out.println(temp);
				words = temp.split(" ");
				for (String string : words) {
					if (!map.containsKey(string)) {
						list = new ArrayList<String>();
						list.add(filepath);
						map.put(string, list);
					} else {
						list = map.get(string);
						// 如果没有包含过此文件名，则把文件名放入,自处文档编号根据程序逻辑自动排好序了
						if (!list.contains(filepath)) {
							list.add(filepath);
						}
					}
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 根据key值对文档集进行排序
	 * @param map
	 * @return
	 */
	public static Map<String, ArrayList<String>> sortMapByKey(Map<String, ArrayList<String>> map) {
		TreeMap<String, ArrayList<String>> result = new TreeMap<String, ArrayList<String>>();
		// Map<String, ArrayList<String>> temp = new HashMap<>();
		// 打印排序前的key值
		//System.out.println("打印每个词项以及该词项所在文档号：");
		//for (Map.Entry<String, ArrayList<String>> m : map.entrySet()) {
			//System.out.println(m.getKey() + ":" + m.getValue());
		//}
		Object[] array = map.keySet().toArray();
		Arrays.sort(array);
		// 打印排序后的key值以及对应的value
		for (Object str : array) {
			//System.out.println(str+""+map.get(str));
			// 注意：HashMap：内部数值的顺序并不是以存放的先后顺序为主 ，而是以hash值的顺序为主，其次才是存放的先后顺序
			// temp.put((String)str, map.get((String)str));
			result.put((String) str, map.get(str));
		}
		return result;
	}
}
