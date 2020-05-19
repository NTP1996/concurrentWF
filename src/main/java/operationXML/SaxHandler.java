
package operationXML;

/**
 * @author LiTao
 * @date 2019/5/17 13:38
 */

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import parallelzation.edge.SWFControlFlowEdge;
import parallelzation.edge.SWFDataFlowEdge;
import parallelzation.edge.SWFEdge;
import parallelzation.graph.SWFGraph;
import parallelzation.node.*;
import util.ControlNodeType;
import util.MyUUID;
import util.NodeType;


import java.util.ArrayList;


public class SaxHandler extends DefaultHandler {
    private int data=0;
    private int task=0;
    private int input=0;
    private int output=0;
    private int node=0;
    private int sequence=0;
    private int reverse=0;//标记循环结点的反向路径
    private int nullnm =0;//标记循环结点的控制流结点直接反向相连
    private int frist=0;//标记每一条路径的起点
    private int fiid=0;//多路结束的任务点标记位
    private int frid=0;//多路发起点的id
    private int start=0;
    private int and=0;//标记并行的出现
    private int xor=0;//标记异或的出现
    private int loop=0;//标记循环结点的出现
    private String caseID;
    private String taskid;
    private String taskstring;
    private SWFTaskNode tasknode;
    private ArrayList <SWFDataNode> datalist;
    private ArrayList <SWFDataNode> inputlist;
    private ArrayList <SWFDataNode> outputlist;
    private ArrayList <SWFTaskNode> taskList;
    private ArrayList <SWFTaskNode> TaskList;
    private ArrayList <SWFTaskNode> frtaskList1;
    private ArrayList <SWFTaskNode> fitaskList1;//存取多路每一条路径的最后一个结点
    private ArrayList <SWFTaskNode> frtaskList2;//存取多路每一条路径的第一个结点
    private ArrayList <SWFTaskNode> fitaskList2;
    private ArrayList <SWFTaskNode> frtaskList3;//存取多路每一条路径的第一个结点
    private ArrayList <SWFTaskNode> fitaskList3;
    private ArrayList <SWFControlFlowEdge> cfEdgeList;
    private ArrayList <SWFDataFlowEdge> dfEdgeList;
    private ArrayList <SWFNode> allNodeList;
    private ArrayList <SWFEdge> allEdgeList;
    private String Nodeid;
    private SWFControlNode andSplit;
    private SWFControlNode andjoin;
    private SWFControlNode xorSplit;
    private SWFControlNode xorjoin;
    private SWFControlNode loopSplit;
    private SWFControlNode loopjoin;
    private ArrayList <SWFControlNode> andSplitlist;
    private ArrayList <SWFControlNode> andjoinlist;
    private ArrayList <SWFControlNode> xorSplitlist;
    private ArrayList <SWFControlNode> xorjoinlist;
    private SWFStartNode startNode;
    private SWFEndNode endNode;


    public SaxHandler(){
        datalist=new ArrayList<SWFDataNode>();
        inputlist=new ArrayList<SWFDataNode>();
        outputlist=new ArrayList<SWFDataNode>();
        taskList=new  ArrayList <SWFTaskNode>();
        TaskList=new ArrayList<SWFTaskNode>();
        frtaskList1=new  ArrayList <SWFTaskNode>();//存取多路每一条路径的第一个结点
        fitaskList1=new  ArrayList <SWFTaskNode>();//存取多路每一条路径的最后一个结点
        frtaskList2=new  ArrayList <SWFTaskNode>();//存取多路每一条路径的第一个结点
        fitaskList2=new  ArrayList <SWFTaskNode>();
        frtaskList3=new  ArrayList <SWFTaskNode>();//存取多路每一条路径的第一个结点
        fitaskList3=new  ArrayList <SWFTaskNode>();
        cfEdgeList=new ArrayList <SWFControlFlowEdge>();
        dfEdgeList=new ArrayList <SWFDataFlowEdge>();
        allNodeList = new ArrayList<SWFNode>();
        allEdgeList = new ArrayList<SWFEdge>();
        Nodeid= MyUUID.getUUID32();
        andSplit=new SWFControlNode(NodeType.CONTROL_NODE,Nodeid, ControlNodeType.AND,ControlNodeType.SPLIT);
        andjoin=new SWFControlNode(NodeType.CONTROL_NODE,Nodeid , ControlNodeType.AND,ControlNodeType.JOIN);
        Nodeid=MyUUID.getUUID32();
        xorSplit=new SWFControlNode(NodeType.CONTROL_NODE,Nodeid,ControlNodeType.XOR,ControlNodeType.SPLIT);
        xorjoin=new SWFControlNode(NodeType.CONTROL_NODE,Nodeid,ControlNodeType.XOR,ControlNodeType.JOIN);
        Nodeid=MyUUID.getUUID32();
        loopSplit=new SWFControlNode(NodeType.CONTROL_NODE,Nodeid,ControlNodeType.LOOP,ControlNodeType.SPLIT);
        loopjoin=new SWFControlNode(NodeType.CONTROL_NODE,Nodeid,ControlNodeType.LOOP,ControlNodeType.JOIN);
        andSplitlist=new ArrayList<SWFControlNode>();
        andjoinlist=new ArrayList<SWFControlNode>();
        xorSplitlist=new ArrayList<SWFControlNode>();
        xorjoinlist=new ArrayList<SWFControlNode>();
        startNode=new SWFStartNode(NodeType.START_NODE,"start");
        endNode=new SWFEndNode(NodeType.END_NODE,"end");
    }



