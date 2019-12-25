package parallelzation.graph;


import parallelzation.edge.SWFControlFlowEdge;
import parallelzation.node.*;
import util.ControlNodeType;
import util.MyUUID;
import util.NodeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * @author kuangzengxiong
 * @date 2019/4/27 11:10
 */
public class SWFBlockBranch {
    //分支的开始节点
    private SWFStartNode startNode;
    //分支的结束节点
    private SWFEndNode endNode;

    //数据节点集合
    private ArrayList<SWFDataNode> swfDataNodeList;
    //split控制流节点集合
    private ArrayList<SWFControlNode> splitControlNodeList;
    //join控制流节点集合
    private ArrayList<SWFControlNode> joinControlNodeList;

    //split节点的编号，和节点的映射
    private HashMap<String, SWFControlNode> joinNodeMap;

    //控制流边集合
    private ArrayList<SWFControlFlowEdge> swfControlEdgeList;

    public SWFBlockBranch(SWFFlowNode startNode, SWFFlowNode endNode, ArrayList<SWFDataNode> swfDataNodeList) {
        //数据节点集合
        this.swfDataNodeList = swfDataNodeList;

        //分支控制流边集合
        ArrayList<SWFControlFlowEdge> branchControlEdgeList = new ArrayList<>();
        //分支split节点集合
        ArrayList<SWFControlNode> branchSplitNodeList = new ArrayList<>();
        //分支join节点集合
        ArrayList<SWFControlNode> branchJoinNodeList = new ArrayList<>();

        Stack<SWFFlowNode> nodeStack = new Stack<>();
        ArrayList<SWFFlowNode> nodeList = new ArrayList<>();

        if (startNode instanceof SWFControlNode && ((SWFControlNode) startNode).getPairType().equals(ControlNodeType.LOOP)) {
            //处理split开始的循环分支
            nodeStack.push(startNode.getSuccessorList().get(0));
            nodeList.add(startNode.getSuccessorList().get(0));
            while (!nodeStack.isEmpty()) {
                SWFFlowNode nowNode = nodeStack.pop();
                //获取分支的split和join控制流节点
                if (nowNode instanceof SWFControlNode) {
                    if (((SWFControlNode) nowNode).getOneType().equals(ControlNodeType.SPLIT)) {
                        branchSplitNodeList.add((SWFControlNode) nowNode);
                    } else if (((SWFControlNode) nowNode).getOneType().equals(ControlNodeType.JOIN)) {
                        branchJoinNodeList.add((SWFControlNode) nowNode);
                    }
                }

                //获取分支的控制流边
                for (SWFFlowNode node : nowNode.getSuccessorList()) {
                    if (node.equals(endNode)) {
                        continue;
                    }
                    branchControlEdgeList.add(new SWFControlFlowEdge(nowNode, node));
                    if (!nodeList.contains(node)) {
                        nodeStack.push(node);
                        nodeList.add(node);
                    }

                }
            }

            //split控制流节点集合
            this.splitControlNodeList = branchSplitNodeList;
            //join控制流节点集合
            this.joinControlNodeList = branchJoinNodeList;
            //控制流边集合
            this.swfControlEdgeList = branchControlEdgeList;

            //更改开始和结束节点
            SWFFlowNode successorNode = startNode.getSuccessorList().get(0);
            startNode.removeSuccessorNode(successorNode);
            successorNode.removePrecursorNode(startNode);
            this.startNode = new SWFStartNode(NodeType.START_NODE, MyUUID.getUUID32());
            this.startNode.addSuccessorNode(successorNode);
            successorNode.addPrecursorNode(this.startNode);

            SWFFlowNode precursorNode = endNode.getPrecursorList().get(0);
            endNode.removePrecursorNode(precursorNode);
            precursorNode.removeSuccessorNode(precursorNode);
            this.endNode = new SWFEndNode(NodeType.END_NODE, MyUUID.getUUID32());
            this.endNode.addPrecursorNode(precursorNode);
            precursorNode.addSuccessorNode(this.endNode);
        } else {
            //处理split开始的循环分支
            nodeStack.push(startNode);
            nodeList.add(startNode);
            while (!nodeStack.isEmpty()) {
                SWFFlowNode nowNode = nodeStack.pop();
                //获取分支的split和join控制流节点
                if (nowNode instanceof SWFControlNode) {
                    if (((SWFControlNode) nowNode).getOneType().equals(ControlNodeType.SPLIT)) {
                        branchSplitNodeList.add((SWFControlNode) nowNode);
                    } else if (((SWFControlNode) nowNode).getOneType().equals(ControlNodeType.JOIN)) {
                        branchJoinNodeList.add((SWFControlNode) nowNode);
                    }
                }

                //获取分支的控制流边
                for (SWFFlowNode node : nowNode.getSuccessorList()) {
                    branchControlEdgeList.add(new SWFControlFlowEdge(nowNode, node));
                    if (!nodeList.contains(node)) {
                        nodeStack.push(node);
                        nodeList.add(node);
                    }

                }
            }

            //split控制流节点集合
            this.splitControlNodeList = branchSplitNodeList;
            //join控制流节点集合
            this.joinControlNodeList = branchJoinNodeList;
            //控制流边集合
            this.swfControlEdgeList = branchControlEdgeList;

            //更改开始和结束节点
            SWFFlowNode precursorNode = startNode.getPrecursorList().get(0);
            startNode.removePrecursorNode(precursorNode);
            precursorNode.removePrecursorNode(startNode);
            this.startNode = new SWFStartNode(NodeType.START_NODE, MyUUID.getUUID32());
            this.startNode.addSuccessorNode(startNode);
            startNode.addPrecursorNode(this.startNode);

            SWFFlowNode successorNode = endNode.getSuccessorList().get(0);
            endNode.removeSuccessorNode(successorNode);
            successorNode.removePrecursorNode(endNode);
            this.endNode = new SWFEndNode(NodeType.END_NODE, MyUUID.getUUID32());
            this.endNode.addPrecursorNode(endNode);
            endNode.addSuccessorNode(this.endNode);
        }


        //split节点的编号，和节点的映射
        joinNodeMap = new HashMap<>();

        for (SWFControlNode node : joinControlNodeList) {
            joinNodeMap.put(node.getNodeDescp(), node);
        }
    }


    public SWFStartNode getStartNode() {
        return startNode;
    }

    public SWFEndNode getEndNode() {
        return endNode;
    }

    public ArrayList<SWFDataNode> getSwfDataNodeList() {
        return swfDataNodeList;
    }

    public ArrayList<SWFControlNode> getSplitControlNodeList() {
        return splitControlNodeList;
    }

    public ArrayList<SWFControlNode> getJoinControlNodeList() {
        return joinControlNodeList;
    }

    public HashMap<String, SWFControlNode> getJoinNodeMap() {
        return joinNodeMap;
    }

    public ArrayList<SWFControlFlowEdge> getSwfControlEdgeList() {
        return swfControlEdgeList;
    }
}
