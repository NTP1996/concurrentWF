package parallelzation.node;

import java.util.ArrayList;

/**
 * @author kuangzengxiong
 * @date 2019/3/29 21:51
 */
public class SWFFlowNode extends SWFNode implements Comparable<SWFFlowNode>{
    private ArrayList<SWFFlowNode> precursorList;//前驱节点集合
    private ArrayList<SWFFlowNode> successorList;//后继节点集合

    public SWFFlowNode(String nodeType, String nodeDescp){
            super(nodeType, nodeDescp);
            // TODO Auto-generated constructor stub
            precursorList=new ArrayList<>();
            successorList=new ArrayList<>();
    }

    @Override
    public int compareTo(SWFFlowNode o) {
        return this.getNodeId().compareTo(o.getNodeId());
    }

    public ArrayList<SWFFlowNode> getPrecursorList() {
        return precursorList;
    }

    public ArrayList<SWFFlowNode> getSuccessorList() {
        return successorList;
    }


    public boolean addPrecursorNode(SWFFlowNode precursorNode){
        return this.precursorList.add(precursorNode);
    }
    public boolean addSuccessorNode(SWFFlowNode successorNode){
        return this.successorList.add(successorNode);
    }

    public boolean removePrecursorNode(SWFFlowNode precursorNode){
        return this.precursorList.remove(precursorNode);
    }
    public boolean removeSuccessorNode(SWFFlowNode successorNode){
        return this.successorList.remove(successorNode);
    }

    public boolean removeAllPrecursorNode(ArrayList<SWFFlowNode> precursorList){
        return this.precursorList.removeAll(precursorList);
    }
    public boolean removeAllSuccessorNode(ArrayList<SWFFlowNode> successorList){
        return this.successorList.removeAll(successorList);
    }

}