    /* 此方法有三个参数
       arg0是传回来的字符数组，其包含元素内容
       arg1和arg2分别是数组的开始位置和结束位置 */
    @Override
    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        String content = new String(arg0, arg1, arg2);
        //System.out.println(content);
        super.characters(arg0, arg1, arg2);
    }

    @Override
    public  void endDocument() throws SAXException {
        //System.out.println("\n…………结束解析文档…………");

        if (fitaskList1.size() > 0) {
            if (fitaskList1.size()==1&&andjoinlist.indexOf(loopjoin)!=0){
                for (int i =0;i<andjoinlist.size()-1;i++){
                    SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(i), andjoinlist.get(i+1));
                }
                if (reverse==1){
                    int i =andjoinlist.indexOf(loopjoin);
                    if (i==1){
                        SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(i), fitaskList2.get(1));
                        cfEdgeList.add(cfe);
                        cfe = new SWFControlFlowEdge(fitaskList2.get(0),andjoinlist.get(i));
                        cfEdgeList.add(cfe);
                        reverse = reverse - 1;
                    }
                    if (i==2){
                        SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(i), fitaskList3.get(1));
                        cfEdgeList.add(cfe);
                        cfe = new SWFControlFlowEdge(fitaskList3.get(0),andjoinlist.get(i));
                        cfEdgeList.add(cfe);
                        reverse = reverse - 1;
                    }

                }
                SWFControlFlowEdge cfe = new SWFControlFlowEdge(fitaskList1.get(0), andjoinlist.get(node));
                cfEdgeList.add(cfe);
                cfe = new SWFControlFlowEdge(andjoinlist.get(node), endNode);
                cfEdgeList.add(cfe);
                fitaskList1.clear();
            }
            else {
                if (reverse == 1) {
                    SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(node), fitaskList1.get(1));
                    cfEdgeList.add(cfe);
                    cfe = new SWFControlFlowEdge(fitaskList1.get(0),andjoinlist.get(node));
                    cfEdgeList.add(cfe);
                    reverse = reverse - 1;
                }
                else {
                    for (int i = 0; i < fitaskList1.size(); i++) {
                        SWFControlFlowEdge cfe = new SWFControlFlowEdge(fitaskList1.get(i), andjoinlist.get(node));
                        cfEdgeList.add(cfe);
                    }
                }
                SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(node), endNode);
                cfEdgeList.add(cfe);
                fitaskList1.clear();
            }

        }
        else{
            if (TaskList.size()!=0){
                SWFControlFlowEdge cfe=new SWFControlFlowEdge(TaskList.get(TaskList.size()-1),endNode);
                cfEdgeList.add(cfe);
            }
        }
        allNodeList.addAll(datalist);
        allNodeList.addAll(TaskList);
        allNodeList.add(startNode);
        allNodeList.add(endNode);

        allEdgeList.addAll(cfEdgeList);
        allEdgeList.addAll(dfEdgeList);
        super.endDocument();
    }

    /* arg0是名称空间
       arg1是包含名称空间的标签，如果没有名称空间，则为空
       arg2是不包含名称空间的标签 */
    @Override
    public void endElement(String arg0, String arg1, String arg2)
            throws SAXException {
        //System.out.println("结束解析元素  " + arg2);
        if (arg2.equals("rwfl:DataflowElement")){
            data=data-1;
        }
        if (arg2.equals("rwfl:SemanticDescription")){
            task=task-1;
        }
        if (arg2.equals("rwfl:Task")){
            if (taskstring!=null&&taskstring.trim().length()!=0) {
                /* SWFTaskNode tasknode = new SWFTaskNode(NodeType.TASK_NODE,taskstring,(ArrayList<SWFDataNode>) inputlist.clone(),(ArrayList<SWFDataNode>) outputlist.clone());
                tasknode.setNodeId(taskid);
                taskList.add(tasknode);
                TaskList.add(tasknode);
                for (int i=0;i<inputlist.size();i++){
                    SWFDataFlowEdge dfe_in = new SWFDataFlowEdge(inputlist.get(i),tasknode);
                    dfEdgeList.add(dfe_in);
                }
                for (int i=0;i<outputlist.size();i++){
                    SWFDataFlowEdge dfe_out=new SWFDataFlowEdge(tasknode,outputlist.get(i));
                    dfEdgeList.add(dfe_out);
                }
                inputlist.clear();
                outputlist.clear();
                if (frist==1)
                {
                    frtaskList.add(tasknode);
                    sequence=sequence-1;
                    frist-=1;
                }
                if (fiid==1&node=1){
                    if (frid>=0){
                        SWFControlFlowEdge cfe=new SWFControlFlowEdge(taskList.get(frid),andSplit);
                        cfEdgeList.add(cfe);
                        for (int i=0;i<frtaskList.size();i++){
                            SWFControlFlowEdge cfe_1=new SWFControlFlowEdge(andSplit,frtaskList.get(i));
                            cfEdgeList.add(cfe_1);
                        }
                        for (int i=0;i<frtaskList.size();i++){
                            SWFNode frnode=frtaskList.get(i);
                            SWFNode finode=fitaskList.get(i);
                            int preid=taskList.indexOf(frnode);
                            int lateid=taskList.indexOf(finode);
                            while (preid!=lateid){
                                SWFControlFlowEdge cfe_1=new SWFControlFlowEdge(taskList.get(preid),taskList.get(preid+1));
                                cfEdgeList.add(cfe_1);
                                preid+=1;
                            }
                        }
                        int c=taskList.size();
                        for (int i = frid;i<c;i++){
                            taskList.remove(frid);
                        }
                        for (int i=0;i<taskList.size()-1;i++){
                            SWFControlFlowEdge cfe_1=new SWFControlFlowEdge(taskList.get(i),taskList.get(i+1));
                            cfEdgeList.add(cfe_1);
                        }
                        taskList.clear();
                        taskList.add(tasknode);
                    }

                    SWFControlFlowEdge cfe=new SWFControlFlowEdge(andjoin,tasknode);
                    cfEdgeList.add(cfe);
                    for (int i=0;i<fitaskList.size();i++){
                        SWFControlFlowEdge cfe_1=new SWFControlFlowEdge(fitaskList.get(i),andjoin);
                        cfEdgeList.add(cfe_1);
                    }
                    fiid-=1;
                }
                if (start==1){
                    SWFControlFlowEdge cfe=new SWFControlFlowEdge(startNode,tasknode);
                    cfEdgeList.add(cfe);
                    start-=1;
                } */
                tasknode = new SWFTaskNode(NodeType.TASK_NODE, taskstring, (ArrayList<SWFDataNode>) inputlist.clone(), (ArrayList<SWFDataNode>) outputlist.clone());
                tasknode.setNodeId(taskid);
                for (int i = 0; i < inputlist.size(); i++) {
                    SWFDataFlowEdge dfe_in = new SWFDataFlowEdge(inputlist.get(i), tasknode);
                    dfEdgeList.add(dfe_in);
                }
                for (int i = 0; i < outputlist.size(); i++) {
                    SWFDataFlowEdge dfe_out = new SWFDataFlowEdge(tasknode, outputlist.get(i));
                    dfEdgeList.add(dfe_out);
                }
                inputlist.clear();
                outputlist.clear();
                if (start == 1) {
                    if (andSplitlist.size() == 0) {
                        SWFControlFlowEdge cfe = new SWFControlFlowEdge(startNode, tasknode);
                        cfEdgeList.add(cfe);
                    }
                    if (andSplitlist.size() > 0) {
                        SWFControlFlowEdge cfe = new SWFControlFlowEdge(startNode, andSplitlist.get(0));
                        cfEdgeList.add(cfe);
                    }
                    start = start - 1;
                }
                if (node == 0) {
                    if (taskList.size() > 0 & fitaskList1.size() == 0) {
                        SWFControlFlowEdge cfe = new SWFControlFlowEdge(taskList.get(taskList.size() - 1), tasknode);
                        cfEdgeList.add(cfe);
                    }
                    if (fitaskList1.size() > 0) {
                        if (fitaskList1.size()==1&&andjoinlist.indexOf(loopjoin)!=0){
                            for (int i =0;i<andjoinlist.size()-1;i++){
                                SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(i), andjoinlist.get(i+1));
                            }
                            if (reverse==1){
                                int i =andjoinlist.indexOf(loopjoin);
                                if (i==1){
                                    SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(i), fitaskList2.get(1));
                                    cfEdgeList.add(cfe);
                                    cfe = new SWFControlFlowEdge(fitaskList2.get(0),andjoinlist.get(i));
                                    cfEdgeList.add(cfe);
                                    reverse = reverse - 1;
                                }
                                if (i==2){
                                    SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(i), fitaskList3.get(1));
                                    cfEdgeList.add(cfe);
                                    cfe = new SWFControlFlowEdge(fitaskList3.get(0),andjoinlist.get(i));
                                    cfEdgeList.add(cfe);
                                    reverse = reverse - 1;
                                }

                            }
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(fitaskList1.get(0), andjoinlist.get(node));
                            cfEdgeList.add(cfe);
                            cfe = new SWFControlFlowEdge(andjoinlist.get(node), tasknode);
                            cfEdgeList.add(cfe);
                            fitaskList1.clear();
                        }
                        else {
                            if (reverse == 1) {
                                SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(node), fitaskList1.get(1));
                                cfEdgeList.add(cfe);
                                cfe = new SWFControlFlowEdge(fitaskList1.get(0),andjoinlist.get(node));
                                cfEdgeList.add(cfe);
                                reverse = reverse - 1;
                            }
                            else {
                                for (int i = 0; i < fitaskList1.size(); i++) {
                                    SWFControlFlowEdge cfe = new SWFControlFlowEdge(fitaskList1.get(i), andjoinlist.get(node));
                                    cfEdgeList.add(cfe);
                                }
                            }
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(node), tasknode);
                            cfEdgeList.add(cfe);
                            fitaskList1.clear();
                        }

                    }
                }
                if (node == 1) {
                    if (sequence == 1) {
                        if (fitaskList2.size() == 0) {
                            if (reverse == 1) {
                                SWFControlFlowEdge cfe = new SWFControlFlowEdge(tasknode, taskList.get(taskList.size() - 1));
                                cfEdgeList.add(cfe);
                            } else {
                                SWFControlFlowEdge cfe = new SWFControlFlowEdge(taskList.get(taskList.size() - 1), tasknode);
                                cfEdgeList.add(cfe);
                            }

                        }
                        if (fitaskList2.size() > 0) {
                            if (reverse == 1) {
                                for (int i = 0; i < fitaskList2.size(); i++) {
                                    SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(node), fitaskList2.get(i));
                                    cfEdgeList.add(cfe);
                                }
                                reverse = reverse - 1;
                            } else {
                                for (int i = 0; i < fitaskList2.size(); i++) {
                                    SWFControlFlowEdge cfe = new SWFControlFlowEdge(fitaskList2.get(i), andjoinlist.get(node));
                                    cfEdgeList.add(cfe);
                                }
                            }
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(node), tasknode);
                            cfEdgeList.add(cfe);
                            fitaskList2.clear();
                        }
                    }
                    if (sequence == 2) {
                        if (taskList.size() > 0) {
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(taskList.get(taskList.size() - 1), andSplitlist.get(node - 1));
                            cfEdgeList.add(cfe);
                        }
//                        if (nullnm == 1) {
//                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(node - 1), andSplitlist.get(node - 1));
//                            cfEdgeList.add(cfe);
//                            nullnm = nullnm - 1;
//                        } else {
                        if (reverse == 1){
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(tasknode,andSplitlist.get(node - 1));
                            cfEdgeList.add(cfe);
                            frtaskList1.add(tasknode);
                        }
                        else {
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(andSplitlist.get(node - 1), tasknode);
                            cfEdgeList.add(cfe);
                            frtaskList1.add(tasknode);
                        }

//                        }
                        if (fitaskList2.size() > 0) {
                            int k = fitaskList2.size();
                            if (reverse == 1) {
                                SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(node), fitaskList2.get(1));
                                cfEdgeList.add(cfe);
                                cfe = new SWFControlFlowEdge(fitaskList2.get(0),andjoinlist.get(node));
                                cfEdgeList.add(cfe);
                                reverse = reverse - 1;
                            } else {
                                for (int i = 0; i < fitaskList2.size(); i++) {
                                    SWFControlFlowEdge cfe = new SWFControlFlowEdge(fitaskList2.get(i), andjoinlist.get(node));
                                    cfEdgeList.add(cfe);
                                }
                            }
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(node), andjoinlist.get(node - 1));
                            cfEdgeList.add(cfe);
                            fitaskList2.clear();
                        }
                        sequence = sequence - 1;
                    }
                }
                if (node == 2) {
                    if (sequence == 1) {
                        if (fitaskList3.size() == 0) {
                            if (reverse == 1) {
                                SWFControlFlowEdge cfe = new SWFControlFlowEdge(tasknode, taskList.get(taskList.size() - 1));
                                cfEdgeList.add(cfe);
                            } else {
                                SWFControlFlowEdge cfe = new SWFControlFlowEdge(taskList.get(taskList.size() - 1), tasknode);
                                cfEdgeList.add(cfe);
                            }
                        }
                        if (fitaskList3.size() > 0) {
                            if (reverse == 1) {
                                for (int i = 0; i < fitaskList3.size(); i++) {
                                    SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(node), fitaskList3.get(i));
                                    cfEdgeList.add(cfe);

                                }
                                reverse = reverse - 1;
                            } else {
                                for (int i = 0; i < fitaskList3.size(); i++) {
                                    SWFControlFlowEdge cfe = new SWFControlFlowEdge(fitaskList3.get(i), andjoinlist.get(node));
                                    cfEdgeList.add(cfe);
                                }
                            }
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(node), tasknode);
                            cfEdgeList.add(cfe);
                            fitaskList3.clear();
                        }
                    }
                    if (sequence == 2) {
                        if (taskList.size() > 0) {
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(taskList.get(taskList.size() - 1), andSplitlist.get(node - 1));
                            cfEdgeList.add(cfe);
                        }
                        if (TaskList.size() == 0) {
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(andSplitlist.get(node - 2), andSplitlist.get(node - 1));
                            cfEdgeList.add(cfe);
                        }
//                        if (nullnm == 1) {
//                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(node - 1), andSplitlist.get(node - 1));
//                            cfEdgeList.add(cfe);
//                            nullnm = nullnm - 1;
//                        } else {
                        if (reverse == 1){
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(tasknode,andSplitlist.get(node - 1));
                            cfEdgeList.add(cfe);
                            frtaskList2.add(tasknode);
                        }
                        else {
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(andSplitlist.get(node - 1), tasknode);
                            cfEdgeList.add(cfe);
                            frtaskList2.add(tasknode);
                        }
//                        }
                        if (fitaskList3.size() > 0) {
                            if (reverse == 1) {
                                SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(node), fitaskList3.get(1));
                                cfEdgeList.add(cfe);
                                cfe = new SWFControlFlowEdge(fitaskList3.get(0),andjoinlist.get(node));
                                cfEdgeList.add(cfe);
                                reverse = reverse - 1;
                            } else {
                                for (int i = 0; i < fitaskList3.size(); i++) {
                                    SWFControlFlowEdge cfe = new SWFControlFlowEdge(fitaskList3.get(i), andjoinlist.get(node));
                                    cfEdgeList.add(cfe);
                                }
                            }
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(node), andjoinlist.get(node - 1));
                            cfEdgeList.add(cfe);
                            fitaskList3.clear();
                        }
                        sequence = sequence - 1;
                    }

                }
                if (node == 3) {
                    if (sequence == 1) {
                        if (reverse == 1) {
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(tasknode, taskList.get(taskList.size() - 1));
                            cfEdgeList.add(cfe);
                        } else {
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(taskList.get(taskList.size() - 1), tasknode);
                            cfEdgeList.add(cfe);
                        }
                    }
                    if (sequence == 2) {
                        if (taskList.size() > 0) {
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(taskList.get(taskList.size() - 1), andSplitlist.get(node - 1));
                            cfEdgeList.add(cfe);
                        }
                        if (TaskList.size() == 0) {
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(andSplitlist.get(node - 3), andSplitlist.get(node - 2));
                            cfEdgeList.add(cfe);
                            cfe = new SWFControlFlowEdge(andSplitlist.get(node - 2), andSplitlist.get(node - 1));
                            cfEdgeList.add(cfe);
                        }
//                        if (nullnm == 1) {
//                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(node - 1), andSplitlist.get(node - 1));
//                            cfEdgeList.add(cfe);
//                            nullnm = nullnm - 1;
//                        } else {
                        if (reverse == 1){
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(tasknode,andSplitlist.get(node - 1));
                            cfEdgeList.add(cfe);
                            frtaskList3.add(tasknode);
                        }
                        else {
                            SWFControlFlowEdge cfe = new SWFControlFlowEdge(andSplitlist.get(node - 1), tasknode);
                            cfEdgeList.add(cfe);
                            frtaskList3.add(tasknode);
                        }

//                        }
                        sequence = sequence - 1;
                    }

                }

                taskList.add(tasknode);
                TaskList.add(tasknode);
            }

        }
        if (arg2.equals("rwfl:input")){
            input=input-1;
        }
        if (arg2.equals("rwfl:output")){
            output=output-1;
        }
        if (arg2.equals("rwfl:Sequence")&&node>0){
            if(taskList.size()!=0) {
                SWFTaskNode tasknode = taskList.get(taskList.size() - 1);
                if (node == 1) {
                    fitaskList1.add(tasknode);
                }
                if (node == 2) {
                    fitaskList2.add(tasknode);
                }
                if (node == 3) {
                    fitaskList3.add(tasknode);
                }
                taskList.clear();
            }
        }
        if (arg2.equals("rwfl:Node")){
            node=node-1;
            if(and>1){
                and=and-1;
            }
            if(xor>1){
                xor=xor-1;
            }
            if(loop>1){
                loop=loop-1;
            }
        }
