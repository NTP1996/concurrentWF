package parallelzation.graph;
import parallelzation.edge.SWFDataFlowEdge;
import parallelzation.node.SWFDataNode;
import parallelzation.node.SWFTaskNode;
import java.util.ArrayList;

/**
 * @author zhoubowen
 * @date 2020/5/14 11:19
 */
public class WFStream extends BaseGraph {
    // stream 所有输入数据节点
    ArrayList<SWFDataNode> inputDataNodeList = new ArrayList<>();
    // stream 所有输出数据节点
    ArrayList<SWFDataNode> outputDataNodeList = new ArrayList<>();

    // stream 的输入数据节点
    ArrayList<SWFDataNode> streamInputDataNodeList = new ArrayList<>();
    // stream 的输入数据节点
    ArrayList<SWFDataNode> streamOutputDataNodeList = new ArrayList<>();
    // stream 所有任务节点
    ArrayList<SWFTaskNode> taskNodeList = new ArrayList<>();

    //如果为真则这个 stream 产生了一个新的数据节点
    private boolean isProducerStream;
    boolean isJoined = false;

    //如果为真则这个 stream 消耗了一个由其他 stream 生产的数据节点
    private boolean isConsumerStream;

    public void addInputDataNode(SWFDataNode node ){
        inputDataNodeList.add(node);
        // 若 stream output 中有这个input data，则这个节点是被内部消耗的，从 stream output 中移除
        if( streamOutputDataNodeList.contains(node)){
            streamOutputDataNodeList.remove(node);
        }else{
            streamInputDataNodeList.add(node);
        }
    }

    public void clear(){
        this.inputDataNodeList.clear();
        this.outputDataNodeList.clear();
        this.streamOutputDataNodeList.clear();
        this.streamInputDataNodeList.clear();
        this.isConsumerStream = false;
        this.isProducerStream = false;
        this.taskNodeList.clear();
        this.isJoined = false;
    }

    public void addOutputDataNode(SWFDataNode node){
        outputDataNodeList.add(node);
        // 同 addInputDataNode
//        if(streamInputDataNodeList.contains(node)){
//            streamInputDataNodeList.remove(node);
//        }else{
            streamOutputDataNodeList.add(node);
//        }
    }

    public void  addTaskAndData(SWFTaskNode node){
        for(SWFDataNode dataNode: node.getInputNodeList()){
            this.addInputDataNode(dataNode);
        }
        for(SWFDataNode dataNode:node.getOutputNodeList()){
            this.addOutputDataNode(dataNode);
        }
        this.addTaskNode(node);

    }

    public boolean containTaskNode(SWFTaskNode node){
        if(this.taskNodeList.contains(node))
            return true;
        else
            return false;

    }

    public void setIsProducerStream(boolean isProducerStream) {
        this.isProducerStream = isProducerStream;
    }


    public void setIsConsumerStream(boolean isConsumerStream) {
        this.isConsumerStream = isConsumerStream;
    }


    public boolean isProducerStream() {
        return isProducerStream;
    }


    public boolean isConsumerStream() {
        return isConsumerStream;
    }

    public void addStreamInputDataNode(SWFDataNode node){
        streamInputDataNodeList.add(node);
    }
    public void addStreamOutputDataNode(SWFDataNode node){
        streamOutputDataNodeList.add(node);
    }
    public void addTaskNode(SWFTaskNode node){
        taskNodeList.add(node);
    }

    public ArrayList<SWFDataNode> getInputDataNodeList() {
        return inputDataNodeList;
    }

    public ArrayList<SWFDataNode> getOutputDataNodeList() {
        return outputDataNodeList;
    }

    public ArrayList<SWFDataNode> getStreamInputDataNodeList() {
        return streamInputDataNodeList;
    }

    public ArrayList<SWFDataNode> getStreamOutputDataNodeList() {
        return streamOutputDataNodeList;
    }

    public ArrayList<SWFTaskNode> getTaskNodeList() {
        return taskNodeList;
    }

    public void setJoined(boolean isJoined) {
        this.isJoined=isJoined;
    }
    public void clearStreamOutput() {
        streamOutputDataNodeList.clear();
    }

    public SWFTaskNode getLastTaskNode(){
        return taskNodeList.get(taskNodeList.size()-1);
    }

    public boolean isJoined() {
        return isJoined;
    }
    public boolean isInitStream(){
        if (this.taskNodeList.size() ==0) return true;
        else return false;
    }
    public void print(){
        String info = "";
        info +="\nStream ID: "+this.getCaseId();
        if (this.isProducerStream())
            info +="[Producer]";
        if (this.isConsumerStream())
            info +="[Consumer]";
        info+= '\n';

        info +="Task: ";
        for(SWFTaskNode node:getTaskNodeList())
            info +=node.getNodeDescp()+"  ";
        info += '\n';

        info +="Stream input: ";
        for(SWFDataNode node:this.getStreamInputDataNodeList())
            info +=node.getNodeDescp()+' ';
        info += '\n';

        info +="Stream output: ";
        for(SWFDataNode node:this.getStreamOutputDataNodeList())
        info +=node.getNodeDescp()+' ';
        System.out.println(info);
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
