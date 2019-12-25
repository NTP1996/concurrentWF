package parallelzation.node;

import java.util.ArrayList;

/**
 * @author kuangzengxiong
 * @date 2019/4/15 14:31
 */
public class SWFPairControlNode extends SWFFlowNode{
    //控制流节点开始的split节点
    private SWFControlNode startControlNode=null;
    //控制流节点结束的join节点
    private SWFControlNode endControlNode=null;
    //内置的split节点集合
    private ArrayList<SWFControlNode> splitNodeList;
    //内置的join节点集合
    private ArrayList<SWFControlNode> joinNodeList;
    //控制流节点对的内置节点
    private ArrayList<SWFFlowNode> inlayNodeList;
    // 输入数据节点集合
    private ArrayList <SWFDataNode> inputNodeList;
    // 输出数据节点集合
    private ArrayList <SWFDataNode> outputNodeList;
    //控制流节点对类型
    private String controlType;

    public SWFPairControlNode(String nodeType,String nodeDescp,String controlType){
        super(nodeType, nodeDescp);
        this.controlType=controlType;
        inlayNodeList=new ArrayList<>();
        inputNodeList=new ArrayList<>();
        outputNodeList=new ArrayList<>();
        splitNodeList=new ArrayList<>();
        joinNodeList=new ArrayList<>();
    }

    public SWFControlNode getStartControlNode() {
        return startControlNode;
    }

    public void setStartControlNode(SWFControlNode splitControlNode) {
        this.startControlNode = splitControlNode;
    }

    public SWFControlNode getEndControlNode() {
        return endControlNode;
    }

    public void setEndControlNode(SWFControlNode joinControlNode) {
        this.endControlNode = joinControlNode;
    }

    public ArrayList<SWFFlowNode> getInlayNodeList() {
        return inlayNodeList;
    }

    public void addInlayNode(SWFFlowNode swfFlowNode){
        inlayNodeList.add(swfFlowNode);
    }

    public void removeInlayNode(SWFFlowNode swfFlowNode){
        inlayNodeList.remove(swfFlowNode);
    }

    public void removeAllInlayNode(ArrayList<SWFFlowNode> swfFlowNodes){
        inlayNodeList.removeAll(swfFlowNodes);
    }

    public String getControlType() {
        return controlType;
    }

    public ArrayList<SWFDataNode> getInputNodeList() {
        return inputNodeList;
    }

    public ArrayList<SWFDataNode> getOutputNodeList() {
        return outputNodeList;
    }

    public void addInputNode(SWFDataNode dataNode){
        if(dataNode!=null){
            this.inputNodeList.add(dataNode);
        }
    }
    public void addAllInputNode(ArrayList<SWFDataNode> dataNodes){
        if(dataNodes!=null){
            this.inputNodeList.addAll(dataNodes);
        }
    }

    public void addOutputNode(SWFDataNode dataNode){
        if(dataNode!=null){
            this.outputNodeList.add(dataNode);
        }
    }
    public void addAllOutputNode(ArrayList<SWFDataNode> dataNodes){
        if(dataNodes!=null){
            this.outputNodeList.addAll(dataNodes);
        }
    }

    public ArrayList<SWFControlNode> getSplitNodeList() {
        return splitNodeList;
    }

    public void addSplitNodeList(SWFControlNode splitNode) {
        this.splitNodeList.add(splitNode);
    }

    public ArrayList<SWFControlNode> getJoinNodeList() {
        return joinNodeList;
    }

    public void addJoinNodeList(SWFControlNode joinNode) {
        this.joinNodeList.add(joinNode);
    }
}
