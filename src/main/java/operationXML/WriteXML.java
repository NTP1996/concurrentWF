package operationXML;



import parallelzation.graph.SWFNewGraph;
import parallelzation.node.*;
import util.ControlNodeType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

/**
 * @author kuangzengxiong
 * @date 2019/5/17 13:38
 */
public class WriteXML {
    private SWFNewGraph newGraph;
    private ArrayList<SWFDataNode> dataNodeList;
    private ArrayList<SWFTaskNode> taskNodeList;
    private SWFStartNode swfStartNode;
    private SWFEndNode swfEndNode;
    private int sequenceId = 0;
    private int controlId = 0;
    private String fileName="";
    private HashMap<SWFFlowNode,Integer> nodeLevel;
    private HashMap<String,SWFControlNode> joinNodeMap;

    public WriteXML(SWFNewGraph newGraph) {
        this.newGraph = newGraph;
        dataNodeList = newGraph.getDataNodeList();
        taskNodeList = newGraph.getTaskNodeList();
        swfStartNode = newGraph.getStartNode();
        swfEndNode = newGraph.getEndNode();

        joinNodeMap=new HashMap<>();

        //设置节点级别，用来判断循环节点的分支，
        nodeLevel=new HashMap<>();
        Stack<SWFFlowNode> nodeStack=new Stack<>();
        nodeStack.push(swfStartNode.getSuccessorList().get(0));
        nodeLevel.put(swfStartNode.getSuccessorList().get(0),0);
        while (!nodeStack.isEmpty()){
            //出栈
            SWFFlowNode nowNode=nodeStack.pop();
            if(nowNode instanceof SWFControlNode){
                if(((SWFControlNode) nowNode).getOneType().equals(ControlNodeType.JOIN)){
                    joinNodeMap.put(nowNode.getNodeDescp(),(SWFControlNode) nowNode);
                }
            }
            //获取节点级别
            int level=nodeLevel.get(nowNode);
            //获取后继节点集合
            ArrayList<SWFFlowNode> successorNodeList=nowNode.getSuccessorList();
            for(SWFFlowNode node:successorNodeList){
                if(node instanceof SWFEndNode){
                    continue;
                }
                if(nodeLevel.getOrDefault(node,-1)==-1){//如果该节点没有设置级别
                    nodeLevel.put(node,level+1);
                    nodeStack.push(node);
                }
            }
        }


    }

    public void setFileName(String fileName){
        this.fileName=fileName;
    }

    public void write() {
        String documents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<cdop:ObjectPool xmlns:cdol=\"http://cake.wi2.uni-trier.de/xml/cdol\" " +
                "xmlns:cdop=\"http://cake.wi2.uni-trier.de/xml/cdop\" " +
                "xmlns:rwfl=\"http://cake.wi2.uni-trier.de/xml/rwfl\" " +
                "xmlns:nest=\"http://cake.wi2.uni-trier.de/xml/nest\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";

        documents += "<rwfl:Workflow refID=\""+newGraph.getCaseId()+"\">\n";
        documents += "<rwfl:DataObjects>\n";
        for (SWFDataNode node : dataNodeList) {
            documents += writeDataNode(node.getNodeId(), node.getNodeDescp());
        }
        documents += "</rwfl:DataObjects>\n";

        documents += "<rwfl:Sequence refID=\"" + sequenceId + "\" status=\"READY\">\n";
        sequenceId++;

        Stack<SWFFlowNode> nodeStack = new Stack<>();
        nodeStack.push(swfStartNode.getSuccessorList().get(0));

        while (!nodeStack.isEmpty()) {
            SWFFlowNode nowNode = nodeStack.pop();
            if (nowNode instanceof SWFTaskNode) {
                documents += writeTaskNode((SWFTaskNode) nowNode);
                for (SWFFlowNode node : nowNode.getSuccessorList()) {
                    nodeStack.push(node);
                }
            } else if (nowNode instanceof SWFControlNode) {
                if (((SWFControlNode) nowNode).getOneType().equals(ControlNodeType.SPLIT)) {
                    ArrayList<SWFFlowNode> endNodeList = new ArrayList<>();
                    documents += writeSequence((SWFControlNode) nowNode, endNodeList);
                    documents+="</rwfl:Node>";
                    nodeStack.push(endNodeList.get(0));
                }
            }
        }

        documents+="</rwfl:Sequence>";
        documents+="</rwfl:Workflow>";
        documents+="</cdop:ObjectPool>";

        try{
//            String resourcePath="src\\main\\java";
            File writeName=new File(fileName);
            writeName.createNewFile();
            FileWriter writer=new FileWriter(writeName);
            BufferedWriter out=new BufferedWriter(writer);
            out.write(documents);
            out.flush();
            out.close();
            writer.close();
        }catch (IOException e){
            e.printStackTrace();
            return ;
        }

    }

