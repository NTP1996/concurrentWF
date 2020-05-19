package parallelzation.node;

import org.apache.poi.ss.formula.functions.T;

import java.util.ArrayList;

/**
 * @author kuangzengxiong
 * @date 2019/3/8 15:38
 */
public class SWFTaskNode extends SWFFlowNode {
    private ArrayList <SWFDataNode> inputNodeList;  // 输入数据节点集合

    private ArrayList <SWFDataNode> outputNodeList;  // 输出数据节点集合


    public SWFTaskNode(String nodeType, String nodeDescp,ArrayList <SWFDataNode> input,
                       ArrayList <SWFDataNode> output) {
        super(nodeType, nodeDescp);
        this.inputNodeList=input;
        this.outputNodeList =output;
    }

    public void print(){
        String info ;

        System.out.println();
    }


    public void addINputNode(SWFDataNode node){
        inputNodeList.add(node);
    }

    public void addOutputNode(SWFDataNode node){
        outputNodeList.add(node);
    }

    public boolean isProcessingTask(){
        if(this.inputNodeList.size() == 0 && this.outputNodeList.size() ==0)
            return true;
        else
            return false;
    }

    public ArrayList<SWFDataNode> getInputNodeList() {
        return inputNodeList;
    }

    public ArrayList<SWFDataNode> getOutputNodeList() {
        return outputNodeList;
    }

}
