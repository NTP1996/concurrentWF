package run;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import operationXML.ParseSWFXML;
import parallelzation.graph.SWFBuildGraph;
import parallelzation.graph.SWFGraph;
import parallelzation.graph.SWFNewGraph;
import parallelzation.node.SWFControlNode;
import util.OpeartionExcel;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * @author kuangzengxiong
 * @date 2019/5/23 21:39
 */
public class TestCollectionToJinTao implements Initializable {
    @FXML
    private TextField openDir;
    @FXML
    private TextField outputDir;
    @FXML
    private TextField outputData;

    @Override
    public void initialize(URL location, ResourceBundle resources){

    }



    public void selectOpen(ActionEvent event){
        FileChooser fileChooser=new FileChooser();
        fileChooser.setTitle("打开文件目录");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(null);
        if(file==null){
            return;
        }
        String srcPath=file.getPath();
        openDir.setText(srcPath);
    }

    public void selectOut(ActionEvent event){
        FileChooser fileChooser=new FileChooser();
        fileChooser.setTitle("选择结果保存路径");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(null);
        if(file==null){
            return;
        }
        String srcPath=file.getPath();
        outputDir.setText(srcPath);
    }

    public void selectData(ActionEvent event){

        FileChooser fileChooser=new FileChooser();
        fileChooser.setTitle("保存工作流文件");
        fileChooser.getExtensionFilters().addAll( new FileChooser.ExtensionFilter("XLS","*.xls"));
        File file=fileChooser.showSaveDialog(null);
        if(file==null){
            return;
        }
        String srcPath=file.getPath();
        outputData.setText(srcPath);
        System.out.println(file);

    }

    public void executeTest(ActionEvent event){
        String srcPath=openDir.getText();
        String resultPath=outputDir.getText();
        String dataPath=outputData.getText();
        if(srcPath.equals("")||dataPath.equals("")){
            return;
        }

        File file = new File(srcPath);
        if(file==null){
            return;
        }


        if(file.isDirectory()){

            for(int i=0;i<15;i++){
                OpeartionExcel opeartionExcel=new OpeartionExcel(dataPath);
                opeartionExcel.clearFile();

                //列出文件夹下所有文件
                String[] fileList=file.list();
                for(String childrenFile:fileList){
                    String[] splitResult=childrenFile.split("\\.");
                    //如果是xml文件
                    if(splitResult.length>0 && splitResult[splitResult.length-1].equals("xml")){
                        System.out.println();
                        try{

                            SWFGraph swfGraph= ParseSWFXML.getSWF(srcPath+"\\"+childrenFile);
                            Long startTime=System.nanoTime();

                            SWFBuildGraph swfBuildGraph=new SWFBuildGraph(swfGraph);
                            SWFNewGraph swfNewGraph=swfBuildGraph.parallelizePetri();
                            int controledgeSize=swfBuildGraph.getRawControlEdgeSize();

                            Long endTime=System.nanoTime();
                            Long timeDifference=(endTime-startTime)/1000;
                            System.out.println(timeDifference);

                            ArrayList<String> dataList=new ArrayList<>();
                            int taskSize=swfNewGraph.getTaskNodeList().size();
                            int paraSize=(int)swfNewGraph.getParallelRelations().zSum()/2;

                            dataList.add(swfNewGraph.getCaseId());
                            dataList.add(String.valueOf(taskSize));
                            dataList.add(String.valueOf(swfNewGraph.getDataNodeList().size()));
                            dataList.add(String.valueOf(controledgeSize));
                            dataList.add(String.valueOf(swfNewGraph.getDataEdgeList().size()));
                            dataList.add(String.valueOf(paraSize));
                            dataList.add(String.valueOf(timeDifference));


                            int parallezation=0;
                            for(SWFControlNode node:swfNewGraph.getSplitControlList()){
                                parallezation+=node.getSuccessorList().size()-1;
                            }
                            dataList.add(String.valueOf(parallezation));
                            dataList.add(String.valueOf((double) paraSize/(taskSize*(taskSize-1)/2)));

                            opeartionExcel.writeExcel(dataList);


                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }
                System.out.println("目录文件并行化结束!");
            }


            Stage stage = (Stage) openDir.getScene().getWindow();
            stage.close();
        }
    }
}
