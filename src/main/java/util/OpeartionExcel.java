package util;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * @author kuangzengxiong
 * @date 2019/5/19 11:35
 */
public class OpeartionExcel {
    private String fileName="src\\main\\java\\test.xls";


    public OpeartionExcel(){}
    public OpeartionExcel(String fileName){
        this.fileName=fileName;
    }

    public void clearFile(){
        File file=new File(fileName);
        if(file.exists()&&file.isFile()){
            file.delete();
        }
    }

    public void writeExcel(ArrayList<String> dataList){
        try{

            File file=new File(fileName);
            if(!file.exists()){
                HSSFWorkbook workbook=new HSSFWorkbook();
                workbook.createSheet();
                HSSFSheet sheet=workbook.getSheetAt(0);
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("工作流编号");
                row.createCell(1).setCellValue("任务节点个数");
                row.createCell(2).setCellValue("数据节点个数");
                row.createCell(3).setCellValue("控制流边数");
                row.createCell(4).setCellValue("数据流边数");
                row.createCell(5).setCellValue("并行节点对数");
                row.createCell(6).setCellValue("并行时间(us)");
                row.createCell(7).setCellValue("TS1并行度");
                row.createCell(8).setCellValue("TS2并行度");
                FileOutputStream outputStream=new FileOutputStream(fileName);
                outputStream.flush();
                workbook.write(outputStream);
                outputStream.close();
            }
            HSSFWorkbook workbook=new HSSFWorkbook(new FileInputStream(fileName));
            HSSFSheet sheet=workbook.getSheetAt(0);
            int rowNumber=sheet.getLastRowNum();


            Row row=sheet.createRow(rowNumber+1);

            row.createCell(0).setCellValue(dataList.get(0));
            row.createCell(1).setCellValue(Integer.parseInt(dataList.get(1)) );
            row.createCell(2).setCellValue(Integer.parseInt(dataList.get(2)));
            row.createCell(3).setCellValue(Integer.parseInt(dataList.get(3)));
            row.createCell(4).setCellValue(Integer.parseInt(dataList.get(4)));
            row.createCell(5).setCellValue(Integer.parseInt(dataList.get(5)));
            row.createCell(6).setCellValue(Integer.parseInt(dataList.get(6)));
            row.createCell(7).setCellValue(Integer.parseInt(dataList.get(7)));
            row.createCell(8).setCellValue(Double.parseDouble(dataList.get(8)));

            FileOutputStream outputStream=new FileOutputStream(fileName);
            outputStream.flush();
            workbook.write(outputStream);
            outputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

//    public void writeExcel(ArrayList<ArrayList<String>> dataList){
//        try{
//
//            File file=new File(fileName);
//            if(!file.exists()){
//                HSSFWorkbook workbook=new HSSFWorkbook();
//                workbook.createSheet();
//                HSSFSheet sheet=workbook.getSheetAt(0);
//                Row row = sheet.createRow(0);
//                row.createCell(0).setCellValue("工作流编号");
//                row.createCell(1).setCellValue("任务节点个数");
//                row.createCell(2).setCellValue("数据节点个数");
//                row.createCell(3).setCellValue("控制流边数");
//                row.createCell(4).setCellValue("数据流边数");
//                row.createCell(5).setCellValue("并行节点对数");
//                row.createCell(6).setCellValue("并行时间");
//                FileOutputStream outputStream=new FileOutputStream(fileName);
//                outputStream.flush();
//                workbook.write(outputStream);
//                outputStream.close();
//            }
//            HSSFWorkbook workbook=new HSSFWorkbook(new FileInputStream(fileName));
//            HSSFSheet sheet=workbook.getSheetAt(0);
//            int rowNumber=sheet.getLastRowNum();
//
//            for(int i=0;i<dataList.size();i++){
//                Row row=sheet.createRow(rowNumber+i+1);
//                ArrayList<String> rowData=dataList.get(i);
//                row.createCell(0).setCellValue(rowData.get(0));
//                row.createCell(1).setCellValue(rowData.get(1));
//                row.createCell(2).setCellValue(rowData.get(2));
//                row.createCell(3).setCellValue(rowData.get(3));
//                row.createCell(4).setCellValue(rowData.get(4));
//                row.createCell(5).setCellValue(rowData.get(5));
//                row.createCell(6).setCellValue(rowData.get(6));
//            }
//            FileOutputStream outputStream=new FileOutputStream(fileName);
//            outputStream.flush();
//            workbook.write(outputStream);
//            outputStream.close();
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//    }
}
