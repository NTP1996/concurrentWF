package parallelzation.graph;

import parallelzation.node.SWFDataNode;
import parallelzation.node.SWFFlowNode;
import parallelzation.node.SWFTaskNode;

import java.util.*;

/**
 * @author zhoubowen
 * @date 2020/5/14 11:40
 */
public class WFStreamContainer {

    ArrayList<WFStream> streamList = new ArrayList<>();
    Integer streamCont = 0;
    public void addStream(WFStream stream){
        //若 stream 为空则跳过加入
        stream.setCaseId(this.streamCont.toString());
        ArrayList<SWFDataNode> streamInoput  = new ArrayList<>();
        streamList.add(stream);
        streamCont+=1;
    }

    public ArrayList<WFStream> getStreamList() {
        return streamList;
    }

    public void removeEmpty(){
        Iterator<WFStream>  iterator = streamList.iterator();
        while (iterator.hasNext()){
            WFStream stream = iterator.next();
            if(stream.getTaskNodeList().size()== 0){
                iterator.remove();
            }
        }
    }
    public void print(){
        for(WFStream stream: streamList){
            stream.print();
        }
    }

    public LinkedList<WFStream> getMergableWFStreams() {
        LinkedList<WFStream> mergableWfStreams = new LinkedList<WFStream>();

        for (WFStream stream : streamList) {
            if (!stream.isProducerStream() ) {
                mergableWfStreams.add(stream);
            }
        }

        return mergableWfStreams;
    }

    public boolean consecutivelyStreams(WFStream preStream,
                                        WFStream postStream) {

        //todo 这里get(0)很有可能有问题
        Set<WFStream> preStreams = getAllPreStreams(postStream
                .getTaskNodeList().get(0));

        if (preStreams.contains(preStream)) {
            return true;
        } else {
            return false;
        }
    }


    private Set<WFStream> getAllPreStreams(SWFFlowNode sequenceNode) {
        Set<WFStream> preStreams = new HashSet<WFStream>();

        // all pre streams found: return
        if (sequenceNode.getallPreviousNodes().size() == 0) {
            if (sequenceNode instanceof SWFTaskNode) {
                for (WFStream prevStream : getWFStreams((SWFTaskNode) sequenceNode)) {
                    preStreams.add(prevStream);
                }
            }
            return preStreams;
        }
        // transitively extract further preStreams for all found preStreams
        for (SWFFlowNode prevNode : sequenceNode.getallPreviousNodes()) {
            Set<WFStream> nodes = getAllPreStreams(prevNode);
            for (WFStream node : nodes) {
                preStreams.add(node);
            }
        }
        if (sequenceNode instanceof SWFTaskNode) {
            for (WFStream prevStream : getWFStreams((SWFTaskNode) sequenceNode)) {
                preStreams.add(prevStream);
            }
        }
        return preStreams;
    }
    

    public LinkedList<WFStream> getWFStreams(SWFTaskNode taskNode) {
        LinkedList<WFStream> wfStreams = new LinkedList<WFStream>();

        for (WFStream stream : streamList) {
            if (stream.containTaskNode(taskNode)) {
                wfStreams.add(stream);
            }
        }
        return wfStreams;
    }

    public void appendStreams(WFStream preStream,
                              LinkedList<WFStream> appendableStreams) {

        // 将 postStream 加入到 preStream 前(preStream-->postStream)
        preStream.clearStreamOutput();
//        preStream.addSplitAfterTask(preStream.getLastTaskNode());  这里可能有问题
        for (WFStream appendStream : appendableStreams) {
            //append mergeStream to stream and update all stream information
            for (SWFTaskNode task : appendStream.getTaskNodeList()) {
                preStream.addTaskNode(task);
            }
            for (SWFDataNode dataNode : appendStream.getStreamInputDataNodeList()) {
                preStream.addInputDataNode(dataNode);
            }
            for (SWFDataNode dataNode : appendStream.getOutputDataNodeList()) {
                preStream.addOutputDataNode(dataNode);
            }

            for (SWFDataNode dataNode : appendStream.getStreamOutputDataNodeList()) {
                preStream.addStreamOutputDataNode(dataNode);
            }

            if(appendStream.isConsumerStream()) {
                preStream.setIsConsumerStream(true);
            }
            if(appendStream.isProducerStream()) {
                preStream.setIsProducerStream(true);
            }

            appendStream.setJoined(true);
            remove(appendStream);
        }

    }
    public void remove(WFStream stream) {
        this.streamList.remove(stream);
    }
}
