package parallelzation.graph;


import parallelzation.node.SWFControlNode;
import parallelzation.node.SWFEndNode;
import parallelzation.node.SWFFlowNode;
import parallelzation.node.SWFStartNode;
import util.ControlNodeType;
import util.NodeType;


import java.util.ArrayList;

/**
 * @author kuangzengxiong
 * @date 2019/4/27 22:48
 */
public class SWFBuildBranch extends SWFBuildGraph {

    public SWFBuildBranch(SWFBlockBranch swfBlockBranch) {
        //数据节点集合
        swfDataNodeList = swfBlockBranch.getSwfDataNodeList();
        //split控制流节点集合
        splitControlNodeList = swfBlockBranch.getSplitControlNodeList();
        //join控制流节点集合
        joinControlNodeList = swfBlockBranch.getJoinControlNodeList();

        //控制流边集合
        swfControlEdgeList = swfBlockBranch.getSwfControlEdgeList();

        //split节点的编号，和节点的映射
        joinNodeMap = swfBlockBranch.getJoinNodeMap();

        swfStartNode = swfBlockBranch.getStartNode();
        swfEndNode = swfBlockBranch.getEndNode();
    }

    /**
     * 对分支进行并行化重构
     */
    public ArrayList<SWFFlowNode> parallelizeBrach() {
        initializeMatrixNodeList();
        getLogRelationsFromGraph();
        getDataDependenceFromGraph();
        updateRelations();
        getSpecialNode();
        addControlNodeToAlpha();
        reconstitutionBranch();

        ArrayList<SWFFlowNode> result = new ArrayList<>();
        result.add(swfStartNode);
        result.add(swfEndNode);
        return result;
    }

    /**
     * 后处理，并返回新的控制流分支
     */
    private void reconstitutionBranch() {
        SWFStartNode startNode = new SWFStartNode(NodeType.START_NODE, "");
        SWFEndNode endNode = new SWFEndNode(NodeType.END_NODE, "");
        int size = matrixNodeList.size();

        ArrayList<SWFFlowNode> recordList = new ArrayList<>();
        //构建开始节点关系
        for (int i = 0; i < size; i++) {
            if (startModelNode.get(i) == 1) {
                recordList.add(matrixNodeList.get(i));
            }
        }
        for (SWFFlowNode node : splitControlNodeList) {
            if (node.getPrecursorList().isEmpty()) {
                recordList.add(node);
            }
        }

        if (recordList.size() > 1) {
            SWFControlNode splitNode = new SWFControlNode(NodeType.CONTROL_NODE, "new" + staticNumber,
                    ControlNodeType.AND, ControlNodeType.SPLIT);
            for (SWFFlowNode node : recordList) {
                splitNode.addSuccessorNode(node);
                node.addPrecursorNode(splitNode);
            }
            splitControlNodeList.add(splitNode);
            startNode.addSuccessorNode(splitNode);
            splitNode.addPrecursorNode(startNode);
        } else {
            startNode.addSuccessorNode(recordList.get(0));
            recordList.get(0).addPrecursorNode(startNode);
        }

        recordList.clear();
        //构建结束节点关系
        for (int i = 0; i < size; i++) {
            if (endModelNode.get(i) == 1) {
                recordList.add(matrixNodeList.get(i));
            }
        }
        for (SWFFlowNode node : joinControlNodeList) {
            if (node.getSuccessorList().isEmpty()) {
                recordList.add(node);
            }
        }

        if (recordList.size() > 1) {
            SWFControlNode joinNode = new SWFControlNode(NodeType.CONTROL_NODE, "new" + staticNumber,
                    ControlNodeType.AND, ControlNodeType.JOIN);
            staticNumber++;
            for (SWFFlowNode node : recordList) {
                joinNode.addPrecursorNode(node);
                node.addSuccessorNode(joinNode);
            }
            joinControlNodeList.add(joinNode);
            endNode.addPrecursorNode(joinNode);
            joinNode.addSuccessorNode(startNode);
        } else {
            endNode.addPrecursorNode(recordList.get(0));
            recordList.get(0).addSuccessorNode(endNode);
        }
    }

}
