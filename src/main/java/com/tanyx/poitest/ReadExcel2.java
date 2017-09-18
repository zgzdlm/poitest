package com.tanyx.poitest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
         StringBuffer sb = new StringBuffer("insert into "+TABLE_NAME+" \n");
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
         sb.append(propertySql);
         //获取数据
         Integer i = 0;
         while (iterator.hasNext()) {
        	 if(i!=0&&i%SPLIT_ROWS==0) {
        		 sb.append("\n--第"+i+"条数据分割: \n");
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
            	 while(proIterator.hasNext()) {
            		 String k = proIterator.next();
            		 if(indexMap.containsKey(k)) {
            			 if(j.equals(indexMap.get(k))) {
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
        		 if(j==0) {
        			 sbValues.append("(");
        		 }
        		 if(cellIterator.hasNext()) {
        			 if(propertyTypeMap.containsKey(j)) {
        				 sbValues.append(value+",");
        			 }else {
        				 sbValues.append("'"+value+"',");
        			 }
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
        	 }
             sb.append(sbValues.toString()+"\n");
         }
         System.out.println(sb.toString());
         workbook.close();
	}
	
}