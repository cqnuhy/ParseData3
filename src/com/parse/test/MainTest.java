package com.parse.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
* Copyright (c) 2016,HUYI<br>
* All rights reserved.<br>
* 
* 文件名称：MainTest.java<br>
* 摘要：用于词汇扩展（递归概念查询），标引分类号<br>
* -------------------------------------------------------<br>
* 当前版本：1.1.1<br>
* 作者：HUYI<br>
* 完成日期：2016-3-20<br>
* -------------------------------------------------------<br>
* 取代版本：1.1.0<br>
* 原作者：HUYI<br>
* 完成日期：2016-3-20<br>
 */
public class MainTest {
	
	private static final String suffix = "ClcNumbers："; 
	private static final Integer[] RATES = {0,100, 200, 400, 600, 800, 1000};	
	/**
	 * @date 2016-3-15 上午12:02:51
	 * @author HUYI
	 * @param args
	 * @throws IOException 
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		System.out.println("过滤中...");
		Map<String,List<String>> source = new HashMap<String,List<String>>();
		Map<Integer,String> map = new HashMap<Integer,String>();
		List<String> clcListOfDict = new ArrayList<String>();
		
		File dev_zh_xls = new File("E:\\corpus\\dev.zh.统计表.xls");
		File test_zh_xls = new File("E:\\corpus\\test.zh.统计表.xls");
		File train_zh_xls = new File("E:\\corpus\\train.zh.统计表.xls");
		File train_zh_txt = new File("E:\\corpus\\train.zh.txt");
		File train_zh = new File("E:\\corpus\\train.zh");
		File train_en = new File("E:\\corpus\\train.en");
		
		// 从train.zh.txt中获取句子的分类号和索引 for Cache，以便通过索引在train.zh，train.en 原始文件中找到对应的句子（他们的索引是一致的）
		BufferedReader br = CommonUtil.getFileBuffer(train_zh_txt.getAbsolutePath());
		String line = null;
		int index = 1;
		while ((line = br.readLine()) != null) {
			if(line.equals("")){continue;}
			String clc = line.substring(line.indexOf(suffix)+11, line.length()-1);
			map.put(index,  clc);
			index++;
		}
		// 获取train.zh ,train.en 中的内容 for Cache，用于检索句子
		File[] files2 = new File[]{train_zh,train_en};
		for(File f : files2){
			List<String> tmp = new ArrayList<String>();
			String fileName = f.getName().substring(0, f.getName().lastIndexOf("."));
			String fileSuffix = f.getName().substring(f.getName().lastIndexOf(".")+1,f.getName().length() );
			BufferedReader br2 = CommonUtil.getFileBuffer(f.getAbsolutePath());
			String line2 = null;
			while ((line2 = br2.readLine()) != null) {
				if(line2.equals("")){continue;}
				tmp.add(line2);
			}
			source.put(fileName+"."+fileSuffix, tmp);
		}
		
		// 从 dev ,test 统计表中获取分类号的并集做分类号字典
		File[] files = new File[]{test_zh_xls,dev_zh_xls,};
		for (int i = 0; i < files.length; i++) {
			HSSFWorkbook book = new HSSFWorkbook(new FileInputStream(files[i]));
			Sheet sheet = book.getSheetAt(0);
			for (int j =1; j < sheet.getPhysicalNumberOfRows(); j++) {
				Row row = sheet.getRow(j);
				String clc= row.getCell(0).getStringCellValue();
				if(!clcListOfDict.contains(clc)){
					clcListOfDict.add(clc);
				}
			}
		}
		
		for(Integer rate : RATES){
			new Thread(new MyThread(train_zh_xls, rate, clcListOfDict,map, source)).start();
		}
		
	}
}
