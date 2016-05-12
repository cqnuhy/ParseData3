package com.parse.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class MyThread implements Runnable{

	
	private File xls;
	private int rate;
	private List<String> clcListOfDict;
	private Map<Integer,String> map;
	private List<String> result;
	private Map<String,List<String>> source;

	public MyThread(File xls,int rate,List<String> clcListOfDict,
			 Map<Integer,String> map,Map<String,List<String>> source) {
		this.xls = xls;
		this.rate = rate;
		this.clcListOfDict = clcListOfDict;
		this.map = map;
		this.source = source;
	}

	@SuppressWarnings("resource")
	@Override
	public void run() {
		long start = System.currentTimeMillis();
		try {
			// 通过分词频率和分类号字典找出train中符合要求的分类号
			List<String> clcListOfTrain = new ArrayList<String>();
			HSSFWorkbook book2 = new HSSFWorkbook(new FileInputStream(xls));
			Sheet sheet = book2.getSheetAt(0);
			for (int j =0; j < sheet.getPhysicalNumberOfRows()-1; j++) {
				Row row = sheet.getRow(j);
				String clc = row.getCell(0).getStringCellValue();
				int clcCount = Integer.valueOf(row.getCell(2).getStringCellValue());
				if(clcCount>=rate&&!"".equals(clc.trim())&&clcListOfDict.contains(clc)){
					clcListOfTrain.add(clc);
				}
			}
			
			// 通过train中符合要求的分类号 找出对应句子的索引 以便去source中找出对应的句子
			Map<Integer, Integer> indexList = new HashMap<Integer, Integer>();
			for (Iterator<Entry<Integer, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
				Entry<Integer, String> item = iterator.next();
				for(String c : item.getValue().split(";")){
					if(clcListOfTrain.contains(c)){
						indexList.put(item.getKey(), item.getKey());
						break;
					}
				}
			}
			System.out.println("频率为"+rate+"共有"+indexList.size()+"条句子");
			// 生成过滤后的文件
			for(Entry<String, List<String>> list : source.entrySet()){
				result = new ArrayList<String>();
				String fileName =list.getKey().substring(0, list.getKey().lastIndexOf("."));
				String fileSuffix = list.getKey().substring(list.getKey().lastIndexOf(".")+1,list.getKey().length() );
				int index3 = 1;
				for (String line2: list.getValue()) {
					if(line2.equals("")){continue;}
					if(indexList.containsKey(index3)){
						result.add(line2);
						index3++;
					}else{
						index3++;
						continue;
					}
				}
				String resultFileName = xls.getParent()+"\\"+fileName+"_filter_"+(0==rate?"all":rate);
				// 统一关闭输出流
				File file = new File(resultFileName+"."+fileSuffix);
				if(file.getParentFile() != null){
					file.getParentFile().mkdirs();
				}
				OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file, true));
				BufferedWriter bw = new BufferedWriter(write);
				try {
					for (String r : result) {
//						CommonUtil.writeFile(resultFileName, r+"\r\n",fileSuffix, true);
				        bw.write(r+"\r\n");
				        bw.flush(); 
					}
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					if(null!=write){
						write.close();
					}
					if(null!=bw){
						bw.close();
					}
					
				}
//				for (String r : result) {
//					CommonUtil.writeFile(resultFileName, r+"\r\n",fileSuffix, true);
//				}
			}
		
			System.out.println("保存词频为"+rate+"共"+indexList.size()+"条句子共耗时："+(System.currentTimeMillis()-start)/(1000)+"秒");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
