package parallelzation.graph;


import parallelzation.node.*;
import util.ControlNodeType;
import java.util.ArrayList;

/**
 * @author kuangzengxiong
 * @date 2019/4/26 23:02
 */
public class SWFBuildBlock extends BaseGraph {
    public static int recursionSum = 1;

    private SWFControlNode startControlNode;
    private SWFControlNode endControlNode;

    //递归处理块节点的次数
    private int recursionNumber = 0;
    //控制流节点块类型
    private String blockType;

    //任务节点集合
    private ArrayList<SWFTaskNode> swfTaskNodeList;
    //数据节点集合
    private ArrayList<SWFDataNode> swfDataNodeList;
    //split控制流节点集合
    private ArrayList<SWFControlNode> splitControlNodeList;
    //join控制流节点集合
    private ArrayList<SWFControlNode> joinControlNodeList;
    //块节点集合
    private ArrayList<SWFPairControlNode> blockNodeList;


    public SWFBuildBlock(SWFPairControlNode pairControlNode) {
        blockType = pairControlNode.getControlType();
        //开始、结束控制流节点
        startControlNode = pairControlNode.getStartControlNode();
        endControlNode = pairControlNode.getEndControlNode();
        //任务节点集合
        swfTaskNodeList = new ArrayList<>();
        //数据节点集合
        swfDataNodeList = new ArrayList<>();
        //split控制流节点集合
        splitControlNodeList = new ArrayList<>();
        //join控制流集合
        joinControlNodeList = new ArrayList<>();
        //块节点集合
        blockNodeList = new ArrayList<>();


        //找到任务节点和块节点
        for (SWFFlowNode node : pairControlNode.getInlayNodeList()) {
            if (node instanceof SWFTaskNode) {
                swfTaskNodeList.add((SWFTaskNode) node);
            } else if (node instanceof SWFPairControlNode) {
                blockNodeList.add((SWFPairControlNode) node);
            }
        }

        //获取控制流节点集合
        splitControlNodeList.addAll(pairControlNode.getSplitNodeList());
        joinControlNodeList.addAll(pairControlNode.getJoinNodeList());

        //获取数据流节点集合
        swfDataNodeList.addAll(pairControlNode.getInputNodeList());
        swfDataNodeList.addAll(pairControlNode.getOutputNodeList());
    }

    public void processBrach() {
        if (recursionNumber >= recursionSum) {
            return;
        }

        if (blockType.equals(ControlNodeType.LOOP)) {
            //处理split开始的循环分支
            SWFBlockBranch firstBranch = new SWFBlockBranch(startControlNode, endControlNode, swfDataNodeList);
            SWFBuildBranch firstParllel = new SWFBuildBranch(firstBranch);
            ArrayList<SWFFlowNode> result = firstParllel.parallelizeBrach();

            SWFStartNode startNode = (SWFStartNode) result.get(0);
            for (SWFFlowNode node : startNode.getSuccessorList()) {
                node.removePrecursorNode(startNode);
                node.addPrecursorNode(startControlNode);
                startControlNode.addSuccessorNode(node);
            }

            SWFEndNode endNode = (SWFEndNode) result.get(1);
            for (SWFFlowNode node : endNode.getPrecursorList()) {
                node.removeSuccessorNode(endNode);
                node.addSuccessorNode(endControlNode);
                endControlNode.addPrecursorNode(node);
            }

            //处理join开始的循环分支
            SWFBlockBranch secondBranch = new SWFBlockBranch(endControlNode, startControlNode, swfDataNodeList);
            SWFBuildBranch secondParllel = new SWFBuildBranch(secondBranch);
            result = secondParllel.parallelizeBrach();
            startNode = (SWFStartNode) result.get(0);
            for (SWFFlowNode node : startNode.getSuccessorList()) {
                node.removePrecursorNode(startNode);
                node.addPrecursorNode(endControlNode);
                endControlNode.addSuccessorNode(node);
            }

            endNode = (SWFEndNode) result.get(1);
            for (SWFFlowNode node : endNode.getPrecursorList()) {
                node.removeSuccessorNode(endNode);
                node.addSuccessorNode(startControlNode);
                startControlNode.addPrecursorNode(node);
            }

        }

        if (blockType.equals(ControlNodeType.XOR)) {
            for (SWFFlowNode node : startControlNode.getSuccessorList()) {
                if (node.equals(endControlNode)) {//如果该分支上没有节点
                    continue;
                }
                SWFFlowNode temp = node;
                while (!temp.getSuccessorList().get(0).equals(endControlNode)) {
                    temp = temp.getSuccessorList().get(0);
                }
                if (temp.equals(node)) {//如果该分支上只有一个节点
                    continue;
                }
                SWFBlockBranch branch = new SWFBlockBranch(node, temp, swfDataNodeList);
                SWFBuildBranch branchParllel = new SWFBuildBranch(branch);
                ArrayList<SWFFlowNode> result = branchParllel.parallelizeBrach();

                SWFStartNode startNode = (SWFStartNode) result.get(0);
                for (SWFFlowNode n : startNode.getSuccessorList()) {
                    n.removePrecursorNode(startNode);
                    n.addPrecursorNode(startControlNode);
                    startControlNode.addSuccessorNode(n);
                }

                SWFEndNode endNode = (SWFEndNode) result.get(1);
                for (SWFFlowNode n : endNode.getPrecursorList()) {
                    n.removeSuccessorNode(endNode);
                    n.addSuccessorNode(endControlNode);
                    endControlNode.addPrecursorNode(n);
                }
            }
        }

    }

}
