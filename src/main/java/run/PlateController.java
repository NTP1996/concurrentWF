package run;



import UIModule.*;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import operationXML.ParseSWFXML;
import operationXML.WriteXML;
import parallelzation.edge.SWFControlFlowEdge;
import parallelzation.edge.SWFDataFlowEdge;
import parallelzation.edge.SWFEdge;
import parallelzation.graph.BaseGraph;
import parallelzation.graph.SWFBuildGraph;
import parallelzation.graph.SWFGraph;
import parallelzation.graph.SWFNewGraph;
import parallelzation.node.*;
import util.ControlNodeType;
import util.MyUUID;
import util.NodeType;
import util.TaskDataRelation;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class PlateController implements Initializable {
    @FXML
    private MenuItem openFile;
    @FXML
    private MenuItem saveFile;
    @FXML
    private MenuItem executeParallelize;
    @FXML
    private TableView dataTable;

    @FXML
    private AnchorPane showGraph;
    @FXML
    private ChoiceBox branchChoice;
    @FXML
    private ChoiceBox sourceChoice1;
    @FXML
    private ChoiceBox sourceChoice2;
    @FXML
    private ChoiceBox sourceLocation;
    @FXML
    private Button sourceExecute;
    @FXML
    private Label paraResult;
    @FXML
    private Label paraLable1;
    @FXML
    private Label paraLable2;
    //输入的工作流图
    private SWFGraph swfGraph;
    private SWFNewGraph swfNewGraph;
    //每个对分支上所有节点集合
    private HashMap<String, ArrayList<ArrayList<SWFFlowNode>>> branchMap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        branchChoice.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println(newValue);
                selectBranch();
            }
        });

        saveFile.setDisable(true);
        executeParallelize.setDisable(true);
    }


    /**********************************************************
     * **********************打开xml文件，读取语义工作流***********
     **********************************************************/
    public void openFilePath(ActionEvent event) {
        Stage fileSelect = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开模型文件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML", "*.xml"),
                new FileChooser.ExtensionFilter("ALL FILE", "*.*")
        );
        File filePath = fileChooser.showOpenDialog(fileSelect);
        System.out.println(filePath);
        if (filePath == null) {
            return;
        }

        //获取当前类路径
        String resourcePath = filePath.getPath();
        System.out.println(resourcePath);

        try {
            swfGraph = ParseSWFXML.getSWF(resourcePath);
            SWFGraph swf1 = ParseSWFXML.getSWF(resourcePath);
            paraLable1.setText("The degree of parallelism TS");
            paraLable2.setText("2");


            //清空图形界面
            showGraph.getChildren().removeAll(showGraph.getChildren());
            //构建工作流图
            buildSWFGUI(swf1);

            //获取工作流图的并行度
            double pResult=getNumberOfParallelNodePair(swf1);
            paraResult.setText("="+String.format("%.2f",pResult));
        } catch (Exception e) {
            e.printStackTrace();
        }

        executeParallelize.setDisable(false);
    }

    /**********************************************************
     * **********************保存文件****************************
     **********************************************************/
    public void saveFilePath(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存工作流文件");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML", "*.xml"));
        File file = fileChooser.showSaveDialog(null);
        if (file == null) {
            return;
        }
        WriteXML writeXML = new WriteXML(swfNewGraph);
        writeXML.setFileName(file.getPath());
        writeXML.write();
    }


    /*************************************************************************
     * **********************打开目录，批量测试本文方法****************************
     *************************************************************************/
    public void openDirectory(ActionEvent event) throws Exception {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("testCollection.fxml"));
        stage.setTitle("SWPR批量测试");
        InputStream iconFile = getClass().getResourceAsStream("/高并发.png");
        stage.getIcons().add(new Image(iconFile));
        stage.setScene(new Scene(root));
        stage.show();
    }

    /*************************************************************************
     ***********************打开目录，批量测试改进后Jin方法****************************
     *************************************************************************/
    public void testDirJinTao(ActionEvent event) throws Exception {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("TestCollectionToJinTao.fxml"));
        stage.setTitle("PNPR批量测试");
        InputStream iconFile = getClass().getResourceAsStream("/高并发.png");
        stage.getIcons().add(new Image(iconFile));
        stage.setScene(new Scene(root));
        stage.show();
    }

    ///////////////////////////随机生成工作流图/////////////////////////////////////////////////
    public void createGraph(ActionEvent event) throws Exception {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("createRandom.fxml"));
        stage.setTitle("生成工作流");
        InputStream iconFile = getClass().getResourceAsStream("/高并发.png");
        stage.getIcons().add(new Image(iconFile));
        stage.setScene(new Scene(root));
        stage.show();
    }




    //////////////////////////////获取并行度//////////////////////////
    private double getNumberOfParallelNodePair(BaseGraph swf) {
        ArrayList<SWFControlNode> splitControlNodeList = null;
        ArrayList<SWFControlNode> joinControlNodeList = null;
        ArrayList<SWFTaskNode> taskNodeList = null;

        if (swf instanceof SWFGraph) {
            splitControlNodeList = new ArrayList<>();
            joinControlNodeList = new ArrayList<>();
            taskNodeList = new ArrayList<>();

            for (SWFNode node : swf.getNodeList()) {
                if (node instanceof SWFTaskNode) {
                    taskNodeList.add((SWFTaskNode) node);
                } else if (node instanceof SWFControlNode) {
                    if (((SWFControlNode) node).getPairType().equals(ControlNodeType.AND)) {
                        if (((SWFControlNode) node).getOneType().equals(ControlNodeType.SPLIT)) {
                            splitControlNodeList.add((SWFControlNode) node);
                        } else if (((SWFControlNode) node).getOneType().equals(ControlNodeType.JOIN)) {
                            joinControlNodeList.add((SWFControlNode) node);
                        }
                    }
                }
            }

        } else if (swf instanceof SWFNewGraph) {
            splitControlNodeList = ((SWFNewGraph) swf).getSplitControlList();
            joinControlNodeList = ((SWFNewGraph) swf).getJoinControlList();
            taskNodeList = ((SWFNewGraph) swf).getTaskNodeList();
        } else {
            return 0;
        }


        HashMap<String, SWFControlNode> joinNodeMap = new HashMap<>();
        for (SWFControlNode node : joinControlNodeList) {
            joinNodeMap.put(node.getNodeDescp(), node);
        }

        int taskSize = taskNodeList.size();
        DoubleMatrix2D paraRelations = DoubleFactory2D.sparse.make(taskSize, taskSize, 0);


        //寻找并行关系
        for (SWFControlNode node : splitControlNodeList) {
            ArrayList<SWFFlowNode> successNodeList = node.getSuccessorList();
            //获取对应的join控制流节点
            String nodeDesc = node.getNodeDescp();
            SWFControlNode joinNode = joinNodeMap.get(nodeDesc);
            //设置集合
            ArrayList<ArrayList<SWFFlowNode>> pathNodeLists = new ArrayList<>();
            for (int i = 0; i < successNodeList.size(); i++) {
                pathNodeLists.add(new ArrayList<>());
            }

            //获取各路径上的节点
            for (int i = 0; i < successNodeList.size(); i++) {
                Stack<SWFFlowNode> nodeStack = new Stack<>();
                nodeStack.push(successNodeList.get(i));
                while (!nodeStack.isEmpty()) {
                    SWFFlowNode nowNode = nodeStack.pop();
                    if (nowNode.equals(joinNode)) {//如果遇到join节点，则直接进入下一次循环
                        continue;
                    }
                    if (!pathNodeLists.get(i).contains(nowNode)) {
                        pathNodeLists.get(i).add(nowNode);
                        for (SWFFlowNode n : nowNode.getSuccessorList()) {
                            nodeStack.push(n);
                        }
                    }
                }
            }

            //将每一个条路径上的节点与另一条路径上的节点设置为并行关系
            for (int i = 0; i < successNodeList.size() - 1; i++) {
                ArrayList<SWFFlowNode> list1 = pathNodeLists.get(i);
                for (int j = i + 1; j < successNodeList.size(); j++) {
                    ArrayList<SWFFlowNode> list2 = pathNodeLists.get(j);
                    for (int k = 0; k < list1.size(); k++) {
                        int index1 = taskNodeList.indexOf(list1.get(k));
                        for (int l = 0; l < list2.size(); l++) {
                            int index2 = taskNodeList.indexOf(list2.get(l));
                            if(index1>=0 && index2>=0){
                                paraRelations.set(index1, index2, 1);
                                paraRelations.set(index2, index1, 1);
                            }
                        }
                    }
                }
            }


        }

        return paraRelations.zSum()/2/(taskSize*(taskSize-1)/2);
    }


    /**********************************************************
     * **********************并行化重构****************************
     **********************************************************/
    public void parallelization(ActionEvent event) {
        System.out.println("开始并行化");
        SWFBuildGraph swfBuildGraph = new SWFBuildGraph(swfGraph);
        swfNewGraph = swfBuildGraph.parallelize();

        int taskSize = swfNewGraph.getTaskNodeList().size();
        int paraSize = (int) swfNewGraph.getParallelRelations().zSum() / 2;
        double parallResult = (double) paraSize / (taskSize * (taskSize - 1) / 2);
        paraResult.setText("=" + String.format("%.2f", parallResult));

        saveFile.setDisable(false);
        executeParallelize.setDisable(true);

        ArrayList<SWFControlNode> splitControlList = swfNewGraph.getSplitControlList();
        ArrayList<SWFControlNode> joinControlList = swfNewGraph.getJoinControlList();
        SWFStartNode startNode = swfNewGraph.getStartNode();
        SWFEndNode endNode = swfNewGraph.getEndNode();
        ArrayList<SWFDataNode> dataNodeList = swfNewGraph.getDataNodeList();
        ArrayList<SWFTaskNode> taskNodeList = swfNewGraph.getTaskNodeList();
        ArrayList<SWFControlFlowEdge> controlEdgeList = swfNewGraph.getSwfControlFlowEdgeList();
        ArrayList<SWFDataFlowEdge> dataEdgeList = swfNewGraph.getDataEdgeList();

        boolean resultReal = interactionSource(splitControlList, joinControlList, startNode);
        if (resultReal == false) {
            showGraph.getChildren().removeAll(showGraph.getChildren());
            return;
        }

        //去掉边的连接关系
        LinkedHashSet<SWFFlowNode> set = new LinkedHashSet<>();
        Queue<SWFFlowNode> queue = new LinkedList<>();
        queue.add(startNode);
        set.add(startNode);
        while (!queue.isEmpty()) {
            SWFFlowNode swfFlowNode = queue.poll();
            swfFlowNode.removeAllPrecursorNode(swfFlowNode.getPrecursorList());
            for (SWFFlowNode node : swfFlowNode.getSuccessorList()) {
                if (!set.contains(node)) {
                    queue.add(node);
                    set.add(node);
                }
            }
            swfFlowNode.removeAllSuccessorNode(swfFlowNode.getSuccessorList());
        }

        ArrayList<SWFNode> nodeList = new ArrayList<>();
        ArrayList<SWFEdge> edgeList = new ArrayList<>();
        nodeList.add(startNode);
        nodeList.add(endNode);
        nodeList.addAll(dataNodeList);
        nodeList.addAll(taskNodeList);
        nodeList.addAll(splitControlList);
        nodeList.addAll(joinControlList);
        edgeList.addAll(controlEdgeList);
        edgeList.addAll(dataEdgeList);

        SWFGraph swfGraph = new SWFGraph(nodeList, edgeList);

        showGraph.getChildren().removeAll(showGraph.getChildren());
        buildSWFGUI(swfGraph);
    }


    //////////////////////////////////////////////////设置界面数据，可视化语义工作流/////////////////////////////////////////////////////////
    private void buildSWFGUI(SWFGraph swf) {

        ArrayList<SWFControlFlowEdge> controlEdgeList = new ArrayList<>();
        SWFStartNode startNode = null;
        SWFEndNode endNode;
        ArrayList<SWFDataNode> dataList = new ArrayList<>();
        ArrayList<SWFTaskNode> taskList = new ArrayList<>();
        ArrayList<SWFControlNode> controlList = new ArrayList<>();

        for (SWFNode node : swf.getNodeList()) {
            if (node instanceof SWFStartNode) {
                startNode = (SWFStartNode) node;
            } else if (node instanceof SWFEndNode) {
                endNode = (SWFEndNode) node;
            } else if (node instanceof SWFDataNode) {
                dataList.add((SWFDataNode) node);
            } else if (node instanceof SWFTaskNode) {
                taskList.add((SWFTaskNode) node);
            } else if (node instanceof SWFControlNode) {
                controlList.add((SWFControlNode) node);
            }
        }

        for (SWFEdge edge : swf.getEdgeList()) {
            if (edge instanceof SWFControlFlowEdge) {
                controlEdgeList.add((SWFControlFlowEdge) edge);
            }
        }


        /////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////              显示数据依赖关系                         //////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////
        dataTable.getVisibleLeafColumn(0).setCellValueFactory(new PropertyValueFactory<>("taskName"));
        dataTable.getVisibleLeafColumn(1).setCellValueFactory(new PropertyValueFactory<>("inputDataName"));
        dataTable.getVisibleLeafColumn(2).setCellValueFactory(new PropertyValueFactory<>("outputDataName"));

        ObservableList<TaskDataRelation> data = FXCollections.observableArrayList();
        for (SWFTaskNode node : taskList) {
            String taskName = node.getNodeDescp();
            ArrayList<SWFDataNode> inputList = node.getInputNodeList();
            ArrayList<SWFDataNode> outputList = node.getOutputNodeList();

            int length = inputList.size() > outputList.size() ? inputList.size() : outputList.size();

            for (int i = 0; i < length; i++) {
                String inputName = i < inputList.size() ? inputList.get(i).getNodeDescp() : "";
                String outputName = i < outputList.size() ? outputList.get(i).getNodeDescp() : "";

                if (i == 0) {
                    data.add(new TaskDataRelation(taskName, inputName, outputName));
                } else {
                    data.add(new TaskDataRelation("", inputName, outputName));
                }
            }


        }
        dataTable.setItems(data);


        /////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////                     构造图形                         //////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////
        for (SWFControlFlowEdge edge : controlEdgeList) {
            SWFFlowNode leftNode = (SWFFlowNode) edge.getLeftNode();
            SWFFlowNode rigthNode = (SWFFlowNode) edge.getRightNode();
            leftNode.addSuccessorNode(rigthNode);
            rigthNode.addPrecursorNode(leftNode);
        }

        HashMap<String, SWFControlNode> joinMap = new HashMap<>();
        for (SWFControlNode node : controlList) {
            if (node.getOneType().equals(ControlNodeType.JOIN)) {
                joinMap.put(node.getNodeDescp(), node);
            }
        }


        //设置节点级别，用来判断循环节点的分支，
        HashMap<SWFFlowNode, Integer> nodeLevel = new HashMap<>();
        Stack<SWFFlowNode> nodeStack = new Stack<>();
        nodeStack.push(startNode.getSuccessorList().get(0));
        nodeLevel.put(startNode.getSuccessorList().get(0), 0);
        while (!nodeStack.isEmpty()) {
            //出栈
            SWFFlowNode nowNode = nodeStack.pop();
            //获取节点级别
            int level = nodeLevel.get(nowNode);
            //获取后继节点集合
            ArrayList<SWFFlowNode> successorNodeList = nowNode.getSuccessorList();
            for (SWFFlowNode node : successorNodeList) {
                if (node instanceof SWFEndNode) {
                    continue;
                }
                if (nodeLevel.getOrDefault(node, -1) == -1) {//如果该节点没有设置级别
                    nodeLevel.put(node, level + 1);
                    nodeStack.push(node);
                }
            }
        }

        double height = showGraph.getHeight();

        //开始节点
        double x = 10;
        double y = height / 5 * 2;

        if (startNode != null) {
            buildDrawGraph(startNode, x, y, 150, joinMap, nodeLevel);
        }
    }


    ////////////////////////////////////////////////画图构建图形界面///////////////////////////////////////////////////////////////////
    private double[] buildDrawGraph(SWFFlowNode node, double x, double y, double height,
                                    HashMap<String, SWFControlNode> joinMap, HashMap<SWFFlowNode, Integer> nodeLevel) {

        if (node instanceof SWFStartNode) {//如果是开始节点
            CircleDraw startDraw = new CircleDraw(x, y);
            showGraph.getChildren().add(startDraw);
            double[] result = startDraw.getRight();
            result = buildDrawGraph(node.getSuccessorList().get(0), result[0], result[1], height, joinMap, nodeLevel);
            return result;
        } else if (node instanceof SWFTaskNode) {//如果是任务节点
            HArrows HArrows = new HArrows(x, y, 50);
            showGraph.getChildren().add(HArrows);
            double[] result = HArrows.getEnd();

            TaskDraw taskDraw = new TaskDraw(result[0], result[1]);
            taskDraw.setText(node.getNodeDescp());
            showGraph.getChildren().add(taskDraw);
            result = taskDraw.getRight();
            result = buildDrawGraph(node.getSuccessorList().get(0), result[0], result[1], height, joinMap, nodeLevel);
            return result;
        } else if (node instanceof SWFControlNode) {
            if (((SWFControlNode) node).getPairType().equals(ControlNodeType.AND)) {//如果是and节点
                if (((SWFControlNode) node).getOneType().equals(ControlNodeType.SPLIT)) {
                    //箭头线
                    HArrows HArrows = new HArrows(x, y, 50);
                    showGraph.getChildren().add(HArrows);

                    //控制流节点
                    double[] result = HArrows.getEnd();
                    ControlDraw controlDraw = new ControlDraw(result[0], result[1]);
                    controlDraw.setText("+");
                    showGraph.getChildren().add(controlDraw);

                    int size = node.getSuccessorList().size();
                    if (size == 2) {
                        //上下画线
                        double[] toopCoord = controlDraw.getTop();
                        double[] middleCoord = controlDraw.getRight();
                        double[] bottomCoord = controlDraw.getBottom();

                        VLine vLineTop = new VLine(toopCoord[0], toopCoord[1], -height);
                        showGraph.getChildren().add(vLineTop);
                        VLine vLineBottom = new VLine(bottomCoord[0], bottomCoord[1], height);
                        showGraph.getChildren().add(vLineBottom);

                        //递归构建分支图
                        toopCoord = vLineTop.getEnd();
                        bottomCoord = vLineBottom.getEnd();
                        toopCoord = buildDrawGraph(node.getSuccessorList().get(0), toopCoord[0], toopCoord[1], height / 2, joinMap, nodeLevel);
                        bottomCoord = buildDrawGraph(node.getSuccessorList().get(1), bottomCoord[0], bottomCoord[1], height / 2, joinMap, nodeLevel);

                        //构建join节点闭合
                        SWFControlNode joinNode = joinMap.get(node.getNodeDescp());

                        double x1 = toopCoord[0];
                        double y1 = toopCoord[1];
                        double x2 = bottomCoord[0];
                        double y2 = bottomCoord[1];

                        double drawX = x1 > x2 ? x1 + 50 : x2 + 50;
                        HLine topLine = new HLine(x1, y1, drawX - x1);
                        showGraph.getChildren().add(topLine);
                        toopCoord = topLine.getEnd();

                        HLine bottomLine = new HLine(x2, y2, drawX - x2);
                        showGraph.getChildren().add(bottomLine);
                        bottomCoord = bottomLine.getEnd();

                        x1 = toopCoord[0];
                        y1 = toopCoord[1];
                        x2 = bottomCoord[0];
                        y2 = bottomCoord[1];

                        VArrows topArrows = new VArrows(x1, y1, height);
                        showGraph.getChildren().add(topArrows);
                        VArrows bottomArrows = new VArrows(x2, y2, -height);
                        showGraph.getChildren().add(bottomArrows);

                        ControlDraw joinDraw = new ControlDraw(x1 - ControlDraw.getLength(), middleCoord[1]);
                        joinDraw.setText("+");
                        showGraph.getChildren().add(joinDraw);

                        result = joinDraw.getRight();
                        result = buildDrawGraph(joinNode.getSuccessorList().get(0), result[0], result[1], height, joinMap, nodeLevel);
                        return result;
                    } else if (size == 3) {
                        //上中下画线
                        double[] toopCoord = controlDraw.getTop();
                        double[] middleCoord = controlDraw.getRight();
                        double[] bottomCoord = controlDraw.getBottom();

                        VLine vLineTop = new VLine(toopCoord[0], toopCoord[1], -height);
                        showGraph.getChildren().add(vLineTop);
                        VLine vLineBottom = new VLine(bottomCoord[0], bottomCoord[1], height);
                        showGraph.getChildren().add(vLineBottom);

                        //递归构建分支图
                        toopCoord = vLineTop.getEnd();
                        bottomCoord = vLineBottom.getEnd();
                        toopCoord = buildDrawGraph(node.getSuccessorList().get(0), toopCoord[0], toopCoord[1], height / 2, joinMap, nodeLevel);
                        middleCoord = buildDrawGraph(node.getSuccessorList().get(1), middleCoord[0], middleCoord[1], height / 2, joinMap, nodeLevel);
                        bottomCoord = buildDrawGraph(node.getSuccessorList().get(2), bottomCoord[0], bottomCoord[1], height / 2, joinMap, nodeLevel);

                        //构建join节点闭合
                        SWFControlNode joinNode = joinMap.get(node.getNodeDescp());

                        double x1 = toopCoord[0];
                        double y1 = toopCoord[1];
                        double x2 = middleCoord[0];
                        double y2 = middleCoord[1];
                        double x3 = bottomCoord[0];
                        double y3 = bottomCoord[1];

                        double drawX = x1 > x2 ? x1 + 50 : x2 + 50;
                        drawX = drawX > x3 ? drawX : x3 + 50;

                        //画直线
                        HLine topLine = new HLine(x1, y1, drawX - x1);
                        showGraph.getChildren().add(topLine);
                        toopCoord = topLine.getEnd();

                        //画射线
                        HArrows middleHarrows = new HArrows(x2, y2, drawX - x2 - ControlDraw.getLength());
                        showGraph.getChildren().add(middleHarrows);
                        middleCoord = middleHarrows.getEnd();

                        //画直线
                        HLine bottomLine = new HLine(x3, y3, drawX - x3);
                        showGraph.getChildren().add(bottomLine);
                        bottomCoord = bottomLine.getEnd();

                        x1 = toopCoord[0];
                        y1 = toopCoord[1];
                        x3 = bottomCoord[0];
                        y3 = bottomCoord[1];

                        VArrows topArrows = new VArrows(x1, y1, height);
                        showGraph.getChildren().add(topArrows);
                        VArrows bottomArrows = new VArrows(x3, y3, -height);
                        showGraph.getChildren().add(bottomArrows);

                        ControlDraw joinDraw = new ControlDraw(x1 - ControlDraw.getLength(), middleCoord[1]);
                        joinDraw.setText("+");
                        showGraph.getChildren().add(joinDraw);

                        result = joinDraw.getRight();
                        result = buildDrawGraph(joinNode.getSuccessorList().get(0), result[0], result[1], height, joinMap, nodeLevel);
                        return result;
                    }
                } else if (((SWFControlNode) node).getOneType().equals(ControlNodeType.JOIN)) {
                    return new double[]{x, y};
                }
            } else if (((SWFControlNode) node).getPairType().equals(ControlNodeType.XOR)) {//如果是xor节点
                if (((SWFControlNode) node).getOneType().equals(ControlNodeType.SPLIT)) {
                    //箭头线
                    HArrows HArrows = new HArrows(x, y, 50);
                    showGraph.getChildren().add(HArrows);

                    //控制流节点
                    double[] result = HArrows.getEnd();
                    ControlDraw controlDraw = new ControlDraw(result[0], result[1]);
                    controlDraw.setText("X");
                    showGraph.getChildren().add(controlDraw);

                    int size = node.getSuccessorList().size();
                    if (size == 2) {
                        //上下画线
                        double[] toopCoord = controlDraw.getTop();
                        double[] middleCoord = controlDraw.getRight();
                        double[] bottomCoord = controlDraw.getBottom();

                        VLine vLineTop = new VLine(toopCoord[0], toopCoord[1], -height);
                        showGraph.getChildren().add(vLineTop);
                        VLine vLineBottom = new VLine(bottomCoord[0], bottomCoord[1], height);
                        showGraph.getChildren().add(vLineBottom);

                        //递归构建分支图
                        toopCoord = vLineTop.getEnd();
                        bottomCoord = vLineBottom.getEnd();
                        toopCoord = buildDrawGraph(node.getSuccessorList().get(0), toopCoord[0], toopCoord[1], height / 2, joinMap, nodeLevel);
                        bottomCoord = buildDrawGraph(node.getSuccessorList().get(1), bottomCoord[0], bottomCoord[1], height / 2, joinMap, nodeLevel);

                        //构建join节点闭合
                        SWFControlNode joinNode = joinMap.get(node.getNodeDescp());

                        double x1 = toopCoord[0];
                        double y1 = toopCoord[1];
                        double x2 = bottomCoord[0];
                        double y2 = bottomCoord[1];

                        double drawX = x1 > x2 ? x1 + 50 : x2 + 50;
                        HLine topLine = new HLine(x1, y1, drawX - x1);
                        showGraph.getChildren().add(topLine);
                        toopCoord = topLine.getEnd();

                        HLine bottomLine = new HLine(x2, y2, drawX - x2);
                        showGraph.getChildren().add(bottomLine);
                        bottomCoord = bottomLine.getEnd();

                        x1 = toopCoord[0];
                        y1 = toopCoord[1];
                        x2 = bottomCoord[0];
                        y2 = bottomCoord[1];

                        VArrows topArrows = new VArrows(x1, y1, height);
                        showGraph.getChildren().add(topArrows);
                        VArrows bottomArrows = new VArrows(x2, y2, -height);
                        showGraph.getChildren().add(bottomArrows);

                        ControlDraw joinDraw = new ControlDraw(x1 - ControlDraw.getLength(), middleCoord[1]);
                        joinDraw.setText("X");
                        showGraph.getChildren().add(joinDraw);

                        result = joinDraw.getRight();
                        result = buildDrawGraph(joinNode.getSuccessorList().get(0), result[0], result[1], height, joinMap, nodeLevel);
                        return result;
                    } else if (size == 3) {
                        //上中下画线
                        double[] toopCoord = controlDraw.getTop();
                        double[] middleCoord = controlDraw.getRight();
                        double[] bottomCoord = controlDraw.getBottom();

                        VLine vLineTop = new VLine(toopCoord[0], toopCoord[1], -height);
                        showGraph.getChildren().add(vLineTop);
                        VLine vLineBottom = new VLine(bottomCoord[0], bottomCoord[1], height);
                        showGraph.getChildren().add(vLineBottom);

                        //递归构建分支图
                        toopCoord = vLineTop.getEnd();
                        bottomCoord = vLineBottom.getEnd();
                        toopCoord = buildDrawGraph(node.getSuccessorList().get(0), toopCoord[0], toopCoord[1], height / 2, joinMap, nodeLevel);
                        middleCoord = buildDrawGraph(node.getSuccessorList().get(1), middleCoord[0], middleCoord[1], height / 2, joinMap, nodeLevel);
                        bottomCoord = buildDrawGraph(node.getSuccessorList().get(2), bottomCoord[0], bottomCoord[1], height / 2, joinMap, nodeLevel);

                        //构建join节点闭合
                        SWFControlNode joinNode = joinMap.get(node.getNodeDescp());

                        double x1 = toopCoord[0];
                        double y1 = toopCoord[1];
                        double x2 = middleCoord[0];
                        double y2 = middleCoord[1];
                        double x3 = bottomCoord[0];
                        double y3 = bottomCoord[1];

                        double drawX = x1 > x2 ? x1 + 50 : x2 + 50;
                        drawX = drawX > x3 ? drawX : x3 + 50;

                        //画直线
                        HLine topLine = new HLine(x1, y1, drawX - x1);
                        showGraph.getChildren().add(topLine);
                        toopCoord = topLine.getEnd();

                        //画射线
                        HArrows middleHarrows = new HArrows(x2, y2, drawX - x2 - ControlDraw.getLength());
                        showGraph.getChildren().add(middleHarrows);
                        middleCoord = middleHarrows.getEnd();

                        //画直线
                        HLine bottomLine = new HLine(x3, y3, drawX - x3);
                        showGraph.getChildren().add(bottomLine);
                        bottomCoord = bottomLine.getEnd();

                        x1 = toopCoord[0];
                        y1 = toopCoord[1];
                        x3 = bottomCoord[0];
                        y3 = bottomCoord[1];

                        VArrows topArrows = new VArrows(x1, y1, height);
                        showGraph.getChildren().add(topArrows);
                        VArrows bottomArrows = new VArrows(x3, y3, -height);
                        showGraph.getChildren().add(bottomArrows);

                        ControlDraw joinDraw = new ControlDraw(x1 - ControlDraw.getLength(), middleCoord[1]);
                        joinDraw.setText("X]");
                        showGraph.getChildren().add(joinDraw);

                        result = joinDraw.getRight();
                        result = buildDrawGraph(joinNode.getSuccessorList().get(0), result[0], result[1], height, joinMap, nodeLevel);
                        return result;
                    }
                } else if (((SWFControlNode) node).getOneType().equals(ControlNodeType.JOIN)) {
                    return new double[]{x, y};
                }
            } else if (((SWFControlNode) node).getPairType().equals(ControlNodeType.LOOP)) {
                if (((SWFControlNode) node).getOneType().equals(ControlNodeType.SPLIT)) {
                    //箭头线
                    HArrows HArrows = new HArrows(x, y, 50);
                    showGraph.getChildren().add(HArrows);

                    //控制流节点
                    double[] result = HArrows.getEnd();
                    ControlDraw controlDraw = new ControlDraw(result[0], result[1]);
                    controlDraw.setText("X");
                    showGraph.getChildren().add(controlDraw);

                    result = controlDraw.getRight();
                    double[] topCoord = controlDraw.getTop();

                    //递归构建分支图
                    result = buildDrawGraph(node.getSuccessorList().get(0), result[0], result[1], height / 2, joinMap, nodeLevel);

                    HArrows hArrows = new HArrows(result[0], result[1], 50);
                    showGraph.getChildren().add(hArrows);
                    result = hArrows.getEnd();


                    //构建join节点闭合
                    SWFControlNode joinNode = joinMap.get(node.getNodeDescp());

                    ControlDraw joinDraw = new ControlDraw(result[0], result[1]);
                    joinDraw.setText("X");
                    showGraph.getChildren().add(joinDraw);

                    VArrows topLine = new VArrows(joinDraw.getTop()[0], joinDraw.getTop()[1], -height / 2);
                    showGraph.getChildren().add(topLine);
                    double[] branchCoord = topLine.getEnd();

                    result = joinDraw.getRight();
                    SWFFlowNode branchNode = judgeBranch(joinNode, (SWFControlNode) node, nodeLevel);
                    for (SWFFlowNode snode : joinNode.getSuccessorList()) {
                        if (snode.equals(branchNode)) {
                            branchCoord = buildLoopBranch(snode, branchCoord[0], branchCoord[1], height / 2, joinMap);
                            HArrows hLine = new HArrows(branchCoord[0], branchCoord[1], -(branchCoord[0] - topCoord[0]));
                            showGraph.getChildren().add(hLine);
                            branchCoord = hLine.getEnd();
                            VArrows arrows = new VArrows(branchCoord[0], branchCoord[1], -(branchCoord[1] - topCoord[1]));
                            showGraph.getChildren().add(arrows);
                        } else {
                            result = buildDrawGraph(snode, result[0], result[1], height, joinMap, nodeLevel);
                        }
                    }
                    return result;
                } else if (((SWFControlNode) node).getOneType().equals(ControlNodeType.JOIN)) {
                    return new double[]{x, y};
                }
            }

        } else if (node instanceof SWFEndNode) {//如果是结束节点
            HArrows HArrows = new HArrows(x, y, 50);
            showGraph.getChildren().add(HArrows);

            double[] result = HArrows.getEnd();
            CircleDraw endDraw = new CircleDraw(result[0], result[1]);
            showGraph.getChildren().add(endDraw);
        }

        return null;
    }

    ////////////////////////////////////////////////构建图形界面，对循环分支进行特殊处理///////////////////////////////////////////////////
    private double[] buildLoopBranch(SWFFlowNode node, double x, double y, double height,
                                     HashMap<String, SWFControlNode> joinMap) {
        if (node instanceof SWFTaskNode) {//如果是任务节点
            HArrows HArrows = new HArrows(x, y, -50);
            showGraph.getChildren().add(HArrows);
            double[] result = HArrows.getEnd();

            TaskDraw taskDraw = new TaskDraw(result[0] - TaskDraw.getLength() * 3, result[1]);
            taskDraw.setText(node.getNodeDescp());
            showGraph.getChildren().add(taskDraw);
            result = taskDraw.getLeft();
            result = buildLoopBranch(node.getSuccessorList().get(0), result[0], result[1], height, joinMap);
            return result;
        } else if (node instanceof SWFControlNode) {
            if (((SWFControlNode) node).getOneType().equals(ControlNodeType.SPLIT)) {
                return new double[]{x, y};
            }
        }
        return null;
    }

    //判断循环分支
    private SWFFlowNode judgeBranch(SWFControlNode joinNode, SWFControlNode splitNode,
                                    HashMap<SWFFlowNode, Integer> nodeLevel) {
        for (SWFFlowNode node : joinNode.getSuccessorList()) {
            if (node.equals(splitNode)) {
                return node;
            }
            Stack<SWFFlowNode> nodeStack = new Stack<>();
            nodeStack.push(node);
            HashSet<SWFFlowNode> nodeSet = new HashSet<>();
            nodeSet.add(node);
            while (!nodeStack.isEmpty()) {
                SWFFlowNode nowNode = nodeStack.pop();
                ArrayList<SWFFlowNode> successorList = nowNode.getSuccessorList();
                for (SWFFlowNode n : successorList) {
                    //如果该级别大于循环节点级别
                    if (n.equals(splitNode) && nodeLevel.get(nowNode) > nodeLevel.get(n)) {
                        return node;
                    }
                    if (!nodeSet.contains(n)) {
                        nodeSet.add(n);
                        nodeStack.push(n);
                    }
                }

            }
        }
        return null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////资源交互处理///////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //为资源交互做准备
    private boolean interactionSource(ArrayList<SWFControlNode> splitControlList, ArrayList<SWFControlNode> joinControlList, SWFStartNode startNode) {
        ArrayList<String> locationList = new ArrayList<>();
        locationList.add("0");
        locationList.add("1");
        sourceLocation.setValue("0");
        sourceLocation.setItems(FXCollections.observableArrayList(locationList));

        HashMap<String, SWFControlNode> joinNodeMap = new HashMap<>();
        //设置join控制流节点的map映射
        for (int i = 0; i < joinControlList.size(); i++) {
            joinNodeMap.put(joinControlList.get(i).getNodeDescp(), joinControlList.get(i));
        }

        //设置节点级别，用来判断循环节点的分支，
        HashMap<SWFFlowNode, Integer> nodeLevel = new HashMap<>();
        Stack<SWFFlowNode> nodeStack = new Stack<>();
        nodeStack.push(startNode.getSuccessorList().get(0));
        nodeLevel.put(startNode.getSuccessorList().get(0), 0);
        while (!nodeStack.isEmpty()) {
            //出栈
            SWFFlowNode nowNode = nodeStack.pop();
            //获取节点级别
            int level = nodeLevel.get(nowNode);
            //获取后继节点集合
            ArrayList<SWFFlowNode> successorNodeList = nowNode.getSuccessorList();
            for (SWFFlowNode node : successorNodeList) {
                if (node instanceof SWFEndNode) {
                    continue;
                }
                if (nodeLevel.getOrDefault(node, -1) == -1) {//如果该节点没有设置级别
                    nodeLevel.put(node, level + 1);
                    nodeStack.push(node);
                }
            }
        }


        branchMap = new HashMap<>();

        for (int i = 0; i < splitControlList.size(); i++) {
            SWFControlNode splitNode = splitControlList.get(i);
            if (splitNode.getPairType().equals(ControlNodeType.AND)) {
                SWFControlNode joinNode = joinNodeMap.get(splitNode.getNodeDescp());
                if (joinNode == null) {
                    break;
                }

                ArrayList<ArrayList<SWFFlowNode>> branchList = new ArrayList<>();
                for (int j = 0; j < splitNode.getSuccessorList().size(); j++) {
                    ArrayList<SWFFlowNode> flowList = new ArrayList<>();
                    SWFFlowNode nowNode = splitNode.getSuccessorList().get(j);
                    while (nowNode != null) {
                        if (nowNode instanceof SWFEndNode) {
                            return false;
                        }
                        if (nowNode instanceof SWFTaskNode) {
                            flowList.add(nowNode);
                            if (!nowNode.getSuccessorList().isEmpty()) {
                                nowNode = nowNode.getSuccessorList().get(0);
                            } else {
                                nowNode = null;
                            }
                        } else if (nowNode instanceof SWFControlNode) {
                            if (nowNode.equals(joinNode)) {
                                break;
                            }
                            flowList.add(nowNode);
                            nowNode = joinNodeMap.get(nowNode.getNodeDescp());
//                        if(nowNode.getSuccessorList()!=null){
                            if (!nowNode.getSuccessorList().isEmpty()) {
                                if (((SWFControlNode) nowNode).getPairType().equals(ControlNodeType.LOOP)) {
                                    SWFFlowNode result = judgeBranch(joinNode, splitNode, nodeLevel);
                                    for (SWFFlowNode n : nowNode.getSuccessorList()) {
                                        if (!n.equals(result)) {
                                            nowNode = n;
                                        }
                                    }
                                } else {
                                    nowNode = nowNode.getSuccessorList().get(0);
                                }
                            } else {
                                nowNode = null;
                            }
                        }
                    }
                    branchList.add(flowList);
                }
                branchMap.put("branch:" + i, branchList);
            }
        }

        ArrayList<String> valueList = new ArrayList<>();
        valueList.add("请选择");
        Iterator iterator = branchMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            valueList.add((String) entry.getKey());
        }

        if (valueList.size() > 1) {
            branchChoice.setValue(valueList.get(0));
            branchChoice.setItems(FXCollections.observableArrayList(valueList));
        }
        return true;
    }

    //选择资源交互分支
    public void selectBranch() {
        System.out.println("11111111111111");
        String value = (String) branchChoice.getValue();

        ArrayList<ArrayList<SWFFlowNode>> branchList = branchMap.getOrDefault(value, null);
        if (branchList == null) {
            return;
        }

        ArrayList<String> valueList1 = new ArrayList<>();
        valueList1.add("请选择");

        ArrayList<String> valueList2 = new ArrayList<>();
        valueList2.add("请选择");

        ArrayList<SWFFlowNode> nowFlowList = branchList.get(0);
        for (int i = 0; i < nowFlowList.size(); i++) {
            SWFFlowNode nowNode = nowFlowList.get(i);
            valueList1.add(nowNode.getNodeDescp() + ":" + i);
        }

        nowFlowList = branchList.get(1);
        for (int i = 0; i < nowFlowList.size(); i++) {
            SWFFlowNode nowNode = nowFlowList.get(i);
            valueList2.add(nowNode.getNodeDescp() + ":" + i);
        }

        if (valueList1.size() > 1 && valueList2.size() > 1) {
            sourceChoice1.setValue("请选择");
            sourceChoice2.setValue("请选择");
            sourceChoice1.setItems(FXCollections.observableArrayList(valueList1));
            sourceChoice2.setItems(FXCollections.observableArrayList(valueList2));
        }
    }

    public void executeSource(ActionEvent event) {
        String branchValue = (String) branchChoice.getValue();
        String choiceValue1 = (String) sourceChoice1.getValue();
        String choiceValue2 = (String) sourceChoice2.getValue();
        System.out.println(branchValue);
        if ((branchValue==null) || (choiceValue1==null) || (choiceValue2==null)){
            return;
        }

        ArrayList<SWFControlNode> splitControlList = swfNewGraph.getSplitControlList();
        ArrayList<SWFControlNode> joinControlList = swfNewGraph.getJoinControlList();
        ArrayList<SWFControlFlowEdge> controlEdgeList = swfNewGraph.getSwfControlFlowEdgeList();
        String selectLocation = (String) sourceLocation.getValue();

        HashMap<String, SWFControlNode> joinNodeMap = new HashMap<>();
        for (SWFControlNode node : joinControlList) {
            joinNodeMap.put(node.getNodeDescp(), node);
        }


        if ((!branchValue.equals("请选择")) && (!choiceValue1.equals("请选择")) && (!choiceValue2.equals("请选择"))) {
            ArrayList<ArrayList<SWFFlowNode>> branchList = branchMap.getOrDefault(branchValue, null);
            if (branchList == null) {
                return;
            }
            int index1 = -1;
            int index2 = -1;
            int index = -1;
            try {
                String[] result = branchValue.split(":");
                index = Integer.parseInt(result[1]);
                result = choiceValue1.split(":");
                index1 = Integer.parseInt(result[1]);
                result = choiceValue2.split(":");
                index2 = Integer.parseInt(result[1]);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            SWFControlNode splitNode = splitControlList.get(index);
            ArrayList<SWFFlowNode> nodeList1 = branchList.get(0);
            ArrayList<SWFFlowNode> nodeList2 = branchList.get(1);
            SWFFlowNode node1 = nodeList1.get(index1);
            SWFFlowNode node2 = nodeList2.get(index2);

            int size1 = nodeList1.size();
            int size2 = nodeList2.size();

            if (size1 == 1 && size2 >= 1) {
                oneToMany(node1, node2, nodeList1, nodeList2, splitControlList, joinControlList, joinNodeMap);
            } else if (size2 == 1 && size1 > 1) {
                oneToMany(node2, node1, nodeList2, nodeList1, splitControlList, joinControlList, joinNodeMap);
            } else {
                manyToMany(node1, node2, nodeList1, nodeList2, splitNode, splitControlList, joinControlList, joinNodeMap);
            }


            controlEdgeList.clear();
            SWFStartNode startNode = swfNewGraph.getStartNode();
            LinkedHashSet<SWFFlowNode> set = new LinkedHashSet<>();
            Queue<SWFFlowNode> queue = new LinkedList<>();
            queue.add(startNode);
            set.add(startNode);
            while (!queue.isEmpty()) {
                SWFFlowNode swfFlowNode = queue.poll();
                for (SWFFlowNode node : swfFlowNode.getSuccessorList()) {
                    SWFControlFlowEdge swfControlFlowEdge = new SWFControlFlowEdge(swfFlowNode, node);
                    controlEdgeList.add(swfControlFlowEdge);
                    if (!set.contains(node)) {
                        queue.add(node);
                        set.add(node);
                    }
                }
            }

            set.clear();
            queue.clear();
            //去掉边的连接关系
            set = new LinkedHashSet<>();
            queue = new LinkedList<>();
            queue.add(startNode);
            set.add(startNode);
            while (!queue.isEmpty()) {
                SWFFlowNode swfFlowNode = queue.poll();
                swfFlowNode.removeAllPrecursorNode(swfFlowNode.getPrecursorList());
                for (SWFFlowNode node : swfFlowNode.getSuccessorList()) {
                    if (!set.contains(node)) {
                        queue.add(node);
                        set.add(node);
                    }
                }
                swfFlowNode.removeAllSuccessorNode(swfFlowNode.getSuccessorList());
            }

            ArrayList<SWFNode> nodeList = new ArrayList<>();
            ArrayList<SWFEdge> edgeList = new ArrayList<>();
            nodeList.add(startNode);
            nodeList.add(swfNewGraph.getEndNode());
            nodeList.addAll(swfNewGraph.getDataNodeList());
            nodeList.addAll(swfNewGraph.getTaskNodeList());
            nodeList.addAll(splitControlList);
            nodeList.addAll(joinControlList);
            edgeList.addAll(controlEdgeList);
            edgeList.addAll(swfNewGraph.getDataEdgeList());

            SWFGraph swfGraph = new SWFGraph(nodeList, edgeList);

            showGraph.getChildren().removeAll(showGraph.getChildren());
            buildSWFGUI(swfGraph);
            double pResult=getNumberOfParallelNodePair(swfGraph);
            paraResult.setText("="+String.format("%.2f",pResult));

            interactionSource(splitControlList, joinControlList, startNode);
        }
    }

    private void oneToMany(SWFFlowNode one, SWFFlowNode two, ArrayList<SWFFlowNode> nodeList1, ArrayList<SWFFlowNode> nodeList2,
                           ArrayList<SWFControlNode> splitControlList, ArrayList<SWFControlNode> joinControlList,
                           HashMap<String, SWFControlNode> joinNodeMap) {
        int size = nodeList2.size();
        int index2 = nodeList2.indexOf(two);
        SWFControlNode splitNode;
        SWFControlNode joinNode;

        try {
            splitNode = (SWFControlNode) one.getPrecursorList().get(0);
            joinNode = joinNodeMap.get(splitNode.getNodeDescp());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (size == 1) {//如果两分支上都只有一个节点
            SWFFlowNode front = splitNode.getPrecursorList().get(0);
            front.removeSuccessorNode(splitNode);
            one.removePrecursorNode(splitNode);
            two.removePrecursorNode(splitNode);


            SWFFlowNode later = joinNode.getSuccessorList().get(0);
            later.removePrecursorNode(joinNode);
            if (one instanceof SWFControlNode) {
                SWFControlNode oneJoinNode = joinNodeMap.get(one.getNodeDescp());
                oneJoinNode.removeSuccessorNode(joinNode);
            } else {
                one.removeSuccessorNode(joinNode);
            }
            if (two instanceof SWFControlNode) {
                SWFControlNode twoJoinNode = joinNodeMap.get(two.getNodeDescp());
                twoJoinNode.removeSuccessorNode(joinNode);
            } else {
                two.removeSuccessorNode(joinNode);
            }

            front.addSuccessorNode(one);
            one.addPrecursorNode(front);

            if (one instanceof SWFControlNode) {
                SWFControlNode oneJoinNode = joinNodeMap.get(one.getNodeDescp());
                oneJoinNode.addSuccessorNode(two);
                two.addPrecursorNode(oneJoinNode);
            } else {
                one.addSuccessorNode(two);
                two.addPrecursorNode(one);
            }
            if (two instanceof SWFControlNode) {
                SWFControlNode twoJoinNode = joinNodeMap.get(two.getNodeDescp());
                twoJoinNode.addSuccessorNode(later);
                later.addPrecursorNode(twoJoinNode);
            } else {
                two.addSuccessorNode(later);
                later.addPrecursorNode(two);
            }

            splitControlList.remove(splitNode);
            joinControlList.remove(joinNode);
        } else {
            if (index2 == 0) {
                SWFFlowNode front = splitNode.getPrecursorList().get(0);
                front.removeSuccessorNode(splitNode);
                splitNode.removePrecursorNode(front);

                splitNode.removeSuccessorNode(two);
                two.removePrecursorNode(splitNode);

                SWFFlowNode later;
                if (two instanceof SWFControlNode) {
                    SWFControlNode twoJoinNode = joinNodeMap.get(two.getNodeDescp());
                    later = twoJoinNode.getSuccessorList().get(0);
                    twoJoinNode.removeSuccessorNode(later);
                    later.removePrecursorNode(twoJoinNode);

                    front.addSuccessorNode(two);
                    two.addPrecursorNode(front);

                    twoJoinNode.addSuccessorNode(splitNode);
                    splitNode.addPrecursorNode(twoJoinNode);
                } else {
                    later = two.getSuccessorList().get(0);
                    two.removeSuccessorNode(later);
                    later.removePrecursorNode(two);

                    front.addSuccessorNode(two);
                    two.addPrecursorNode(front);

                    two.addSuccessorNode(splitNode);
                    splitNode.addPrecursorNode(two);
                }

                splitNode.addSuccessorNode(later);
                later.addPrecursorNode(splitNode);
            } else if (index2 == size - 1) {
                SWFFlowNode front = two.getPrecursorList().get(0);
                front.removeSuccessorNode(two);
                two.removePrecursorNode(front);

                if (two instanceof SWFControlNode) {
                    SWFControlNode twoJoinNode = joinNodeMap.get(two.getNodeDescp());
                    twoJoinNode.removeSuccessorNode(joinNode);
                    joinNode.removePrecursorNode(twoJoinNode);
                } else {
                    two.removeSuccessorNode(joinNode);
                    joinNode.removePrecursorNode(two);
                }

                SWFFlowNode later = joinNode.getSuccessorList().get(0);
                later.removePrecursorNode(joinNode);
                joinNode.removeSuccessorNode(later);

                front.addSuccessorNode(joinNode);
                joinNode.addPrecursorNode(front);

                joinNode.addSuccessorNode(two);
                two.addPrecursorNode(joinNode);

                if (two instanceof SWFControlNode) {
                    SWFControlNode twoJoinNode = joinNodeMap.get(two.getNodeDescp());
                    twoJoinNode.addSuccessorNode(later);
                    later.addPrecursorNode(twoJoinNode);
                } else {
                    two.addSuccessorNode(later);
                    later.addPrecursorNode(two);
                }
            } else {
                SWFFlowNode front = two.getPrecursorList().get(0);
                front.removeSuccessorNode(two);
                two.removePrecursorNode(front);

                SWFFlowNode end = nodeList2.get(size - 1);
                if (end instanceof SWFControlNode) {
                    SWFControlNode endJoinNode = joinNodeMap.get(end.getNodeDescp());
                    endJoinNode.removeSuccessorNode(joinNode);
                    joinNode.removePrecursorNode(endJoinNode);
                } else {
                    end.removeSuccessorNode(joinNode);
                    joinNode.removePrecursorNode(end);
                }


                SWFFlowNode later = joinNode.getSuccessorList().get(0);
                later.removePrecursorNode(later);
                joinNode.removeSuccessorNode(later);

                front.addSuccessorNode(joinNode);
                joinNode.addPrecursorNode(front);

                joinNode.addSuccessorNode(two);
                two.addPrecursorNode(joinNode);

                if (end instanceof SWFControlNode) {
                    SWFControlNode endJoinNode = joinNodeMap.get(end.getNodeDescp());
                    endJoinNode.addSuccessorNode(later);
                    later.addPrecursorNode(endJoinNode);
                } else {
                    end.addSuccessorNode(later);
                    later.addPrecursorNode(end);
                }

            }
        }

    }

    private void manyToMany(SWFFlowNode one, SWFFlowNode two, ArrayList<SWFFlowNode> nodeList1, ArrayList<SWFFlowNode> nodeList2,
                            SWFControlNode splitNode, ArrayList<SWFControlNode> splitControlList,
                            ArrayList<SWFControlNode> joinControlList, HashMap<String, SWFControlNode> joinNodeMap) {
        int index1 = nodeList1.indexOf(one);
        int index2 = nodeList2.indexOf(two);
        int size1 = nodeList1.size();
        int size2 = nodeList2.size();
        SWFControlNode joinNode = joinNodeMap.get(splitNode.getNodeDescp());

        if (index1 == 0 && index2 == 0) {
            SWFFlowNode front = splitNode.getPrecursorList().get(0);
            front.removeSuccessorNode(splitNode);
            splitNode.removePrecursorNode(front);

            one.removePrecursorNode(splitNode);
            splitNode.removeSuccessorNode(one);

            SWFFlowNode later;
            if (one instanceof SWFControlNode) {
                SWFControlNode oneJoinNode = joinNodeMap.get(one.getNodeDescp());
                later = oneJoinNode.getSuccessorList().get(0);
                later.removePrecursorNode(oneJoinNode);
                oneJoinNode.removeSuccessorNode(later);
            } else {
                later = one.getSuccessorList().get(0);
                later.removePrecursorNode(one);
                one.removeSuccessorNode(later);
            }


            front.addSuccessorNode(one);
            one.addPrecursorNode(front);

            if (one instanceof SWFControlNode) {
                SWFControlNode oneJoinNode = joinNodeMap.get(one.getNodeDescp());
                oneJoinNode.addSuccessorNode(splitNode);
                splitNode.addPrecursorNode(oneJoinNode);
            } else {
                one.addSuccessorNode(splitNode);
                splitNode.addPrecursorNode(one);
            }

            splitNode.addSuccessorNode(later);
            later.addPrecursorNode(splitNode);
        } else if (index1 == size1 - 1 && index2 == size2 - 1) {
            SWFFlowNode front = two.getPrecursorList().get(0);
            front.removeSuccessorNode(two);
            two.removePrecursorNode(front);

            if (two instanceof SWFControlNode) {
                SWFControlNode twoJoinNode = joinNodeMap.get(two.getNodeDescp());
                twoJoinNode.removeSuccessorNode(joinNode);
                joinNode.removePrecursorNode(twoJoinNode);
            } else {
                two.removeSuccessorNode(joinNode);
                joinNode.removePrecursorNode(two);
            }


            SWFFlowNode later = joinNode.getSuccessorList().get(0);
            later.removePrecursorNode(joinNode);
            joinNode.removeSuccessorNode(later);

            front.addSuccessorNode(joinNode);
            joinNode.addPrecursorNode(front);

            joinNode.addSuccessorNode(two);
            two.addPrecursorNode(joinNode);

            if (two instanceof SWFControlNode) {
                SWFControlNode twoJoinNode = joinNodeMap.get(two.getNodeDescp());
                twoJoinNode.addSuccessorNode(later);
                later.addPrecursorNode(twoJoinNode);
            } else {
                two.addSuccessorNode(later);
                later.addPrecursorNode(two);
            }

        } else {
            SWFFlowNode later;
            if (one instanceof SWFControlNode) {
                SWFControlNode oneJoinNode = joinNodeMap.get(one.getNodeDescp());
                later = oneJoinNode.getSuccessorList().get(0);
                oneJoinNode.removeSuccessorNode(later);
                later.removePrecursorNode(oneJoinNode);
            } else {
                later = one.getSuccessorList().get(0);
                one.removeSuccessorNode(later);
                later.removePrecursorNode(one);
            }


            SWFFlowNode front = two.getPrecursorList().get(0);
            two.removePrecursorNode(front);
            front.removeSuccessorNode(two);

            String desc = joinNode.getNodeDescp();
            SWFControlNode newJoinNode = new SWFControlNode(NodeType.CONTROL_NODE, desc, ControlNodeType.AND, ControlNodeType.JOIN);
            joinNodeMap.put(desc, newJoinNode);
            joinControlList.add(newJoinNode);

            desc = MyUUID.getUUID32();
            joinNode.setNodeDescp(desc);
            SWFControlNode newSplitNode = new SWFControlNode(NodeType.CONTROL_NODE, desc, ControlNodeType.AND, ControlNodeType.SPLIT);
            joinNodeMap.put(desc, joinNode);
            splitControlList.add(newSplitNode);

            if (one instanceof SWFControlNode) {
                SWFControlNode oneJoinNode = joinNodeMap.get(one.getNodeDescp());
                newJoinNode.addPrecursorNode(oneJoinNode);
                oneJoinNode.addSuccessorNode(newJoinNode);
            } else {
                newJoinNode.addPrecursorNode(one);
                one.addSuccessorNode(newJoinNode);
            }


            newJoinNode.addPrecursorNode(front);
            front.addSuccessorNode(newJoinNode);

            newJoinNode.addSuccessorNode(newSplitNode);
            newSplitNode.addPrecursorNode(newJoinNode);

            newSplitNode.addSuccessorNode(later);
            later.addPrecursorNode(newSplitNode);
            newSplitNode.addSuccessorNode(two);
            two.addPrecursorNode(newSplitNode);
        }
    }




}
