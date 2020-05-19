package parallelzation.node;

import util.ControlNodeType;
import util.NodeType;

import java.util.ArrayList;

/**
 * @author kuangzengxiong
 * @date 2019/3/29 21:51
 */
public class SWFFlowNode extends SWFNode implements Comparable<SWFFlowNode>{
    private ArrayList<SWFFlowNode> precursorList;//直接前驱节点集合
    private ArrayList<SWFFlowNode> successorList;//直接后继节点集合

    public SWFFlowNode(String nodeType, String nodeDescp){
            super(nodeType, nodeDescp);
            // TODO Auto-generated constructor stub
            precursorList=new ArrayList<>();
            successorList=new ArrayList<>();
    }

    //实现 comareTo 接口，完成节点之间的比较（通过 ID 比较大小）
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


    public SWFFlowNode getSuccessorNode(){
        if(successorList.size() != 0)
            return  successorList.get(0);
        else
            return null;
    }

    // 获得所以前序
    public ArrayList<SWFFlowNode> getallPreviousNodes(){
        ArrayList<SWFFlowNode> preNodeList = new ArrayList<>();
        ArrayList<SWFFlowNode> nextNodeList = this.getPrecursorList();
        for(SWFFlowNode nextNode: nextNodeList){
            preNodeList.add(nextNode);
            preNodeList.addAll(nextNode.getallPreviousNodes());
//            if(this instanceof SWFControlNode && ((SWFControlNode) nextNode).getOneType().equals(ControlNodeType.SPLIT))
//                break;
        }
        return preNodeList;
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
