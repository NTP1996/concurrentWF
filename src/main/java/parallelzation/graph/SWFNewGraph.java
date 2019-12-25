package parallelzation.graph;


import cern.colt.matrix.DoubleMatrix2D;
import parallelzation.edge.SWFControlFlowEdge;
import parallelzation.edge.SWFDataFlowEdge;
import parallelzation.node.*;

import java.util.ArrayList;

/**
 * @author kuangzengxiong
 * @date 2019/3/31 20:19
 */
public class SWFNewGraph extends BaseGraph {
    private SWFStartNode startNode;
    private SWFEndNode endNode;
    private ArrayList<SWFTaskNode> taskNodeList;
    private ArrayList<SWFDataNode> dataNodeList;
    private ArrayList<SWFControlNode> splitControlList;
    private ArrayList<SWFControlNode> joinControlList;

    private ArrayList<SWFControlFlowEdge> swfControlFlowEdgeList;
    private ArrayList<SWFDataFlowEdge> dataEdgeList;

    private DoubleMatrix2D parallelRelations;
//    private ArrayList<SWFDataFlowInEdge> dataInEdgeList;
//    private ArrayList<SWFDataFlowOutEdge> dataOutEdgeList;

    public SWFNewGraph(SWFStartNode startNode, SWFEndNode endNode, ArrayList<SWFDataNode> dataNodeList,
                       ArrayList<SWFTaskNode> taskNodeList, ArrayList<SWFControlNode> splitControlList,
                       ArrayList<SWFControlNode> joinControlList, ArrayList<SWFControlFlowEdge> swfControlFlowEdgeList,
                       ArrayList<SWFDataFlowEdge> dataEdgeList) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.taskNodeList = taskNodeList;
        this.dataNodeList = dataNodeList;
        this.splitControlList = splitControlList;
        this.joinControlList = joinControlList;
        this.swfControlFlowEdgeList = swfControlFlowEdgeList;
        this.dataEdgeList = dataEdgeList;


    }

    public SWFStartNode getStartNode() {
        return startNode;
    }

    public SWFEndNode getEndNode() {
        return endNode;
    }

    public ArrayList<SWFTaskNode> getTaskNodeList() {
        return taskNodeList;
    }

    public ArrayList<SWFDataNode> getDataNodeList() {
        return dataNodeList;
    }

    public ArrayList<SWFControlNode> getSplitControlList() {
        return splitControlList;
    }

    public ArrayList<SWFControlNode> getJoinControlList() {
        return joinControlList;
    }

    public ArrayList<SWFControlFlowEdge> getSwfControlFlowEdgeList() {
        return swfControlFlowEdgeList;
    }

    public ArrayList<SWFDataFlowEdge> getDataEdgeList() {
        return dataEdgeList;
    }

    public DoubleMatrix2D getParallelRelations() {
        return parallelRelations;
    }

    public void setParallelRelations(DoubleMatrix2D parallelRelations) {
        this.parallelRelations = parallelRelations;
    }
}
