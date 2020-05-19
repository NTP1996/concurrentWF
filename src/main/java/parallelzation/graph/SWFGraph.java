package parallelzation.graph;


import parallelzation.edge.SWFEdge;
import parallelzation.node.SWFNode;
import java.util.ArrayList;

/**
 * @author kuangzengxiong
 * @date 2019/3/8 15:39
 */
public class SWFGraph extends BaseGraph {
    public SWFGraph(ArrayList<SWFNode> nodeList, ArrayList<SWFEdge> edgeList) {
        super();
        setNodeList(nodeList);
        setEdgeList(edgeList);
    }

}
