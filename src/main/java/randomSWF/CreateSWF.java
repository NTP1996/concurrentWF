package randomSWF;





import parallelzation.edge.SWFControlFlowEdge;
import parallelzation.edge.SWFDataFlowEdge;
import parallelzation.graph.SWFNewGraph;
import parallelzation.node.*;
import util.NodeType;


import java.util.ArrayList;
import java.util.Random;

/**
 * @author kuangzengxiong
 * @date 2019/5/16 16:59
 */
public class CreateSWF {
    private int minNum=2;
    private int maxNum=50;
    private static int swfNum=0;

    public CreateSWF(){
    }

    public CreateSWF(int minNum, int maxNum){
        this.minNum=minNum;
        this.maxNum=maxNum;
//        this.createNum=createNum;
    }

    public SWFNewGraph execute(){
        Random randomNum=new Random();
        int taskNodeSize=randomNum.nextInt(maxNum-minNum)+minNum;
        int dataNodeSize=randomNum.nextInt(taskNodeSize*2+1-taskNodeSize)+taskNodeSize;

        ArrayList<SWFTaskNode> taskNodeList=new ArrayList<>();
        ArrayList<SWFDataNode> dataNodeList=new ArrayList<>();
        ArrayList<SWFControlFlowEdge> controlEdgeList=new ArrayList<>();
        ArrayList<SWFDataFlowEdge> inputEdgeList=new ArrayList<>();
        ArrayList<SWFDataFlowEdge> outputEdgeList=new ArrayList<>();

        for(int i=0;i<dataNodeSize;i++){
            SWFDataNode dataNode=new SWFDataNode(NodeType.DATA_NODE,"data"+i);
            dataNode.setNodeId(String.valueOf(i));
            dataNodeList.add(dataNode);
        }

        int preId=0;
        for(int i=0;i<taskNodeSize;i++){
            ArrayList<SWFDataNode> outputNodeList=new ArrayList<>();
            ArrayList<SWFDataNode> inputNodeList=new ArrayList<>();

            //随机生成输出节点的坐标
            int dataId=randomNum.nextInt(dataNodeSize-taskNodeSize+i+1-preId)+preId;
            outputNodeList.add(dataNodeList.get(dataId));

            //随机生成输入数据节点的个数
            int inputSize=randomNum.nextInt(3)+1;
            inputSize=inputSize>=dataId?dataId+1:inputSize;

            for(int j=0;j<inputSize;j++){
                int inputDataId=randomNum.nextInt(dataId+1);
                if(!inputNodeList.contains(dataNodeList.get(inputDataId))){
                    inputNodeList.add(dataNodeList.get(inputDataId));
                }
            }

            SWFTaskNode taskNode=new SWFTaskNode(NodeType.TASK_NODE,"T"+i,inputNodeList,outputNodeList);
            taskNode.setNodeId(String.valueOf(i));
            taskNodeList.add(taskNode);

            //增加数据输出边
            for(int j=0;j<outputNodeList.size();j++){
                outputEdgeList.add(new SWFDataFlowEdge(taskNode,outputNodeList.get(j)));
            }
            //增加数据输入边
            for(SWFDataNode dataNode:inputNodeList){
                inputEdgeList.add(new SWFDataFlowEdge(dataNode,taskNode));
            }

            preId=dataId+1;
        }


        //增加控制流边
        for(int i=0;i<taskNodeSize-1;i++){
            SWFControlFlowEdge controlFlowEdge=new SWFControlFlowEdge(taskNodeList.get(i),taskNodeList.get(i+1));
            controlEdgeList.add(controlFlowEdge);
        }

        SWFStartNode startNode=new SWFStartNode(NodeType.START_NODE,"");
        SWFEndNode endNode=new SWFEndNode(NodeType.END_NODE,"");
        controlEdgeList.add(new SWFControlFlowEdge(startNode,taskNodeList.get(0)));
        controlEdgeList.add(new SWFControlFlowEdge(taskNodeList.get(taskNodeSize-1),endNode));

        ArrayList<SWFDataFlowEdge> dataEdgeList=new ArrayList<>();
        dataEdgeList.addAll(inputEdgeList);
        dataEdgeList.addAll(outputEdgeList);

        SWFNewGraph swfNewGraph=new SWFNewGraph(startNode,endNode,dataNodeList,taskNodeList,
                new ArrayList<SWFControlNode>(),new ArrayList<SWFControlNode>(),controlEdgeList,dataEdgeList);

        swfNewGraph.setCaseId("swf"+swfNum);
        swfNum++;

        return swfNewGraph;
    }

}
