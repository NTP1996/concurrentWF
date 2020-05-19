package parallelzation.graph;

import parallelzation.edge.SWFEdge;
import parallelzation.node.*;
import util.ControlNodeType;
import util.NodeType;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author zhoubowen
 * @date 2020/5/14 11:40
 */

public class WFStreamBuilder {
    private WFStreamContainer streamContainer ;
    private ArrayList<SWFFlowNode> visitedNode ;

    public WFStreamContainer building(SWFNewGraph graph){
        this.streamContainer = new WFStreamContainer();
        this.visitedNode = new ArrayList<>();

       SWFFlowNode startnode = graph.getStartNode();
        WFStream stream =new WFStream();
        this.streamContainer.addStream(stream);
        //遍历工作流，找到 stream 并加入到 streamContainer
        buildingFragmemts(stream,startnode);
//        streamContainer.print();
        streamContainer.removeEmpty();
        this.appendStreams();
        return streamContainer;
    }

    private void buildingFragmemts(WFStream stream, SWFFlowNode node){
        if(visitedNode.contains(node)|| node ==null){
            return;
        }
        visitedNode.add(node);


//        System.out.println(node.getNodeDescp());

        // 若是开始或结束节点，则遍历一下一个，或结束
        if (node.getNodeType().equals(NodeType.START_NODE) || node.getNodeType().equals(NodeType.END_NODE))
            buildingFragmemts(stream,node.getSuccessorNode());

        if(node instanceof SWFControlNode){
            // 如果是控制节点
            stream = new WFStream();
            this.streamContainer.addStream(stream);
            for(SWFFlowNode nextnode :node.getSuccessorList())
             buildingFragmemts(stream,nextnode);
        }
        else if(node instanceof SWFTaskNode) {
            //如果是任务节点
//            System.out.println("else" + node.getNodeType());
            SWFTaskNode taskNode = (SWFTaskNode) node;

            // 如果 tasknode 消耗数据D，且当前 stream 不是数据 D 的生产者
            if (isConsumer(stream, taskNode)) {
                stream = new WFStream();
                this.streamContainer.addStream(stream);
                stream.setIsConsumerStream(true);
            } else {
                if (!stream.isInitStream()) {
                    if (!dataflowConnected(stream, taskNode)) {
//                        System.out.println("出现这句话，请注意查看--01");
                        stream = new WFStream();
                        streamContainer.addStream(stream);
                    }
                }
            }

            //在这部分，tasknode 就加入stream 中了
            if (!taskNode.isProcessingTask()) {
                // 若不是处理节点，直接添加
                stream.addTaskAndData(taskNode);
            }
            // Todo 这里不全，若是处理节点应该怎么办
//            else{
//
//            }

            if (isProducer(taskNode)) {
                stream.setIsProducerStream(true);
                stream = new WFStream();
                streamContainer.addStream(stream);
            }

            buildingFragmemts(stream,taskNode.getSuccessorNode());
        }

    }



    private boolean isProducer(SWFTaskNode node ){
        for(SWFDataNode outputData: node.getOutputNodeList()){
            if (!node.getInputNodeList().contains(outputData)){
                return true;
            }
        }
        return  false;
    }

    private boolean isConsumer(WFStream stream,SWFTaskNode taskNode){
        for (SWFDataNode dataNode: taskNode.getInputNodeList()){
            for (WFStream wfStream: this.streamContainer.getStreamList()){
                if(wfStream.getStreamOutputDataNodeList().contains(dataNode)
                        && !stream.getStreamOutputDataNodeList().contains(dataNode))
                    return true;
            }
        }
        return false;
    }

    private boolean dataflowConnected(WFStream stream,SWFTaskNode node){

        for(SWFDataNode dataNode : stream.getOutputDataNodeList()){
            for(SWFTaskNode swfTaskNode: dataNode.getConsumerTaskList()){
                if(swfTaskNode.equals(node)){
                    return true;
                }
            }
        }
        return false;
    }
    private void appendStreams() {
        boolean appending = true;
        while (appending == true) {
            appending = false;
            // for each stream

            for (WFStream stream1 : streamContainer.getMergableWFStreams()) {
                String info =stream1.getCaseId()+":";
                for(WFStream stream: streamContainer.getStreamList())
                    info += " "+stream.getCaseId();
                System.out.println(info);
                if (stream1.isProducerStream() || stream1.isJoined())
                {
                    continue;
                }
                LinkedList<WFStream> appendableStreams = new LinkedList<WFStream>();
                // get all appendable streams
                for (WFStream stream2 : streamContainer.getMergableWFStreams()) {
                    System.out.println(stream1.getCaseId()+"----"+stream2.getCaseId()+"----?run");
                    if (isAppendable(stream1, stream2)) {
                        appendableStreams.add(stream2);
                    }
                }

                // 找到顺序不正确的 stream
                LinkedList<WFStream> filterStreams = new LinkedList<WFStream>();
                for (WFStream wfStream1 : appendableStreams) {
                    for (WFStream wfStream2 : appendableStreams) {
                        if (streamContainer.consecutivelyStreams(wfStream1, wfStream2) && wfStream1 != wfStream2) {
                            filterStreams.add(wfStream2);
                        }
                    }
                }
                // 剔除那些顺序不正确的 stream
                for (WFStream stream : filterStreams) {
                    appendableStreams.remove(stream);
                }

                // now append appendableStreams to stream1
                if (!appendableStreams.isEmpty()) {
                    appending = true;
                    streamContainer.appendStreams(stream1, appendableStreams);
                }
            }
        }
    }

    private boolean isAppendable(WFStream stream1, WFStream stream2) {
        ArrayList<SWFDataNode> outputDataNodes = stream1.getStreamOutputDataNodeList();
        ArrayList<SWFDataNode> inputDataNodes = stream2.getInputDataNodeList();
        // the same stream can not be appended
        if (stream1 == stream2) {
            return false;
        }

        // 若 stream2 input 中有一个是由stream1 产生的,就可以合并。
        if (!hasIntersection(inputDataNodes,outputDataNodes)) {
            return false;
        }

        if (streamContainer.consecutivelyStreams(stream1, stream2) && !stream1.isJoined() && !stream2.isJoined()) {
            return true;
        }
        return false;
    }

    private boolean hasIntersection(ArrayList<SWFDataNode> a,ArrayList<SWFDataNode> b){
        ArrayList<SWFDataNode> list = new ArrayList<>();
        list = (ArrayList<SWFDataNode>) a.clone();
        list.retainAll(b);
        if(list.size()>0){
            return  true;
        }else {
            return false;
        }
    }

}