    private String writeSequence(SWFControlNode startNode, ArrayList<SWFFlowNode> endNode) {
        String xmlTxt="";
        if(startNode.getPairType().equals(ControlNodeType.AND)){
            xmlTxt = "<rwfl:Node refID=\"" + controlId + "\" type=\"AND\" status=\"READY\">";
        }else if(startNode.getPairType().equals(ControlNodeType.XOR)){
            xmlTxt = "<rwfl:Node refID=\"" + controlId + "\" type=\"XOR\" status=\"READY\">";

        }else if(startNode.getPairType().equals(ControlNodeType.LOOP)){
            xmlTxt = "<rwfl:Node refID=\"" + controlId + "\" type=\"LOOP\" status=\"READY\">";

        }
        controlId++;

        xmlTxt += "<rwfl:Sequence refID=\"" + sequenceId + "\" status=\"READY\">";
        sequenceId++;
        Stack<SWFFlowNode> nodeStack = new Stack<>();
        for (SWFFlowNode node : startNode.getSuccessorList()) {
            nodeStack.push(node);
        }

        while (!nodeStack.isEmpty()) {
            SWFFlowNode nowNode = nodeStack.pop();
            if (nowNode instanceof SWFTaskNode) {
                xmlTxt += writeTaskNode((SWFTaskNode) nowNode);
                for (SWFFlowNode node : nowNode.getSuccessorList()) {
                    nodeStack.push(node);
                }
            } else if (nowNode instanceof SWFControlNode) {
                if (((SWFControlNode) nowNode).getOneType().equals(ControlNodeType.SPLIT)) {
                    if(((SWFControlNode) nowNode).getPairType().equals(ControlNodeType.AND) || ((SWFControlNode) nowNode).getPairType().equals(ControlNodeType.XOR)){
                        ArrayList<SWFFlowNode> endNodeList = new ArrayList<>();
                        xmlTxt += writeSequence((SWFControlNode) nowNode, endNodeList);
                        xmlTxt+="</rwfl:Node>";
                        nodeStack.push(endNodeList.get(0));
                    }else if(((SWFControlNode) nowNode).getPairType().equals(ControlNodeType.LOOP)){
                        ArrayList<SWFFlowNode> endNodeList=new ArrayList<>();
                        xmlTxt += writeSequence((SWFControlNode) nowNode, endNodeList);
                        xmlTxt+="</rwfl:Node>";
                        nodeStack.push(endNodeList.get(0));
                    }

                } else if (((SWFControlNode) nowNode).getOneType().equals(ControlNodeType.JOIN)) {
                    if(((SWFControlNode) nowNode).getPairType().equals(ControlNodeType.AND) || ((SWFControlNode) nowNode).getPairType().equals(ControlNodeType.XOR)){
                        if(endNode.size()==0){
                            endNode.add(nowNode.getSuccessorList().get(0));
                        }
                        xmlTxt += "</rwfl:Sequence>";
                        if(!nodeStack.isEmpty()){
                            xmlTxt += "<rwfl:Sequence refID=\"" + sequenceId + "\" status=\"READY\">";
                            sequenceId++;
                        }
                    }else if(((SWFControlNode) nowNode).getPairType().equals(ControlNodeType.LOOP)){
                        SWFFlowNode result = judgeBranch((SWFControlNode) nowNode,startNode,nodeLevel);
                        //循环节点对的下一个节点
                        SWFFlowNode loopBranch=null;
                        for (SWFFlowNode n:nowNode.getSuccessorList()){
                            if(!n.equals(result)){
                                endNode.add(n);
                            }else {
                                loopBranch=n;
                            }
                        }
                        xmlTxt += "</rwfl:Sequence>";
                        if(loopBranch!=null){
                            xmlTxt+=writeReSequence(loopBranch,startNode);

                        }

                    }
                    continue;
                }
            }

        }


        return xmlTxt;
    }