//        if (arg2.equals("rwfl:Reverse")) {
//            reverse=reverse-1;
//        }
        super.endElement(arg0, arg1, arg2);
    }

    @Override
    public void startDocument() throws SAXException {
        //System.out.println("…………开始解析文档…………\n");
        start+=1;
        super.startDocument();
    }

    /*arg0是名称空间
      arg1是包含名称空间的标签，如果没有名称空间，则为空
      arg2是不包含名称空间的标签
      arg3很明显是属性的集合 */
    @Override
    public void startElement(String arg0, String arg1, String arg2,
                             Attributes arg3) throws SAXException {
        if (arg2.equals("rwfl:DataflowWrapper")){
            data=data+1;
            if (arg3 != null){
//                System.out.println("refId:"+arg3.getValue(0));
            }
        }
        if (arg2.equals("rwfl:Workflow")){
            if (arg3 != null){
                caseID=arg3.getValue(0);
            }
        }
        if (arg2.equals("rwfl:SemanticDescription")){
            task=task+1;
        }
        if (arg2.equals("rwfl:input")){
            input=input+1;
        }
        if (arg2.equals("rwfl:output")){
            output=output+1;
        }
        if (arg2.equals("rwfl:Node")){

            frid=taskList.size()-1;
            node=node+1;
            if(sequence<2){
                sequence=sequence+1;
            }
            if (arg3 != null){
                String type=arg3.getValue(1);
                if(type.equals("AND")){
                    Nodeid=MyUUID.getUUID32();
                    andSplit=new SWFControlNode(NodeType.CONTROL_NODE,Nodeid, ControlNodeType.AND,ControlNodeType.SPLIT);
                    andjoin=new SWFControlNode(NodeType.CONTROL_NODE,Nodeid , ControlNodeType.AND,ControlNodeType.JOIN);
                    allNodeList.add(andSplit);
                    allNodeList.add(andjoin);
                    andSplitlist.add(andSplit);
                    andjoinlist.add(andjoin);
                    and=and+1;
                }
                if(type.equals("XOR")){
                    Nodeid=MyUUID.getUUID32();
                    xorSplit=new SWFControlNode(NodeType.CONTROL_NODE,Nodeid,ControlNodeType.XOR,ControlNodeType.SPLIT);
                    xorjoin=new SWFControlNode(NodeType.CONTROL_NODE,Nodeid,ControlNodeType.XOR,ControlNodeType.JOIN);
                    allNodeList.add(xorSplit);
                    allNodeList.add(xorjoin);
                    andSplitlist.add(xorSplit);
                    andjoinlist.add(xorjoin);
                    xor=xor+1;
                }
                if(type.equals("LOOP")){
                    Nodeid=MyUUID.getUUID32();
                    loopSplit=new SWFControlNode(NodeType.CONTROL_NODE,Nodeid,ControlNodeType.LOOP,ControlNodeType.SPLIT);
                    loopjoin=new SWFControlNode(NodeType.CONTROL_NODE,Nodeid,ControlNodeType.LOOP,ControlNodeType.JOIN);
                    allNodeList.add(loopSplit);
                    allNodeList.add(loopjoin);
                    andSplitlist.add(loopSplit);
                    andjoinlist.add(loopjoin);
                    loop=loop+1;
                }
            }
        }
        if (arg2.equals("rwfl:Sequence")){
            if (node>=1&sequence<2){
                sequence=sequence+1;
            }
        }
        if (arg2.equals("rwfl:Reverse")){
            reverse=reverse+1;
        }
        if (arg2.equals("rwfl:Null")){
            SWFControlFlowEdge cfe = new SWFControlFlowEdge(andjoinlist.get(node - 1), andSplitlist.get(node - 1));
            cfEdgeList.add(cfe);
            reverse=reverse-1;
        }
        if (arg2.equals("rwfl:Task")) {
            if (arg3 != null){
                taskid=arg3.getValue(0);
            }
        }
        if (arg2.equals("rwfl:Task")&sequence==2&node==1){
            frist=frist+1;
        }
        if (data!=0) {
            if (arg2.equals("cdol:AA")) {
                //System.out.println("开始解析元素 " + arg2);
                if (arg3 != null) {
                    if (arg3.getValue(0).equals("name")) {
                        // getQName()是获取属性名称，
                        String datastring=arg3.getValue(1);
                        SWFDataNode datanode=new SWFDataNode(NodeType.DATA_NODE,datastring);
                        datanode.setNodeId(String.valueOf(datalist.size()));
                        datalist.add(datanode);
//                        System.out.println("data:"+arg3.getValue(1));
                    }
                }
            }
        }
        if (task!=0) {
            if (arg2.equals("cdol:AA")) {
                //System.out.println("开始解析元素 " + arg2);
                if (arg3 != null) {
                    if (arg3.getValue(0).equals("name")) {
                        // getQName()是获取属性名称，
                        taskstring=arg3.getValue(1);
//                        System.out.println("task:"+arg3.getValue(1));
                    }
                }
            }
        }
        if (input!=0) {
            if (arg2.equals("rwfl:DataRef")) {
                //System.out.println("开始解析元素 " + arg2);
                if (arg3 != null) {

                    // getQName()是获取属性名称，
                    String inputstring=arg3.getValue(0);
                    int inputnumber=Integer.parseInt(inputstring);
                    inputlist.add(datalist.get(inputnumber));
//                    System.out.println("input:"+arg3.getValue(0));
                }
            }
        }
        if (output!=0) {
            if (arg2.equals("rwfl:DataRef")) {
                //System.out.println("开始解析元素 " + arg2);
                if (arg3 != null) {
                    // getQName()是获取属性名称，
                    String outputstring=arg3.getValue(0);
                    int outputnumber=Integer.parseInt(outputstring);
                    outputlist.add(datalist.get(outputnumber));
//                    System.out.println("output"+arg3.getValue(0));
                }
            }
        }
        //System.out.print(arg2 + ":");
        super.startElement(arg0, arg1, arg2, arg3);
    }




    public SWFGraph InitGraph(){
        SWFGraph swf1=new SWFGraph(allNodeList,allEdgeList);

        swf1.setCaseId(caseID);
        return swf1;
    }
}
