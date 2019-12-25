package run;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import operationXML.ParseSWFXML;
import operationXML.WriteXML;
import parallelzation.edge.SWFControlFlowEdge;
import parallelzation.edge.SWFEdge;
import parallelzation.graph.SWFBuildGraph;
import parallelzation.graph.SWFGraph;
import parallelzation.graph.SWFNewGraph;
import parallelzation.node.SWFFlowNode;
import parallelzation.node.SWFNode;
import randomSWF.CreateSWF;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * @author kuangzengxiong
 * @date 2019/5/18 16:24
 */
public class CreateRandom implements Initializable {

    @FXML
    private TextField minNum;
    @FXML
    private TextField maxNum;
    @FXML
    private TextField swfNumber;
    @FXML
    private TextField directoryName;


    public void selectSrc(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开模型文件");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(null);
        if (file == null) {
            return;
        }
        String srcPath = file.getPath();
        directoryName.setText(srcPath);
    }

    public void createStart(ActionEvent event) {
        int min = 0;
        int max = 0;
        int number = 0;
        try {
            min = Integer.parseInt(minNum.getText());
            max = Integer.parseInt(maxNum.getText());
            number = Integer.parseInt(swfNumber.getText());
        } catch (Exception e) {
            return;
        }

        String fileName = directoryName.getText();
        if (fileName.equals("")) {
            return;
        }


        CreateSWF createSWF = new CreateSWF(min, max);

        for (int i = 0; i < number; ) {
            SWFNewGraph newGraph = createSWF.execute();

            ArrayList<SWFNode> nodeList = new ArrayList<>();
            ArrayList<SWFEdge> edgeList = new ArrayList<>();

            nodeList.addAll(newGraph.getTaskNodeList());
            nodeList.addAll(newGraph.getDataNodeList());
            nodeList.add(newGraph.getStartNode());
            nodeList.add(newGraph.getEndNode());
            edgeList.addAll(newGraph.getDataEdgeList());
            edgeList.addAll(newGraph.getSwfControlFlowEdgeList());


            for (SWFControlFlowEdge edge : newGraph.getSwfControlFlowEdgeList()) {
                SWFFlowNode left = (SWFFlowNode) edge.getLeftNode();
                SWFFlowNode right = (SWFFlowNode) edge.getRightNode();
                left.addSuccessorNode(right);
                right.addPrecursorNode(left);
            }

            WriteXML writeXML = new WriteXML(newGraph);
            System.out.println(fileName);
            String saveFileName = fileName + "\\randomSWF" + i + ".xml";
            writeXML.setFileName(saveFileName);
            writeXML.write();

            if(detectionSWF(saveFileName)){
                i++;
            }
        }


        Stage stage = (Stage) swfNumber.getScene().getWindow();
        stage.close();
    }


    //检测生成的工作流图是否合理
    private boolean detectionSWF(String fileName) {


        try {
            SWFGraph swfGraph = ParseSWFXML.getSWF(fileName);
            SWFBuildGraph swfBuildGraph = new SWFBuildGraph(swfGraph);
            SWFNewGraph swfNewGraph = swfBuildGraph.parallelize();

            if (swfNewGraph.getSplitControlList().size() != swfNewGraph.getJoinControlList().size()) {
                File deleteFile = new File(fileName);
                if (deleteFile.delete()) {
                    System.out.println("删除成功");
                }
                return false;
            }
        } catch (Exception e) {
            System.out.println(fileName);
            File deleteFile = new File(fileName);

            if (deleteFile.delete()) {
                System.out.println("删除成功");
            }
            return false;
        }
        return true;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        minNum.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    minNum.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        maxNum.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    minNum.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        swfNumber.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    minNum.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

    }


}
