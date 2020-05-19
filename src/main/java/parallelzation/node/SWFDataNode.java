package parallelzation.node;

import parallelzation.edge.SWFDataFlowEdge;
import parallelzation.edge.SWFDataFlowInEdge;
import parallelzation.edge.SWFDataFlowOutEdge;

import java.util.ArrayList;

/**
 * @author kuangzengxiong
 * @date 2019/3/8 15:32
 */
public class SWFDataNode extends SWFNode {
    ArrayList<SWFDataFlowEdge> inEdgesList = new ArrayList<>();
    ArrayList<SWFDataFlowEdge> outEdgesList = new ArrayList<>();

    public SWFDataNode(String nodeType, String nodeDescp) {
        super(nodeType, nodeDescp);
        // TODO Auto-generated constructor stub
    }

    public void addInEdge(SWFDataFlowEdge edge){
        inEdgesList.add( edge);
    }
    public void addOutEdge(SWFDataFlowEdge edge){
        outEdgesList.add(edge);
    }

    public void setInEdgesList(ArrayList<SWFDataFlowEdge> inEdgeList) {
        this.inEdgesList = inEdgeList;
    }

    public void setOutEdgesList(ArrayList<SWFDataFlowEdge> outEdgeList) {
        this.outEdgesList = outEdgeList;
    }
    public ArrayList<SWFTaskNode> getConsumerTaskList(){
        ArrayList<SWFTaskNode> taskNodeArrayList = new ArrayList<>();
        for(SWFDataFlowEdge edge: this.outEdgesList){

            taskNodeArrayList.add( (SWFTaskNode) edge.getRightNode());
        }
        return taskNodeArrayList;
    }
}