    private String writeReSequence(SWFFlowNode startNode, SWFControlNode splitNode) {
        String xmlTxt="";

        xmlTxt += "<rwfl:Sequence refID=\"" + sequenceId + "\" status=\"READY\">";
        sequenceId++;

        xmlTxt+="<rwfl:Reverse>";
        if(startNode.equals(splitNode)){
            xmlTxt+="<rwfl:Null>";
            xmlTxt+="</rwfl:Null>";
            xmlTxt+="</rwfl:Reverse>";
            xmlTxt += "</rwfl:Sequence>";
            return xmlTxt;
        }



        Stack<SWFFlowNode> nodeStack = new Stack<>();
        nodeStack.push(startNode);

        while (!nodeStack.isEmpty()) {
            SWFFlowNode nowNode = nodeStack.pop();
            if (nowNode instanceof SWFTaskNode) {
                xmlTxt += writeTaskNode((SWFTaskNode) nowNode);
                for (SWFFlowNode node : nowNode.getSuccessorList()) {
                    nodeStack.push(node);
                }
            } else if (nowNode instanceof SWFControlNode) {
                if(nowNode.equals(splitNode)){
                    xmlTxt+="</rwfl:Reverse>";
                    xmlTxt += "</rwfl:Sequence>";
                    return xmlTxt;
                }

                if (((SWFControlNode) nowNode).getOneType().equals(ControlNodeType.SPLIT)) {
                    if(((SWFControlNode) nowNode).getPairType().equals(ControlNodeType.AND) || ((SWFControlNode) nowNode).getPairType().equals(ControlNodeType.XOR)){
                        ArrayList<SWFFlowNode> endNodeList = new ArrayList<>();
                        xmlTxt += writeSequence((SWFControlNode) nowNode, endNodeList);
                        xmlTxt+="</rwfl:Node>";
                        nodeStack.push(endNodeList.get(0));
                    }else if(((SWFControlNode) nowNode).getPairType().equals(ControlNodeType.LOOP)){
                        ArrayList<SWFFlowNode> endNodeList=new ArrayList<>();
                        xmlTxt += writeSequence((SWFControlNode) nowNode, endNodeList);
                        xmlTxt+="</rwfl:Node>";
                        nodeStack.push(endNodeList.get(0));
                    }

                }
            }

        }

        return xmlTxt;
    }



    //生成数据节点的xml
    private String writeDataNode(String redId, String name) {
        String xmlTxt = "<rwfl:DataflowWrapper refID=\"" + redId + "\">\n" +
                "\t\t\t\t<rwfl:DataflowElement>\n" +
                "\t\t\t\t\t<cdol:Agg c=\"DataflowElement\">\n" +
                "\t\t\t\t\t\t<cdol:AA n=\"name\" v=\"" + name + "\"/>\n" +
                "\t\t\t\t\t</cdol:Agg>\n" +
                "\t\t\t\t</rwfl:DataflowElement>\n" +
                "\t\t\t</rwfl:DataflowWrapper>\n";
        return xmlTxt;
    }


    //生成任务节点的xml
    private String writeTaskNode(SWFTaskNode taskNode) {
        String name = taskNode.getNodeDescp();
        String refId = taskNode.getNodeId();

        String xmlTxt = "<rwfl:Task refID=\"" + refId + "\" type=\"WORKLISTTASK\" status=\"READY\">" +
                "<rwfl:SemanticDescription>" +
                "<cdol:Agg c=\"TaskSemantic\">" +
                "<cdol:AA n=\"name\" v=\"" + name + "\"/>" +
                "</cdol:Agg>" +
                "</rwfl:SemanticDescription>";

        ArrayList<SWFDataNode> inputData = taskNode.getInputNodeList();

        xmlTxt += "<rwfl:input>";
        for (SWFDataNode node : inputData) {
            int index = dataNodeList.indexOf(node);
            xmlTxt += "<rwfl:DataRef refID=\"" + index + "\"/>";
        }

        xmlTxt += "</rwfl:input>";
        xmlTxt += "<rwfl:output>";

        for (SWFDataNode node : taskNode.getOutputNodeList()) {
            int index = dataNodeList.indexOf(node);
            xmlTxt += "<rwfl:DataRef refID=\"" + index + "\"/>";
        }
        xmlTxt += "</rwfl:output>";
        xmlTxt += "</rwfl:Task>";

        return xmlTxt;
    }


    //判断循环分支
    private SWFFlowNode judgeBranch(SWFControlNode joinNode,SWFControlNode splitNode,
                                    HashMap<SWFFlowNode,Integer> nodeLevel){
        for(SWFFlowNode node:joinNode.getSuccessorList()){
            if(node.equals(splitNode)){
                return node;
            }
            Stack<SWFFlowNode> nodeStack=new Stack<>();
            nodeStack.push(node);
            HashSet<SWFFlowNode> nodeSet=new HashSet<>();
            nodeSet.add(node);
            while (!nodeStack.isEmpty()){
                SWFFlowNode nowNode=nodeStack.pop();
                ArrayList<SWFFlowNode> successorList=nowNode.getSuccessorList();
                for(SWFFlowNode n:successorList){
                    //如果该级别大于循环节点级别
                    if(n.equals(splitNode) && nodeLevel.get(nowNode)>nodeLevel.get(n)){
                        return node;
                    }
                    if(!nodeSet.contains(n)){
                        nodeSet.add(n);
                        nodeStack.push(n);
                    }
                }

            }
        }
        return null;
    }

}
