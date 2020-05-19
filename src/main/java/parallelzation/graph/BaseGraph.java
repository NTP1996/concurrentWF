package parallelzation.graph;


import parallelzation.edge.SWFEdge;
import parallelzation.node.SWFNode;

import java.util.ArrayList;

/**
 * @author kuangzengxiong
 * @date 2019/3/8 19:56
 */
public class BaseGraph {
    private ArrayList<SWFNode> nodeList;  // 节点集合
    private ArrayList<SWFEdge> edgeList;  // 边集合
    private String caseId;

//	public HashSet <SWFNode> nodeList;  // 节点集合
//	public HashSet <SWFEdge> edgeList;  // 边集合

    public ArrayList<SWFNode> getNodeList() {
        return nodeList;
    }

    public void setNodeList(ArrayList<SWFNode> nodeList) {
        this.nodeList = nodeList;
    }

    public ArrayList<SWFEdge> getEdgeList() {
        return edgeList;
    }

    public void setEdgeList(ArrayList<SWFEdge> edgeList) {
        this.edgeList = edgeList;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }
}
