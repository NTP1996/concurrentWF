import operationXML.ParseSWFXML;

import parallelzation.graph.*;
import parallelzation.node.SWFFlowNode;

public class readGraph {
    public static void main(String[] args) {
        //Baked_Spaghetti-SW-B01
        //Fettuccine_Primavera-SW02
        //SWB02改
        SWFGraph swf = ParseSWFXML.getSWF("C:/Users/NTP/Desktop/workspace/IDEA/SWPR/测试用例/SWB02改.xml");

        SWFBuildGraph swfbuild = new SWFBuildGraph(swf);
        SWFNewGraph g = swfbuild.build();
        WFStreamBuilder streamBuilder  = new WFStreamBuilder();
        WFStreamContainer wfStreamContainer = streamBuilder.building(g);
        wfStreamContainer.print();



        //测试 获得所有前序是否有效
//        SWFFlowNode endnode = g.getEndNode();
//        for(SWFFlowNode node: endnode.getallPreviousNodes()){
//            System.out.println(node.getNodeDescp());
//        }



        //测试 startnode 是否有效
//        SWFFlowNode startnode = g.getStartNode();
//        System.out.println(startnode.getSuccessorList().size());



    }

}
