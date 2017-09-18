package com.tanyx.poitest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ReadExcel2 {
	
	private static final String FILE_NAME = "D:/route.xls";
	
	private static final String TABLE_NAME = "T_CPS_ROUTE";
	//100条生成一次sql
	private static final Integer SPLIT_ROWS = 100;
	//是否切割并生成sql文件
	private static final boolean SPLIT_FILE = true;
	
	public static void main(String[] args) throws IOException {
		 Date dt = new Date();
		 SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 String dateStr = sdf.format(dt);
		 System.out.println("--当前时间:"+dateStr);
		 //生成时 给属性赋值
		 HashMap<String,String> propertyValueMap = new HashMap<>();
		 propertyValueMap.put("RESERVED", "0");
//		 propertyValueMap.put("ROW_ID", "+2000");
		 //字段为空时插入默认值
		 propertyValueMap.put("REC_UPD_USR", "$init");
		 propertyValueMap.put("ROW_CRT_TS", dateStr);
		 propertyValueMap.put("REC_UPD_TS", dateStr);
		 propertyValueMap.put("REC_CMT_TS", dateStr);
		 
		 //map中存在的字段值都不需要引号 （字段位置，属性名）
		 HashMap<Integer,String> propertyTypeMap = new HashMap<>();
		 propertyTypeMap.put(0, "ROW_ID");
		 propertyTypeMap.put(1, "GROUP_ID");
		 propertyTypeMap.put(2, "USAGE_KEY");
		 propertyTypeMap.put(14, "AMT");
		 
		 //字段索引
		 Map<String, Integer> indexMap = new HashMap<>();
		 
		 FileInputStream excelFile = new FileInputStream(new File(FILE_NAME));
		 Workbook workbook = new HSSFWorkbook(excelFile);
         Sheet datatypeSheet = workbook.getSheetAt(0);
         Iterator<Row> iterator = datatypeSheet.iterator();
         StringBuffer sb = new StringBuffer("");
         //获取SQl property 只获取excel第一行数据
         StringBuffer propertySql = new StringBuffer("(");
         if(iterator.hasNext()) {
        	 Row propertys = iterator.next();
        	 Iterator<Cell> cellIterator = propertys.iterator();
        	 int pIndex = 0;
        	 while (cellIterator.hasNext()) {
            	 Cell currentCell = cellIterator.next();
            	 String value = currentCell.getStringCellValue().trim();
            	 propertySql.append(value);
            	 if(cellIterator.hasNext()) {
            		 propertySql.append(",");
            	 }else {
            		 propertySql.append(") \n values \n");
            	 }
            	 indexMap.put(value, pIndex);
            	 pIndex++;
        	 }
         }
         File sqlFile = null;
         //获取数据
         Integer i = 0;
         while (iterator.hasNext()) {
        	 if(i%SPLIT_ROWS==0) {
        		 //分割文件
        		 if(SPLIT_FILE) {
        			 sb = new StringBuffer("\n--"+i+"到"+(i+SPLIT_ROWS)+"条数据分割: \n");
	        		 String path = "./"+TABLE_NAME+i+"-"+(i+SPLIT_ROWS)+".sql";
	        		 sqlFile = new File(path);
	        		 sqlFile.createNewFile();
        		 }else {
        			 sb.append("\n--"+i+"到"+(i+SPLIT_ROWS)+"条数据分割: \n");
        		 }
            	 sb.append("insert into "+TABLE_NAME+" \n");
            	 sb.append(propertySql);
             }
        	 Row currentRow = iterator.next();
        	 StringBuffer sbValues = new StringBuffer();
             Iterator<Cell> cellIterator = currentRow.iterator();
             Integer j = 0;
             while (cellIterator.hasNext()) {
            	 Iterator<String> proIterator = propertyValueMap.keySet().iterator();
            	 Cell currentCell = cellIterator.next();
            	 String value = currentCell.getStringCellValue().trim();
            	 //根据pkv赋值
            	 value = ReadExcel2.setPropertyValue(propertyValueMap, indexMap, value, j);
            	 //开头括号
        		 if(j==0) {
        			 sbValues.append("(");
        		 }
        		 if(cellIterator.hasNext()) {
        			 if(propertyTypeMap.containsKey(j)) {
        				 sbValues.append(value+",");
        			 }else {
        				 sbValues.append("'"+value+"',");
        			 }
        		 //结尾
            	 }else {
            		 if(propertyTypeMap.containsKey(j)) {
            			 sbValues.append(value+")");
        			 }else {
        				 sbValues.append("'"+value+"')");
        			 }
            	 }
            	 j++;
             }
             i++;
             if(iterator.hasNext() && i%SPLIT_ROWS !=0) {
    			 sbValues.append(",");
        	 }else {
        		 if(SPLIT_FILE && sqlFile!=null) {
        			 try (Writer writer = new BufferedWriter(new OutputStreamWriter(
        		              new FileOutputStream(sqlFile), "utf-8"))) {
        				 writer.write(sb.append(sbValues.toString()+"\n").toString());
        			 }
        		 }
        	 }
             sb.append(sbValues.toString()+"\n");
         }
         System.out.println(sb.toString());
         workbook.close();
	}
	
		public static String setPropertyValue(HashMap<String,String> propertyValueMap
									  		,Map<String, Integer> indexMap
										  	,String value,Integer index) {
	    Iterator<String> proIterator = propertyValueMap.keySet().iterator();
		  //根据pkv赋值
	   	 while(proIterator.hasNext()) {
	   		 String k = proIterator.next();
	   		 if(indexMap.containsKey(k)) {
	   			 if(index.equals(indexMap.get(k))) {
	   				 String pv = propertyValueMap.get(k);
	   				 if(pv.indexOf("+")>-1) {
	   					 value = String.valueOf(Integer.valueOf(value)+Integer.valueOf(pv.substring(pv.indexOf("+")+1)));
	   				 }else if(pv.indexOf("$")>-1) {
	   					 if(value==null||"".equals(value)) {
	   						 value = pv.substring(pv.indexOf("$")+1);
	   					 }
	   				 }else {
	   					 value = propertyValueMap.get(k);
	   				 }
	   			 }
	       	 }
	   	 }
		return value;
	}
	
}