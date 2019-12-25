package parallelzation.graph;


import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import parallelzation.edge.SWFControlFlowEdge;
import parallelzation.edge.SWFDataFlowEdge;
import parallelzation.edge.SWFEdge;
import parallelzation.node.*;
import util.ControlNodeType;
import util.MapSort;
import util.MyUUID;
import util.NodeType;


import java.util.*;

/**
 * 用于并行化重构
 * @author kuangzengxiong
 * @date 2019/4/23 13:10
 */
public class SWFBuildGraph extends BaseGraph {
    protected String caseId;
    //起始节点
    protected SWFStartNode swfStartNode;
    //结束节点
    protected SWFEndNode swfEndNode;

    //任务节点集合
    protected ArrayList<SWFTaskNode> swfTaskNodeList;
    //数据节点集合
    protected ArrayList<SWFDataNode> swfDataNodeList;
    //split控制流节点集合
    protected ArrayList<SWFControlNode> splitControlNodeList;
    //join控制流节点集合
    protected ArrayList<SWFControlNode> joinControlNodeList;
    //块节点集合
    protected ArrayList<SWFPairControlNode> blockNodeList;
    //用来记录矩阵下标的节点集合
    protected ArrayList<SWFFlowNode> matrixNodeList;

    //split节点的编号，和节点的映射
    protected HashMap<String, SWFControlNode> joinNodeMap;

    //控制流边集合
    protected ArrayList<SWFControlFlowEdge> swfControlEdgeList;
    //数据输入、输出边集合
    protected ArrayList<SWFDataFlowEdge> swfDataFlowEdges;
//    //数据输入边集合
//    protected ArrayList<SWFDataFlowInEdge> swfDataFlowInEdges;
//    //数据输出边集合
//    protected ArrayList<SWFDataFlowOutEdge> swfDataFlowOutEdges;


    //开始节点一维关系矩阵
    protected DoubleMatrix1D startModelNode;
    //结束节点一维关系矩阵
    protected DoubleMatrix1D endModelNode;
    //因果关系矩阵
    protected DoubleMatrix2D causalityRelation;
    //传递因果关系矩阵
    protected DoubleMatrix2D transmissionCausalityRelations;
    //并行关系矩阵
    protected DoubleMatrix2D parallelRelations;
    //初始化传递关系依赖矩阵
    protected DoubleMatrix2D transmissionDataDependence;
    protected DoubleMatrix2D dataDependenceCore;

    //用来记录控制流节点对的标号
    protected int staticNumber = 0;

    private int rawControlEdgeSize = 0;

    public SWFBuildGraph() {
    }



    /**
     * @param swfGraph, 构造函数，传入一个语义工作流图，解析出任务节点集合，数据节点集合，split控制流节点集合，join控制流节点集合，
     *                  控制流边集合，数据输入输出边集合，以及在控制流边集合中剔除与开始节点和结束节点相连的控制流边
     */
    public SWFBuildGraph(SWFGraph swfGraph) {
        //任务节点集合
        swfTaskNodeList = new ArrayList<>();
        //数据节点集合
        swfDataNodeList = new ArrayList<>();
        //split控制流节点集合
        splitControlNodeList = new ArrayList<>();
        //join控制流节点集合
        joinControlNodeList = new ArrayList<>();

        //split节点的编号，和节点的映射
        joinNodeMap = new HashMap<>();

        //控制流边集合
        swfControlEdgeList = new ArrayList<>();
        //数据输入、输出边集合
        swfDataFlowEdges = new ArrayList<>();
//        //数据集合
//        swfDataFlowOutEdges=new ArrayList<>();
        caseId = swfGraph.getCaseId();

        //找出图中所有任务节点、数据节点和控制流节点
        for (SWFNode swfNode : swfGraph.getNodeList()) {
            if (swfNode.getNodeType().equals(NodeType.TASK_NODE)) {//任务节点
                swfTaskNodeList.add((SWFTaskNode) swfNode);
            } else if (swfNode.getNodeType().equals(NodeType.DATA_NODE)) {//数据节点
                swfDataNodeList.add((SWFDataNode) swfNode);
            } else if (swfNode instanceof SWFControlNode) {//控制流节点
                if (((SWFControlNode) swfNode).getOneType().equals(ControlNodeType.SPLIT)) {
                    splitControlNodeList.add((SWFControlNode) swfNode);
                } else if (((SWFControlNode) swfNode).getOneType().equals(ControlNodeType.JOIN)) {
                    joinControlNodeList.add((SWFControlNode) swfNode);
                }
            } else if (swfNode instanceof SWFStartNode) {//开始节点
                swfStartNode = (SWFStartNode) swfNode;
            } else if (swfNode instanceof SWFEndNode) {//结束节点
                swfEndNode = (SWFEndNode) swfNode;
            }
        }

        //找出所有数据输入输出流边、所有控制流边
        for (SWFEdge swfEdge : swfGraph.getEdgeList()) {
            if (swfEdge instanceof SWFDataFlowEdge) {
                swfDataFlowEdges.add((SWFDataFlowEdge) swfEdge);
            } else if (swfEdge instanceof SWFControlFlowEdge) {
                swfControlEdgeList.add((SWFControlFlowEdge) swfEdge);
                rawControlEdgeSize++;
            }
        }

        //去除掉控制流边中的起始控制流边和结束控制流边
        Iterator<SWFControlFlowEdge> iterator = swfControlEdgeList.iterator();
        while (iterator.hasNext()) {
            SWFControlFlowEdge swfEdge = iterator.next();
            if (swfEdge.getLeftNode() instanceof SWFStartNode) {
                if (swfStartNode != null) {
                    swfStartNode.addSuccessorNode((SWFFlowNode) swfEdge.getRightNode());
                }
                iterator.remove();
            } else if (swfEdge.getRightNode() instanceof SWFEndNode) {
                if (swfEndNode != null) {
                    swfEndNode.addPrecursorNode((SWFFlowNode) swfEdge.getLeftNode());
                }
                iterator.remove();
            }
        }

        //通过边集合，把所有节点关系加入节点对象
        for (SWFControlFlowEdge controlEdge : swfControlEdgeList) {
            SWFFlowNode left = (SWFFlowNode) controlEdge.getLeftNode();
            SWFFlowNode right = (SWFFlowNode) controlEdge.getRightNode();
            left.addSuccessorNode(right);
            right.addPrecursorNode(left);
        }

        //控制流节点映射
        for (SWFControlNode node : joinControlNodeList) {
            joinNodeMap.put(node.getNodeDescp(), node);
        }
    }


    /**
     * @return 对输入的语义工作流进行并行化重构
     */
    public SWFNewGraph parallelize() {
        nodePairToBlock();
        initializeMatrixNodeList();
        getLogRelationsFromGraph();
        getDataDependenceFromGraph();
        updateRelations();
        getSpecialNode();
        addControlNodeToAlpha();
        SWFNewGraph newGraph = reconstitutionAlpha();
        newGraph.setCaseId(caseId);
        newGraph.setParallelRelations(parallelRelations.copy());
        return newGraph;
    }

    /**
     * @return 返回petri网
     */
    public SWFNewGraph parallelizePetri() {
        nodePairToBlock();
        initializeMatrixNodeList();
        getLogRelationsFromGraph();
        getDataDependenceFromGraph();
        updateRelations();
        getSpecialNode();
        addControlNodeToAlpha();
        SWFNewGraph newGraph = reconstitutionAlphaToJinTao();
        newGraph.setCaseId(caseId);
        newGraph.setParallelRelations(parallelRelations.copy());
        return newGraph;
    }






    /**
     * 将不是and的控制流节点对，构造成块，同时将控制流节点对中的所有任务节点与数据节点的关系，加入该块中
     */
    private void nodePairToBlock() {
        blockNodeList = new ArrayList<>();

        //设置节点级别，用来判断循环节点的分支，
        HashMap<SWFFlowNode, Integer> nodeLevel = new HashMap<>();
        setNodeLevel(nodeLevel);

        int controlNodeSize = splitControlNodeList.size();
        for (int i = controlNodeSize - 1; i >= 0; i--) {
            SWFControlNode splitNode = splitControlNodeList.get(i);
            if (splitNode.getPairType().equals(ControlNodeType.XOR)) {//处理互斥节点对
                String nodeNumber = splitNode.getNodeDescp();
                //获取split节点相对应的join节点
                SWFControlNode joinNode = joinNodeMap.get(nodeNumber);

                //创建块节点对象
                SWFPairControlNode blockNode = new SWFPairControlNode(NodeType.BLOCK_NODE, MyUUID.getUUID32(), splitNode.getPairType());
                blockNode.setNodeId(MyUUID.getUUID32());
                blockNode.setStartControlNode(splitNode);
                blockNode.setEndControlNode(joinNode);

                //修改split节点的边关系
                ArrayList<SWFFlowNode> nodeList = (ArrayList<SWFFlowNode>) splitNode.getPrecursorList().clone();

                for (SWFFlowNode node : nodeList) {
                    node.removeSuccessorNode(splitNode);
                    splitNode.removePrecursorNode(node);

                    node.addSuccessorNode(blockNode);
                    blockNode.addPrecursorNode(node);
                }

                //修改join节点边关系
                nodeList = (ArrayList<SWFFlowNode>) joinNode.getSuccessorList().clone();

                for (SWFFlowNode node : nodeList) {
                    node.removePrecursorNode(joinNode);
                    joinNode.removeSuccessorNode(node);

                    node.addPrecursorNode(blockNode);
                    blockNode.addSuccessorNode(node);
                }

                //将控制流节点对中任务节点的输入输出关系加入“块”节点中
                Set<SWFFlowNode> recordNodeList = new HashSet<>();
                Stack<SWFFlowNode> stack = new Stack<>();
                stack.push(splitNode);
                while (!stack.isEmpty()) {
                    SWFFlowNode nowNode = stack.pop();

                    if (nowNode instanceof SWFTaskNode) {//任务节点的数据依赖关系加入blockNode
                        blockNode.addAllInputNode(((SWFTaskNode) nowNode).getInputNodeList());
                        blockNode.addAllOutputNode(((SWFTaskNode) nowNode).getOutputNodeList());
                        blockNode.addInlayNode(nowNode);
                    } else if (nowNode instanceof SWFPairControlNode) {//子blockNode的数据依赖关系加入blockNode
                        blockNode.addAllInputNode(((SWFPairControlNode) nowNode).getInputNodeList());
                        blockNode.addAllOutputNode(((SWFPairControlNode) nowNode).getOutputNodeList());
                        blockNode.addInlayNode(nowNode);
                    } else if (nowNode instanceof SWFControlNode) {//子控制流节点加入blockNode中
                        if (((SWFControlNode) nowNode).getPairType().equals(ControlNodeType.AND)) {
                            if (((SWFControlNode) nowNode).getOneType().equals(ControlNodeType.SPLIT)) {
                                blockNode.addSplitNodeList((SWFControlNode) nowNode);
                            } else {
                                blockNode.addJoinNodeList((SWFControlNode) nowNode);
                            }
                        }
                    }

                    for (SWFFlowNode node : nowNode.getSuccessorList()) {
                        if (!recordNodeList.contains(node)) {
                            stack.push(node);
                            recordNodeList.add(node);
                        }
                    }
                }

                blockNodeList.add(blockNode);
            }
            else if (splitNode.getPairType().equals(ControlNodeType.LOOP)) {//处理循环节点对
                String nodeNumber = splitNode.getNodeDescp();
                //获取split节点相对应的join节点
                SWFControlNode joinNode = joinNodeMap.get(nodeNumber);

                //创建块节点对象
                SWFPairControlNode blockNode = new SWFPairControlNode(NodeType.BLOCK_NODE, MyUUID.getUUID32(), splitNode.getPairType());
                blockNode.setNodeId(MyUUID.getUUID32());
                blockNode.setStartControlNode(splitNode);
                blockNode.setEndControlNode(joinNode);

                ArrayList<SWFFlowNode> result = judgeBranch(joinNode, splitNode, nodeLevel);
                if (result != null) {
                    if (result.size() == 1) {//如果循环分支上没有节点
                        SWFFlowNode temp = null;
                        if (swfStartNode.getSuccessorList().get(0).equals(splitNode)) {
                            swfStartNode.removeSuccessorNode(splitNode);
                            swfStartNode.addSuccessorNode(blockNode);
                            blockNode.addPrecursorNode(swfStartNode);
                        }
                        for (SWFFlowNode node : splitNode.getPrecursorList()) {
                            if (!node.equals(joinNode)) {
                                temp = node;
                                node.removeSuccessorNode(splitNode);
                                node.addSuccessorNode(blockNode);
                                blockNode.addPrecursorNode(node);
                            }
                        }
                        splitNode.removePrecursorNode(temp);
                        for (SWFFlowNode node : joinNode.getSuccessorList()) {
                            if (!node.equals(splitNode)) {
                                temp = node;
                                node.removePrecursorNode(joinNode);
                                node.addPrecursorNode(blockNode);
                                blockNode.addSuccessorNode(node);
                            } else {
                                Stack<SWFFlowNode> stackNode = new Stack<>();
                                stackNode.push(node);
                                while (!stackNode.isEmpty()) {
                                    SWFFlowNode nowNode = stackNode.pop();
                                    if (nowNode.equals(splitNode)) {
                                        continue;
                                    }
                                    if (nowNode instanceof SWFTaskNode) {
                                        blockNode.addAllInputNode(((SWFTaskNode) nowNode).getInputNodeList());
                                        blockNode.addAllOutputNode(((SWFTaskNode) nowNode).getOutputNodeList());
                                    } else if (nowNode instanceof SWFPairControlNode) {
                                        blockNode.addAllInputNode(((SWFPairControlNode) nowNode).getInputNodeList());
                                        blockNode.addAllOutputNode(((SWFPairControlNode) nowNode).getOutputNodeList());
                                    }
                                    stackNode.addAll(nowNode.getSuccessorList());
                                }
                            }
                        }
                        joinNode.removeSuccessorNode(temp);
                    }
                    else if (result.size() == 2) {//如果循环分支上存在节点
                        SWFFlowNode temp = null;
                        if (swfStartNode.getSuccessorList().get(0).equals(splitNode)) {
                            swfStartNode.removeSuccessorNode(splitNode);
                            swfStartNode.addSuccessorNode(blockNode);
                            blockNode.addPrecursorNode(swfStartNode);
                        }
                        //处理split节点
                        for (SWFFlowNode node : splitNode.getPrecursorList()) {
                            if (!node.equals(result.get(1))) {
                                temp = node;
                                node.removeSuccessorNode(splitNode);
                                node.addSuccessorNode(blockNode);
                                blockNode.addPrecursorNode(node);
                            }
                        }
                        splitNode.removePrecursorNode(temp);
                        //处理join节点
                        for (SWFFlowNode node : joinNode.getSuccessorList()) {
                            if (!node.equals(result.get(0))) {
                                temp = node;
                                node.removePrecursorNode(joinNode);
                                node.addPrecursorNode(blockNode);
                                blockNode.addSuccessorNode(node);
                            } else {
                                Stack<SWFFlowNode> stackNode = new Stack<>();
                                stackNode.push(node);
                                while (!stackNode.isEmpty()) {
                                    SWFFlowNode nowNode = stackNode.pop();
                                    if (nowNode.equals(splitNode)) {
                                        continue;
                                    }
                                    if (nowNode instanceof SWFTaskNode) {
                                        blockNode.addAllInputNode(((SWFTaskNode) nowNode).getInputNodeList());
                                        blockNode.addAllOutputNode(((SWFTaskNode) nowNode).getOutputNodeList());
                                    } else if (nowNode instanceof SWFPairControlNode) {
                                        blockNode.addAllInputNode(((SWFPairControlNode) nowNode).getInputNodeList());
                                        blockNode.addAllOutputNode(((SWFPairControlNode) nowNode).getOutputNodeList());
                                    }
                                    stackNode.addAll(nowNode.getSuccessorList());
                                }
                            }
                        }
                        joinNode.removeSuccessorNode(temp);
                    }
                }

                //将控制流节点对中任务节点的输入输出关系加入“块”节点中
                Stack<SWFFlowNode> stackNode = new Stack<>();
                stackNode.push(splitNode.getSuccessorList().get(0));
                while (!stackNode.isEmpty()) {
                    SWFFlowNode nowNode = stackNode.pop();
                    if (nowNode.equals(joinNode)) {
                        continue;
                    }

                    if (nowNode instanceof SWFTaskNode) {
                        blockNode.addAllInputNode(((SWFTaskNode) nowNode).getInputNodeList());
                        blockNode.addAllOutputNode(((SWFTaskNode) nowNode).getOutputNodeList());
                    } else if (nowNode instanceof SWFPairControlNode) {
                        blockNode.addAllInputNode(((SWFPairControlNode) nowNode).getInputNodeList());
                        blockNode.addAllOutputNode(((SWFPairControlNode) nowNode).getOutputNodeList());
                    }
                    stackNode.addAll(nowNode.getSuccessorList());
                }
                blockNodeList.add(blockNode);
            }
        }
    }

    /*
     *设置节点级别
     */
    private void setNodeLevel(HashMap<SWFFlowNode, Integer> nodeLevel){
        Stack<SWFFlowNode> nodeStack = new Stack<>();
        nodeStack.push(swfStartNode.getSuccessorList().get(0));
        nodeLevel.put(swfStartNode.getSuccessorList().get(0), 0);
        while (!nodeStack.isEmpty()) {
            //出栈
            SWFFlowNode nowNode = nodeStack.pop();
            //获取节点级别
            int level = nodeLevel.get(nowNode);
            //获取后继节点集合
            ArrayList<SWFFlowNode> successorNodeList = nowNode.getSuccessorList();
            for (SWFFlowNode node : successorNodeList) {
                if (node instanceof SWFEndNode) {
                    continue;
                }
                if (nodeLevel.getOrDefault(node, -1) == -1) {//如果该节点没有设置级别
                    nodeLevel.put(node, level + 1);
                    nodeStack.push(node);
                }
            }
        }
    }

    /*
     * 判断循环分支
     */
    private ArrayList<SWFFlowNode> judgeBranch(SWFControlNode joinNode, SWFControlNode splitNode,
                                               HashMap<SWFFlowNode, Integer> nodeLevel) {
        ArrayList<SWFFlowNode> result = new ArrayList<>();

        for (SWFFlowNode node : joinNode.getSuccessorList()) {
            if (node.equals(splitNode)) {
                result.add(node);
                return result;
            }
            Stack<SWFFlowNode> nodeStack = new Stack<>();
            nodeStack.push(node);
            HashSet<SWFFlowNode> nodeSet = new HashSet<>();
            nodeSet.add(node);
            while (!nodeStack.isEmpty()) {
                SWFFlowNode nowNode = nodeStack.pop();
                ArrayList<SWFFlowNode> successorList = nowNode.getSuccessorList();
                for (SWFFlowNode n : successorList) {
                    //如果该级别大于循环节点级别
                    if (n.equals(splitNode) && nodeLevel.get(nowNode) > nodeLevel.get(n)) {
                        result.add(node);
                        result.add(nowNode);
                        return result;
                    }
                    if (!nodeSet.contains(n)) {
                        nodeSet.add(n);
                        nodeStack.push(n);
                    }
                }

            }
        }
        return null;
    }


    /**
     * 初始化矩阵下标节点集合
     */
    protected void initializeMatrixNodeList() {
        //用来记录矩阵下标的节点集合
        this.matrixNodeList = new ArrayList<>();

        ArrayList<SWFFlowNode> nodeList = swfStartNode.getSuccessorList();

        if (nodeList.size() <= 0) {
            return;
        }

        Stack<SWFFlowNode> stack = new Stack<>();
        for (SWFFlowNode node : nodeList) {
            stack.push(node);
        }

        swfControlEdgeList.clear();

        while (!stack.isEmpty()) {
            SWFFlowNode nowNode = stack.pop();
            if (!(nowNode instanceof SWFControlNode) && !matrixNodeList.contains(nowNode)) {
                matrixNodeList.add(nowNode);
            }
            for (SWFFlowNode node : nowNode.getSuccessorList()) {
                swfControlEdgeList.add(new SWFControlFlowEdge(nowNode, node));
                stack.push(node);
            }
        }
    }

    /**
     * 获取任务执行关系
     */
    protected void getLogRelationsFromGraph() {
        int matrixNodeSize = matrixNodeList.size();
        //因果关系矩阵
        causalityRelation = DoubleFactory2D.sparse.make(matrixNodeSize, matrixNodeSize, 0);
        //传递因果关系矩阵
        transmissionCausalityRelations = DoubleFactory2D.sparse.make(matrixNodeSize, matrixNodeSize, 0);
        //并行关系矩阵
        parallelRelations = DoubleFactory2D.sparse.make(matrixNodeSize, matrixNodeSize, 0);

        //加入已存在的执行关系
        for (SWFEdge swfEdge : swfControlEdgeList) {
            SWFFlowNode leftNode = (SWFFlowNode) swfEdge.getLeftNode();
            SWFFlowNode rightNode = (SWFFlowNode) swfEdge.getRightNode();
            if (matrixNodeList.contains(leftNode) && matrixNodeList.contains(rightNode)) {
                int i = matrixNodeList.indexOf(leftNode);
                int j = matrixNodeList.indexOf(rightNode);
                if (i != j) {
                    causalityRelation.set(i, j, 1);
                }
            } else if (matrixNodeList.contains(leftNode) && (rightNode instanceof SWFControlNode)) {//将任务节点和控制流节点的关系加入矩阵
                if (((SWFControlNode) rightNode).getPairType().equals(ControlNodeType.AND)) {
                    Stack<SWFFlowNode> nodeStack = new Stack<>();
                    ArrayList<SWFFlowNode> nodeList = new ArrayList<>();
                    nodeStack.push(rightNode);
                    while (!nodeStack.isEmpty()) {
                        SWFFlowNode nowNode = nodeStack.pop();
                        if (!(nowNode instanceof SWFControlNode)) {//如果不是控制流节点
                            if (!nodeList.contains(nowNode)) {
                                nodeList.add(nowNode);
                            }
                            continue;
                        }

                        for (SWFFlowNode node : nowNode.getSuccessorList()) {
                            nodeStack.push(node);
                        }
                    }
                    //设置控制流左边的任务节点和右边的任务节点的关系
                    int index1 = matrixNodeList.indexOf(leftNode);
                    for (int i = 0; i < nodeList.size(); i++) {
                        int index2 = matrixNodeList.indexOf(nodeList.get(i));
                        if (index1 != index2) {
                            causalityRelation.set(index1, index2, 1);
                        }
                    }

                }
            } else if (matrixNodeList.contains(rightNode) && (leftNode instanceof SWFControlNode)) {
                if (((SWFControlNode) leftNode).getPairType().equals(ControlNodeType.AND)) {
                    Stack<SWFFlowNode> nodeStack = new Stack<>();
                    ArrayList<SWFFlowNode> nodeList = new ArrayList<>();
                    nodeStack.push(leftNode);
                    while (!nodeStack.isEmpty()) {
                        SWFFlowNode nowNode = nodeStack.pop();
                        if (!(nowNode instanceof SWFControlNode)) {//如果不是控制流节点
                            if (!nodeList.contains(nowNode)) {
                                nodeList.add(nowNode);
                            }
                            continue;
                        }

                        for (SWFFlowNode node : nowNode.getPrecursorList()) {
                            nodeStack.push(node);
                        }
                    }
                    //设置控制流左边的任务节点和右边的任务节点的关系
                    int index2 = matrixNodeList.indexOf(rightNode);
                    for (int i = 0; i < nodeList.size(); i++) {
                        int index1 = matrixNodeList.indexOf(nodeList.get(i));
                        if (index1 != index2) {
                            causalityRelation.set(index1, index2, 1);
                        }
                    }

                }
            }
        }

        transmissionCausalityRelations.assign(causalityRelation);

        //寻找因果传递关系
        for (int i = 0; i < matrixNodeSize; i++) {
            for (int j = 0; j < matrixNodeSize; j++) {
                if (i == j) {
                    continue;
                }
                if (transmissionCausalityRelations.get(i, j) == 1) {
                    for (int k = 0; k < matrixNodeSize; k++) {
                        if (k == i || k == j) {
                            continue;
                        }
                        if (transmissionCausalityRelations.get(i, j) == 1
                                && transmissionCausalityRelations.get(j, k) == 1
                                && transmissionCausalityRelations.get(i, k) == 0) {
                            transmissionCausalityRelations.set(i, k, 1);
                        }
                    }
                }
            }
        }

        //寻找并行关系
        for (SWFControlNode node : splitControlNodeList) {
            ArrayList<SWFFlowNode> successNodeList = node.getSuccessorList();
            if (matrixNodeList.contains(successNodeList.get(0))) {//如果控制流节点，是要并行化块中的层的控制流节点
                //获取对应的join控制流节点
                String nodeDesc = node.getNodeDescp();
                SWFControlNode joinNode = joinNodeMap.get(nodeDesc);
                //设置集合
                ArrayList<ArrayList<SWFFlowNode>> pathNodeLists = new ArrayList<>();
                for (int i = 0; i < successNodeList.size(); i++) {
                    pathNodeLists.add(new ArrayList<>());
                }

                //获取各路径上的节点
                for (int i = 0; i < successNodeList.size(); i++) {
                    Stack<SWFFlowNode> nodeStack = new Stack<>();
                    nodeStack.push(successNodeList.get(i));
                    while (!nodeStack.isEmpty()) {
                        SWFFlowNode nowNode = nodeStack.pop();
                        if (nowNode.equals(joinNode)) {//如果遇到join节点，则直接进入下一次循环
                            continue;
                        }
                        if (!pathNodeLists.get(i).contains(nowNode)) {
                            pathNodeLists.get(i).add(nowNode);
                            for (SWFFlowNode n : nowNode.getSuccessorList()) {
                                nodeStack.push(n);
                            }
                        }
                    }
                }

                //将每一个条路径上的节点与另一条路径上的节点设置为并行关系
                for (int i = 0; i < successNodeList.size() - 1; i++) {
                    ArrayList<SWFFlowNode> list1 = pathNodeLists.get(i);
                    for (int j = i + 1; j < successNodeList.size(); j++) {
                        ArrayList<SWFFlowNode> list2 = pathNodeLists.get(j);
                        for (int k = 0; k < list1.size(); k++) {
                            int index1 = matrixNodeList.indexOf(list1.get(k));
                            for (int l = 0; l < list2.size(); l++) {
                                int index2 = matrixNodeList.indexOf(list2.get(l));
                                parallelRelations.set(index1, index2, 1);
                                parallelRelations.set(index2, index1, 1);
                            }
                        }
                    }
                }

            }


        }


    }


    /**
     * 获取数据依赖关系
     */
    protected void getDataDependenceFromGraph() {

        int matrixSize = matrixNodeList.size();
        transmissionDataDependence = DoubleFactory2D.sparse.make(matrixSize, matrixSize, 0);
        dataDependenceCore = DoubleFactory2D.sparse.make(matrixSize, matrixSize, 0);

        HashMap<SWFDataNode, HashSet<SWFFlowNode>> inputDependenceMap = new HashMap<>();
        HashMap<SWFDataNode, HashSet<SWFFlowNode>> outputDependenceMap = new HashMap<>();

        //初始化输入、输出节点的集合
        for (SWFDataNode dataNode : swfDataNodeList) {
            inputDependenceMap.put(dataNode, new HashSet<>());
            outputDependenceMap.put(dataNode, new HashSet<>());
        }

        //得到输入、输出节点的映射
        for (SWFFlowNode node : matrixNodeList) {
            if (node instanceof SWFTaskNode) {
                for (SWFDataNode dataNode : ((SWFTaskNode) node).getInputNodeList()) {
                    inputDependenceMap.get(dataNode).add(node);
                }
                for (SWFDataNode dataNode : ((SWFTaskNode) node).getOutputNodeList()) {
                    outputDependenceMap.get(dataNode).add(node);
                }
            } else if (node instanceof SWFPairControlNode) {
                for (SWFDataNode dataNode : ((SWFPairControlNode) node).getInputNodeList()) {
                    inputDependenceMap.get(dataNode).add(node);
                }
                for (SWFDataNode dataNode : ((SWFPairControlNode) node).getOutputNodeList()) {
                    outputDependenceMap.get(dataNode).add(node);
                }
            }


        }


        //计算输出-输入的依赖关系
        for (SWFDataNode dataNode : outputDependenceMap.keySet()) {
            SWFFlowNode[] stnint = inputDependenceMap.get(dataNode).toArray(new SWFFlowNode[0]);
            SWFFlowNode[] stnout = outputDependenceMap.get(dataNode).toArray(new SWFFlowNode[0]);

            for (int i = 0; i < stnint.length; i++) {
                for (int j = 0; j < stnout.length; j++) {
                    int x = matrixNodeList.indexOf(stnint[i]);
                    int y = matrixNodeList.indexOf(stnout[j]);

                    if (x != y) {
                        if (transmissionCausalityRelations.get(y, x) == 1) {
                            dataDependenceCore.set(y, x, 1);
                        }
                    }

                }
            }
        }


        //计算传递依赖关系，并更新关键因果依赖关系
        transmissionDataDependence.assign(dataDependenceCore);
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (i == j) {
                    continue;
                }
                if (transmissionDataDependence.get(i, j) == 1) {
                    for (int k = 0; k < matrixSize; k++) {
                        if (k == i || k == j) {
                            continue;
                        }
                        if (transmissionDataDependence.get(i, j) == 1
                                && transmissionDataDependence.get(j, k) == 1
                                && (transmissionDataDependence.get(i, k) == 0
                                || dataDependenceCore.get(i, k) == 1)) {
                            transmissionDataDependence.set(i, k, 1);
                            dataDependenceCore.set(i, k, 0);
                        }

                    }
                }
            }
        }

    }

    /**
     * 更新任务执行关系
     */
    protected void updateRelations() {
        int matrixNodeSize = causalityRelation.rows();
        for (int i = 0; i < matrixNodeSize; i++) {
            for (int j = 0; j < matrixNodeSize; j++) {
                if (i == j) {
                    continue;
                }
                //假因果关系变并行 a-->b ^ not D(a > b)
                if (causalityRelation.get(i, j) == 1 && dataDependenceCore.get(i, j) == 0) {
                    parallelRelations.set(i, j, 1);
                    parallelRelations.set(j, i, 1);
                    causalityRelation.set(i, j, 0);
                    causalityRelation.set(j, i, 0);
                    transmissionCausalityRelations.set(i, j, 0);
                    transmissionCausalityRelations.set(j, i, 0);
                }
                //假传递因果变成并行 a-->>b ^ not D(a>b) ^ not D(a>>b)
                else if (causalityRelation.get(i, j) == 0 && transmissionCausalityRelations.get(i, j) == 1
                        && transmissionDataDependence.get(i, j) == 0) {
                    parallelRelations.set(i, j, 1);
                    parallelRelations.set(j, i, 1);
                    transmissionCausalityRelations.set(i, j, 0);
                    transmissionCausalityRelations.set(j, i, 0);
                }

                //假传递因果变因果 a-->>b ^ D(a->b)
                else if (causalityRelation.get(i, j) == 0 && transmissionCausalityRelations.get(i, j) == 1
                        && dataDependenceCore.get(i, j) == 1) {
                    causalityRelation.set(i, j, 1);
                }
            }
        }

    }


    //寻找没有前驱节点的节点和没有后继节点的节点
    protected void getSpecialNode() {
        int size = causalityRelation.rows();

        startModelNode = DoubleFactory1D.sparse.make(size, 0);
        endModelNode = DoubleFactory1D.sparse.make(size, 0);

        //寻找没有前驱节点的节点和没有后继节点的节点
        for (int i = 0; i < size; i++) {
            boolean start = true, end = true;
            for (int j = 0; j < size; j++) {
                if (i == j) continue;
                if (causalityRelation.get(j, i) == 1) {//如果有前驱节点则标记
                    start = false;
                }
                if (causalityRelation.get(i, j) == 1) {//如果有后继节点则标记
                    end = false;
                }
            }
            if (start) {
                startModelNode.set(i, 1);
            }
            if (end) {
                endModelNode.set(i, 1);
            }
        }


        for (int i = 0; i < size; i++) {
            matrixNodeList.get(i).removeAllPrecursorNode(matrixNodeList.get(i).getPrecursorList());
            matrixNodeList.get(i).removeAllSuccessorNode(matrixNodeList.get(i).getSuccessorList());
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (causalityRelation.get(i, j) == 1) {
                    matrixNodeList.get(i).addSuccessorNode(matrixNodeList.get(j));
                    matrixNodeList.get(j).addPrecursorNode(matrixNodeList.get(i));
                }
            }
        }
    }

    //将并行关系变为控制流节点加入图中
    protected void addControlNodeToAlpha() {
        //清空初始化的所有控制流节点
        splitControlNodeList.clear();
        joinControlNodeList.clear();

        //从前往后遍历所使用的栈
        Stack<SWFControlNode> startToEnd = new Stack<>();
        //从后往前遍历所使用的栈
        Stack<SWFControlNode> endToStart = new Stack<>();
        //从开始到结尾的级别
        TreeMap<SWFFlowNode, Integer> startLevel = new TreeMap<>(new Comparator<SWFFlowNode>() {
            @Override
            public int compare(SWFFlowNode o1, SWFFlowNode o2) {
                if (o1 instanceof SWFControlNode && o2 instanceof SWFControlNode) {
                    return (((SWFControlNode) o1).getOneType() + o1.getNodeDescp())
                            .compareTo(((SWFControlNode) o2).getOneType() + o2.getNodeDescp());
                } else {
                    return (o1.getNodeDescp() + o1.getNodeId()).compareTo(o2.getNodeDescp() + o2.getNodeId());
                }
            }
        });
        //从结尾到开始的级别
        TreeMap<SWFFlowNode, Integer> endLevel = new TreeMap<>(new Comparator<SWFFlowNode>() {
            @Override
            public int compare(SWFFlowNode o1, SWFFlowNode o2) {
                if (o1 instanceof SWFControlNode && o2 instanceof SWFControlNode) {
                    return (((SWFControlNode) o1).getOneType() + o1.getNodeDescp())
                            .compareTo(((SWFControlNode) o2).getOneType() + o2.getNodeDescp());
                } else {
                    return (o1.getNodeDescp() + o1.getNodeId()).compareTo(o2.getNodeDescp() + o2.getNodeId());
                }

            }
        });

        Queue<SWFFlowNode> flowNodeQueue = new LinkedList<>();

        int size = startModelNode.size();

        for (int i = 0; i < size; i++) {//从开始往后遍历，采取广度优先算法
            if (startModelNode.get(i) != 1) {
                continue;
            }
            //获取没有前驱的起始任务节点
            SWFFlowNode p = matrixNodeList.get(i);
            //入队
            flowNodeQueue.add(p);
            //设置级别为0
            startLevel.put(p, 0);

            //从前往后遍历，将所有split节点入栈
            while (!flowNodeQueue.isEmpty()) {
                SWFFlowNode q = flowNodeQueue.poll();
                int level = startLevel.get(q) + 1;

                if (q.getSuccessorList().size() <= 0) {//如果没有后继节点
                    continue;
                } else if ((q.getSuccessorList().size() > 1) && !(q instanceof SWFControlNode)) {//如果后继节点大于1,则增加控制流节点，然后又控制流节点栈
//                    SWFControlNode swfControlNode = new SWFControlNode(NodeType.CONTROL_NODE,
//                            "new"+staticNumber,ControlNodeType.AND,ControlNodeType.SPLIT);
//                    staticNumber++;
                    SWFControlNode swfControlNode = new SWFControlNode(NodeType.CONTROL_NODE,
                            MyUUID.getUUID32(), ControlNodeType.AND, ControlNodeType.SPLIT);
                    swfControlNode.setNodeId(MyUUID.getUUID32());
//
                    //设置级别
                    startLevel.put(swfControlNode, level);
                    //更新节点关系
                    swfControlNode.addPrecursorNode(q);

                    //将节点的后继节点加入到控制流节点后面,并设置级别,并加入队列
                    for (SWFFlowNode flowNode : q.getSuccessorList()) {
                        swfControlNode.addSuccessorNode(flowNode);
                        flowNode.addPrecursorNode(swfControlNode);
                        flowNode.removePrecursorNode(q);
                        //设置级别
                        if (startLevel.getOrDefault(flowNode, -1) < (level + 1)) {
                            startLevel.put(flowNode, level + 1);
                        }
                        flowNodeQueue.add(flowNode);
                    }
                    //删除节点与后继节点的关系
                    q.removeAllSuccessorNode(q.getSuccessorList());

                    q.addSuccessorNode(swfControlNode);
                    splitControlNodeList.add(swfControlNode);
                    //控制流节点入栈
                    startToEnd.push(swfControlNode);
                } else {
                    for (SWFFlowNode flowNode : q.getSuccessorList()) {
                        //设置后继节点的级别，如果已存在的级别小于当前级别
                        if (startLevel.getOrDefault(flowNode, -1) < level) {
                            startLevel.put(flowNode, level);
                        }
                        flowNodeQueue.add(flowNode);
                    }
                }

            }
        }

        //从前往后增加控制流节点，进行闭合
        while (!startToEnd.isEmpty()) {
            SWFControlNode splitNode = startToEnd.pop();
            String nodeDesc = splitNode.getNodeDescp();
            //判断后继节点是否有公共后继节点
            int childrenSize = splitNode.getSuccessorList().size();
            ArrayList<SWFFlowNode> result = getPublicChildrenMore(splitNode.getSuccessorList(), startLevel);
            TreeSet<SWFFlowNode> resultSet = new TreeSet<>(result);
            //当没有公共后继节点时，即最后到达的为多结束节点,则增加一个控制流节点
            if (resultSet.size() == childrenSize && result.size() == (childrenSize - 1) * 2) {
                //创建join控制流节点
                SWFControlNode joinNode = new SWFControlNode(NodeType.CONTROL_NODE, nodeDesc,
                        ControlNodeType.AND, ControlNodeType.JOIN);
                joinNode.setNodeId(MyUUID.getUUID32());
                joinNodeMap.put(nodeDesc, joinNode);
                for (SWFFlowNode node : resultSet) {
                    if (startLevel.getOrDefault(joinNode, -1) < startLevel.get(node)) {
                        startLevel.put(joinNode, startLevel.get(node) + 1);
                    }
                    joinNode.addPrecursorNode(node);
                    node.addSuccessorNode(joinNode);
                    if (node instanceof SWFTaskNode || node instanceof SWFPairControlNode) {
                        int index = matrixNodeList.indexOf(node);
                        endModelNode.set(index, 0);
                    }
                }
                joinControlNodeList.add(joinNode);
            }

            //如果找公共后继节点
            if (resultSet.size() == 1) {
                SWFControlNode joinNode = new SWFControlNode(NodeType.CONTROL_NODE, nodeDesc,
                        ControlNodeType.AND, ControlNodeType.JOIN);
                joinNode.setNodeId(MyUUID.getUUID32());
                joinNodeMap.put(nodeDesc, joinNode);
                SWFFlowNode flowNode = resultSet.first();
                for (SWFFlowNode node : flowNode.getPrecursorList()) {
                    node.removeSuccessorNode(flowNode);
                    node.addSuccessorNode(joinNode);
                    joinNode.addPrecursorNode(node);
                }
                flowNode.removeAllPrecursorNode(flowNode.getPrecursorList());
                joinNode.addSuccessorNode(flowNode);
                flowNode.addPrecursorNode(joinNode);
                joinControlNodeList.add(joinNode);
            }
        }


        flowNodeQueue.clear();
        ArrayList<SWFFlowNode> endModelList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (endModelNode.get(i) == 1) {
                endModelList.add(matrixNodeList.get(i));
            }
        }
        for (SWFFlowNode node : joinControlNodeList) {
            if (node.getSuccessorList().size() == 0) {
                endModelList.add(node);
            }
        }

        for (SWFFlowNode p : endModelList) {//从后往开始遍历，采取广度优先算法
            //入队
            flowNodeQueue.add(p);
            //设置级别为0
            endLevel.put(p, 0);

            //从后往前遍历，将所有join节点入栈
            while (!flowNodeQueue.isEmpty()) {
                SWFFlowNode q = flowNodeQueue.poll();
                int level = endLevel.get(q) + 1;

                if (q.getPrecursorList().size() <= 0) {//如果没有前驱节点
                    continue;
                } else if ((q.getPrecursorList().size() > 1) && !(q instanceof SWFControlNode)) {//如果前驱节点大于1且不是控制流节点,则增加控制流节点，然后控制流节点栈
//                    SWFControlNode swfControlNode = new SWFControlNode(NodeType.CONTROL_NODE,"new"+staticNumber,
//                            ControlNodeType.AND,ControlNodeType.JOIN);
                    SWFControlNode swfControlNode = new SWFControlNode(NodeType.CONTROL_NODE, MyUUID.getUUID32(),
                            ControlNodeType.AND, ControlNodeType.JOIN);
                    swfControlNode.setNodeId(MyUUID.getUUID32());
                    joinNodeMap.put(swfControlNode.getNodeDescp(), swfControlNode);

//                    joinNodeMap.put("new"+staticNumber,swfControlNode);
//                    staticNumber++;


                    //设置级别
                    endLevel.put(swfControlNode, level);
                    //更新节点关系
                    swfControlNode.addSuccessorNode(q);

                    //将节点的前驱节点加入到控制流节点前面,并设置级别,并加入队列
                    for (SWFFlowNode flowNode : q.getPrecursorList()) {
                        swfControlNode.addPrecursorNode(flowNode);
                        flowNode.addSuccessorNode(swfControlNode);
                        flowNode.removeSuccessorNode(q);
                        //设置级别
                        if (endLevel.getOrDefault(flowNode, -1) < (level + 1)) {
                            endLevel.put(flowNode, level + 1);
                        }
                        flowNodeQueue.add(flowNode);
                    }
                    //删除节点与前驱节点的关系
                    q.removeAllPrecursorNode(q.getPrecursorList());

                    q.addPrecursorNode(swfControlNode);
                    joinControlNodeList.add(swfControlNode);
                    //控制流节点入栈
                    endToStart.push(swfControlNode);
                } else {
                    for (SWFFlowNode flowNode : q.getPrecursorList()) {
                        //设置前驱节点的级别，如果已存在的级别小于当前级别
                        if (endLevel.getOrDefault(flowNode, -1) < level) {
                            endLevel.put(flowNode, level);
                        }
                        flowNodeQueue.add(flowNode);
                    }
                }

            }
        }


        while (!endToStart.isEmpty()) {
            SWFControlNode joinNode = endToStart.pop();
            String nodeDesc = joinNode.getNodeDescp();
            //判断后继节点是否有前驱后继节点
            int parentSize = joinNode.getPrecursorList().size();
            ArrayList<SWFFlowNode> result = getPublicParentMore(joinNode.getPrecursorList(), endLevel);
            LinkedHashSet<SWFFlowNode> resultSet = new LinkedHashSet<>(result);
            //当没有公共前驱节点时，即最后到达的为多结束节点,则增加一个控制流节点
            if (resultSet.size() == parentSize && result.size() == (parentSize - 1) * 2) {
                SWFControlNode splitNode = new SWFControlNode(NodeType.CONTROL_NODE, nodeDesc,
                        ControlNodeType.AND, ControlNodeType.SPLIT);
                splitNode.setNodeId(MyUUID.getUUID32());
                int maxLevel = -1;
                for (SWFFlowNode node : resultSet) {
                    maxLevel = maxLevel > endLevel.get(node) ? maxLevel : endLevel.get(node);
                    splitNode.addSuccessorNode(node);
                    node.addPrecursorNode(splitNode);
                    if (node instanceof SWFTaskNode || node instanceof SWFPairControlNode) {
                        int index = matrixNodeList.indexOf(node);
                        startModelNode.set(index, 0);
                    }
                }
                if (endLevel.getOrDefault(splitNode, -1) < maxLevel) {
                    endLevel.put(splitNode, maxLevel + 1);
                }

                splitControlNodeList.add(splitNode);
            }

            //如果找公共后继节点
            if (resultSet.size() == 1) {
                SWFControlNode splitNode = new SWFControlNode(NodeType.CONTROL_NODE, nodeDesc,
                        ControlNodeType.AND, ControlNodeType.SPLIT);
                splitNode.setNodeId(MyUUID.getUUID32());
                SWFFlowNode flowNode = resultSet.iterator().next();
                for (SWFFlowNode node : flowNode.getSuccessorList()) {
                    node.removePrecursorNode(flowNode);
                    node.addPrecursorNode(splitNode);

                    splitNode.addSuccessorNode(node);
                    flowNode.removeSuccessorNode(node);
                }
                flowNode.addSuccessorNode(splitNode);
                splitNode.addPrecursorNode(flowNode);
                splitControlNodeList.add(splitNode);
            }
        }
    }

    private ArrayList<SWFFlowNode> getPublicChildrenMore(ArrayList<SWFFlowNode> swfFlowNodes, TreeMap<SWFFlowNode, Integer> startLevel) {
        ArrayList<SWFFlowNode> result = new ArrayList<>();
        for (int i = 0; i < swfFlowNodes.size() - 1; i++) {
            ArrayList<SWFFlowNode> children = getPublicChildren2(swfFlowNodes.get(i), swfFlowNodes.get(i + 1), startLevel);
            result.addAll(children);
        }
        return result;
    }

    private ArrayList<SWFFlowNode> getPublicChildren2(SWFFlowNode one, SWFFlowNode two, TreeMap<SWFFlowNode, Integer> startLevel) {
        ArrayList<SWFFlowNode> result = new ArrayList<>();
        //用于存储映射集合
        Map<SWFFlowNode, Integer> map = new TreeMap<>(new Comparator<SWFFlowNode>() {
            @Override
            public int compare(SWFFlowNode o1, SWFFlowNode o2) {
                if (o1 instanceof SWFControlNode && o2 instanceof SWFControlNode) {
                    return (((SWFControlNode) o1).getOneType() + o1.getNodeDescp())
                            .compareTo(((SWFControlNode) o2).getOneType() + o2.getNodeDescp());
                } else {
                    return (o1.getNodeDescp() + o1.getNodeId()).compareTo(o2.getNodeDescp() + o2.getNodeId());
                }
            }
        });

        Queue<Map.Entry<SWFFlowNode, Integer>> queue1 = new LinkedList<>();
        Queue<Map.Entry<SWFFlowNode, Integer>> queue2 = new LinkedList<>();

        //遍历第一个节点的所有节点，加入级别队列，并排序
        Queue<SWFFlowNode> queue = new LinkedList<>();
        queue.add(one);
        while (!queue.isEmpty()) {
            SWFFlowNode temp = queue.poll();
            int level = startLevel.get(temp);
            map.put(temp, level);
            queue.addAll(temp.getSuccessorList());
        }
        map = MapSort.sortMapByValueSmall(map);
        Iterator<Map.Entry<SWFFlowNode, Integer>> iterator = map.entrySet().iterator();
        //将节点按级别大小从小到到加入队列
        while (iterator.hasNext()) {
            Map.Entry<SWFFlowNode, Integer> entry = iterator.next();
            queue1.add(entry);
        }

        queue.clear();
        map.clear();

        //遍历第二个节点的所有节点，加入级别队列，并排序
        queue.add(two);
        while (!queue.isEmpty()) {
            SWFFlowNode temp = queue.poll();
//            System.out.println(temp.getNodeDescp());
            int level = startLevel.get(temp);
            map.put(temp, level);
            queue.addAll(temp.getSuccessorList());
        }
        map = MapSort.sortMapByValueSmall(map);
        iterator = map.entrySet().iterator();
        //将节点按级别大小从小到到加入队列
        while (iterator.hasNext()) {
            Map.Entry<SWFFlowNode, Integer> entry = iterator.next();
            queue2.add(entry);
        }

        SWFFlowNode end1 = ((LinkedList<Map.Entry<SWFFlowNode, Integer>>) queue1).getLast().getKey();
        SWFFlowNode end2 = ((LinkedList<Map.Entry<SWFFlowNode, Integer>>) queue2).getLast().getKey();

        Map.Entry<SWFFlowNode, Integer> temp1 = queue1.poll();
        Map.Entry<SWFFlowNode, Integer> temp2 = queue2.poll();
        while (queue1.size() > 0 || queue2.size() > 0) {
            if (temp1.getKey() == temp2.getKey()) {
                result.add(temp1.getKey());
                return result;
            }
            if (temp1.getValue() > temp2.getValue()) {
                if (!queue2.isEmpty()) {
                    temp2 = queue2.poll();
                } else {
                    break;
                }
            } else if (temp1.getValue() < temp2.getValue()) {
                if (!queue1.isEmpty()) {
                    temp1 = queue1.poll();
                } else {
                    break;
                }
            } else {
                if (!queue1.isEmpty()) {
                    temp1 = queue1.poll();
                } else {
                    break;
                }
                if (!queue2.isEmpty()) {
                    temp2 = queue2.poll();
                } else {
                    break;
                }
            }
        }
        result.add(end1);
        result.add(end2);
        return result;
    }


    private ArrayList<SWFFlowNode> getPublicParentMore(ArrayList<SWFFlowNode> swfFlowNodes, TreeMap<SWFFlowNode, Integer> endLevel) {
        ArrayList<SWFFlowNode> result = new ArrayList<>();
        for (int i = 0; i < swfFlowNodes.size() - 1; i++) {
            ArrayList<SWFFlowNode> children = getPublicParent2(swfFlowNodes.get(i), swfFlowNodes.get(i + 1), endLevel);
            result.addAll(children);
        }
        return result;
    }

    private ArrayList<SWFFlowNode> getPublicParent2(SWFFlowNode one, SWFFlowNode two, TreeMap<SWFFlowNode, Integer> endLevel) {
        ArrayList<SWFFlowNode> result = new ArrayList<>();
        //用于存储映射集合
        Map<SWFFlowNode, Integer> map = new TreeMap<>(new Comparator<SWFFlowNode>() {
            @Override
            public int compare(SWFFlowNode o1, SWFFlowNode o2) {
                if (o1 instanceof SWFControlNode && o2 instanceof SWFControlNode) {
                    return (((SWFControlNode) o1).getOneType() + o1.getNodeDescp())
                            .compareTo(((SWFControlNode) o2).getOneType() + o2.getNodeDescp());
                } else {
                    return (o1.getNodeDescp() + o1.getNodeId()).compareTo(o2.getNodeDescp() + o2.getNodeId());
                }
            }
        });

        Queue<Map.Entry<SWFFlowNode, Integer>> queue1 = new LinkedList<>();
        Queue<Map.Entry<SWFFlowNode, Integer>> queue2 = new LinkedList<>();

        //遍历第一个节点的所有节点，加入级别队列，并排序
        Queue<SWFFlowNode> queue = new LinkedList<>();
        queue.add(one);
        while (!queue.isEmpty()) {
            SWFFlowNode temp = queue.poll();
            int level = endLevel.get(temp);
            map.put(temp, level);
            queue.addAll(temp.getPrecursorList());
        }
        map = MapSort.sortMapByValueSmall(map);
        Iterator<Map.Entry<SWFFlowNode, Integer>> iterator = map.entrySet().iterator();
        //将节点按级别大小从小到到加入队列
        while (iterator.hasNext()) {
            Map.Entry<SWFFlowNode, Integer> entry = iterator.next();
            queue1.add(entry);
        }

        queue.clear();
        map.clear();

        //遍历第二个节点的所有节点，加入级别队列，并排序
        queue.add(two);
        while (!queue.isEmpty()) {
            SWFFlowNode temp = queue.poll();
            int level = endLevel.get(temp);
            map.put(temp, level);
            queue.addAll(temp.getPrecursorList());
        }
        map = MapSort.sortMapByValueSmall(map);
        iterator = map.entrySet().iterator();
        //将节点按级别大小从小到到加入队列
        while (iterator.hasNext()) {
            Map.Entry<SWFFlowNode, Integer> entry = iterator.next();
            queue2.add(entry);
        }

        SWFFlowNode end1 = ((LinkedList<Map.Entry<SWFFlowNode, Integer>>) queue1).getLast().getKey();
        SWFFlowNode end2 = ((LinkedList<Map.Entry<SWFFlowNode, Integer>>) queue2).getLast().getKey();

        Map.Entry<SWFFlowNode, Integer> temp1 = queue1.poll();
        Map.Entry<SWFFlowNode, Integer> temp2 = queue2.poll();
        while (queue1.size() > 0 || queue2.size() > 0) {
            if (temp1.getKey() == temp2.getKey()) {
                result.add(temp1.getKey());
                return result;
            }
            if (temp1.getValue() > temp2.getValue()) {
                if (!queue2.isEmpty()) {
                    temp2 = queue2.poll();
                } else {
                    break;
                }
            } else if (temp1.getValue() < temp2.getValue()) {
                if (!queue1.isEmpty()) {
                    temp1 = queue1.poll();
                } else {
                    break;
                }
            } else {
                if (!queue1.isEmpty()) {
                    temp1 = queue1.poll();
                } else {
                    break;
                }
                if (!queue2.isEmpty()) {
                    temp2 = queue2.poll();
                } else {
                    break;
                }
            }
        }
        result.add(end1);
        result.add(end2);
        return result;
    }

    /**
     * 后处理，并返回新的工作流图
     *
     * @return
     */
    private SWFNewGraph reconstitutionAlpha() {

        SWFStartNode startNode = new SWFStartNode(NodeType.START_NODE, "");
        startNode.setNodeId(MyUUID.getUUID32());
        SWFEndNode endNode = new SWFEndNode(NodeType.END_NODE, "");
        endNode.setNodeId(MyUUID.getUUID32());
        int size = matrixNodeList.size();

        ArrayList<SWFFlowNode> recordList = new ArrayList<>();
        //构建开始节点关系
        for (int i = 0; i < size; i++) {
            if (startModelNode.get(i) == 1) {
                recordList.add(matrixNodeList.get(i));
            }
        }
        for (SWFFlowNode node : splitControlNodeList) {
            if (node.getPrecursorList().isEmpty()) {
                recordList.add(node);
            }
        }

        String nodeDesc = MyUUID.getUUID32();
        if (recordList.size() > 1) {
//            SWFControlNode splitNode=new SWFControlNode(NodeType.CONTROL_NODE,"new"+staticNumber,
//                    ControlNodeType.AND,ControlNodeType.SPLIT);
            SWFControlNode splitNode = new SWFControlNode(NodeType.CONTROL_NODE, nodeDesc,
                    ControlNodeType.AND, ControlNodeType.SPLIT);
            splitNode.setNodeId(MyUUID.getUUID32());
            for (SWFFlowNode node : recordList) {
                splitNode.addSuccessorNode(node);
                node.addPrecursorNode(splitNode);
            }
            splitControlNodeList.add(splitNode);
            startNode.addSuccessorNode(splitNode);
            splitNode.addPrecursorNode(startNode);
        } else {
            startNode.addSuccessorNode(recordList.get(0));
            recordList.get(0).addPrecursorNode(startNode);
        }

        recordList.clear();
        //构建结束节点关系
        for (int i = 0; i < size; i++) {
            if (endModelNode.get(i) == 1) {
                recordList.add(matrixNodeList.get(i));
            }
        }
        for (SWFFlowNode node : joinControlNodeList) {
            if (node.getSuccessorList().isEmpty()) {
                recordList.add(node);
            }
        }

        if (recordList.size() > 1) {
//            SWFControlNode joinNode=new SWFControlNode(NodeType.CONTROL_NODE,"new"+staticNumber,
//                    ControlNodeType.AND,ControlNodeType.JOIN);
            SWFControlNode joinNode = new SWFControlNode(NodeType.CONTROL_NODE, nodeDesc,
                    ControlNodeType.AND, ControlNodeType.JOIN);
            joinNode.setNodeId(MyUUID.getUUID32());
            staticNumber++;
            for (SWFFlowNode node : recordList) {
                joinNode.addPrecursorNode(node);
                node.addSuccessorNode(joinNode);
            }
            joinControlNodeList.add(joinNode);
            endNode.addPrecursorNode(joinNode);
            joinNode.addSuccessorNode(endNode);
        } else {
            endNode.addPrecursorNode(recordList.get(0));
            recordList.get(0).addSuccessorNode(endNode);
        }


        /////////////////////////将块节点，转化成正常节点/////////////////////
        blockNodeToPair();

        //获取控制流边
        splitControlNodeList.clear();
        joinControlNodeList.clear();
        ArrayList<SWFControlFlowEdge> swfControlFlowEdgeList = new ArrayList<>();
        LinkedHashSet<SWFFlowNode> set = new LinkedHashSet<>();
        Queue<SWFFlowNode> queue = new LinkedList<>();
        queue.add(startNode);
        set.add(startNode);
        while (!queue.isEmpty()) {
            SWFFlowNode swfFlowNode = queue.poll();
            for (SWFFlowNode node : swfFlowNode.getSuccessorList()) {
                SWFControlFlowEdge swfControlFlowEdge = new SWFControlFlowEdge(swfFlowNode, node);
                swfControlFlowEdgeList.add(swfControlFlowEdge);
                if (!set.contains(node)) {
                    queue.add(node);
                    set.add(node);
                    if (node instanceof SWFControlNode) {
                        if (((SWFControlNode) node).getOneType().equals(ControlNodeType.SPLIT)) {
                            splitControlNodeList.add((SWFControlNode) node);
                        } else if (((SWFControlNode) node).getOneType().equals(ControlNodeType.JOIN)) {
                            joinControlNodeList.add((SWFControlNode) node);
                        }
                    }
                }
            }
        }

        //构建新的工作流对象
        SWFNewGraph swfNewGraph = new SWFNewGraph(startNode, endNode, swfDataNodeList, swfTaskNodeList,
                splitControlNodeList, joinControlNodeList, swfControlFlowEdgeList, swfDataFlowEdges);

        return swfNewGraph;
    }


    /**
     * 后处理，重构petri网，用于对比时间
     *
     * @return
     */
    private SWFNewGraph reconstitutionAlphaToJinTao() {

        SWFStartNode startNode = new SWFStartNode(NodeType.START_NODE, "");
        startNode.setNodeId(MyUUID.getUUID32());
        SWFEndNode endNode = new SWFEndNode(NodeType.END_NODE, "");
        endNode.setNodeId(MyUUID.getUUID32());
        int size = matrixNodeList.size();

        ArrayList<SWFFlowNode> recordList = new ArrayList<>();
        //构建开始节点关系
        for (int i = 0; i < size; i++) {
            if (startModelNode.get(i) == 1) {
                recordList.add(matrixNodeList.get(i));
            }
        }
        for (SWFFlowNode node : splitControlNodeList) {
            if (node.getPrecursorList().isEmpty()) {
                recordList.add(node);
            }
        }

        String nodeDesc = MyUUID.getUUID32();
        if (recordList.size() > 1) {
//            SWFControlNode splitNode=new SWFControlNode(NodeType.CONTROL_NODE,"new"+staticNumber,
//                    ControlNodeType.AND,ControlNodeType.SPLIT);
            SWFControlNode splitNode = new SWFControlNode(NodeType.CONTROL_NODE, nodeDesc,
                    ControlNodeType.AND, ControlNodeType.SPLIT);
            splitNode.setNodeId(MyUUID.getUUID32());
            for (SWFFlowNode node : recordList) {
                splitNode.addSuccessorNode(node);
                node.addPrecursorNode(splitNode);
            }
            splitControlNodeList.add(splitNode);
            startNode.addSuccessorNode(splitNode);
            splitNode.addPrecursorNode(startNode);
        } else {
            startNode.addSuccessorNode(recordList.get(0));
            recordList.get(0).addPrecursorNode(startNode);
        }

        recordList.clear();
        //构建结束节点关系
        for (int i = 0; i < size; i++) {
            if (endModelNode.get(i) == 1) {
                recordList.add(matrixNodeList.get(i));
            }
        }
        for (SWFFlowNode node : joinControlNodeList) {
            if (node.getSuccessorList().isEmpty()) {
                recordList.add(node);
            }
        }

        if (recordList.size() > 1) {
//            SWFControlNode joinNode=new SWFControlNode(NodeType.CONTROL_NODE,"new"+staticNumber,
//                    ControlNodeType.AND,ControlNodeType.JOIN);
            SWFControlNode joinNode = new SWFControlNode(NodeType.CONTROL_NODE, nodeDesc,
                    ControlNodeType.AND, ControlNodeType.JOIN);
            joinNode.setNodeId(MyUUID.getUUID32());
            staticNumber++;
            for (SWFFlowNode node : recordList) {
                joinNode.addPrecursorNode(node);
                node.addSuccessorNode(joinNode);
            }
            joinControlNodeList.add(joinNode);
            endNode.addPrecursorNode(joinNode);
            joinNode.addSuccessorNode(endNode);
        } else {
            endNode.addPrecursorNode(recordList.get(0));
            recordList.get(0).addSuccessorNode(endNode);
        }


        /////////////////////////将块节点，转化成正常节点/////////////////////
        blockNodeToPair();


        //添加库所
        HashSet<SWFFlowNode> nodeSet = new HashSet<>();
        Stack<SWFFlowNode> nodeStack = new Stack<>();

        nodeStack.push(startNode);
        nodeSet.add(startNode);
        while (!nodeStack.isEmpty()) {
            SWFFlowNode nowNode = nodeStack.pop();

            Iterator<SWFFlowNode> iterator = nowNode.getSuccessorList().iterator();
            ArrayList<SWFFlowNode> removeNode = new ArrayList<>();
            ArrayList<SWFFlowNode> addNode = new ArrayList<>();

            while (iterator.hasNext()) {
                SWFFlowNode node = (SWFFlowNode) iterator.next();
                //如果后一个节点不是控制流节点，增加一个控制流节点
                if (!(node instanceof SWFControlNode)) {
                    removeNode.add(node);
                    node.removePrecursorNode(nowNode);
                    SWFControlNode controlNode = new SWFControlNode(NodeType.LIBRARY_NODE, MyUUID.getUUID32(), "", ControlNodeType.SPLIT);
                    controlNode.addPrecursorNode(nowNode);
                    addNode.add(controlNode);

                    node.addPrecursorNode(controlNode);
                    controlNode.addSuccessorNode(node);
                }

                if (!nodeSet.contains(node)) {
                    nodeSet.add(node);
                    nodeStack.push(node);
                }

            }
            nowNode.removeAllSuccessorNode(removeNode);
            for (SWFFlowNode node : addNode) {
                nowNode.addSuccessorNode(node);
            }
        }


        //获取控制流边
        splitControlNodeList.clear();
        joinControlNodeList.clear();
        ArrayList<SWFControlFlowEdge> swfControlFlowEdgeList = new ArrayList<>();
        LinkedHashSet<SWFFlowNode> set = new LinkedHashSet<>();
        Queue<SWFFlowNode> queue = new LinkedList<>();
        queue.add(startNode);
        set.add(startNode);
        while (!queue.isEmpty()) {
            SWFFlowNode swfFlowNode = queue.poll();
            for (SWFFlowNode node : swfFlowNode.getSuccessorList()) {
                SWFControlFlowEdge swfControlFlowEdge = new SWFControlFlowEdge(swfFlowNode, node);
                swfControlFlowEdgeList.add(swfControlFlowEdge);
                if (!set.contains(node)) {
                    queue.add(node);
                    set.add(node);
                    if (node instanceof SWFControlNode) {
                        if (((SWFControlNode) node).getOneType().equals(ControlNodeType.SPLIT)) {
                            splitControlNodeList.add((SWFControlNode) node);
                        } else if (((SWFControlNode) node).getOneType().equals(ControlNodeType.JOIN)) {
                            joinControlNodeList.add((SWFControlNode) node);
                        }
                    }
                }
            }
        }

        //构建新的工作流对象
        SWFNewGraph swfNewGraph = new SWFNewGraph(startNode, endNode, swfDataNodeList, swfTaskNodeList,
                splitControlNodeList, joinControlNodeList, swfControlFlowEdgeList, swfDataFlowEdges);

        return swfNewGraph;
    }


    //将块节点，转化成正常节点
    private void blockNodeToPair() {
        for (SWFPairControlNode node : blockNodeList) {
            SWFControlNode splitNode = node.getStartControlNode();
            SWFControlNode joinNode = node.getEndControlNode();

            //处理split的关系
            SWFFlowNode precursorNode = node.getPrecursorList().get(0);
            precursorNode.removeSuccessorNode(node);
            node.removePrecursorNode(precursorNode);

            precursorNode.addSuccessorNode(splitNode);
            splitNode.addPrecursorNode(precursorNode);

            //处理join的关系
            SWFFlowNode successorNode = node.getSuccessorList().get(0);
            successorNode.removePrecursorNode(node);
            node.removeSuccessorNode(successorNode);

            successorNode.addPrecursorNode(joinNode);
            joinNode.addSuccessorNode(successorNode);
        }
    }

    public int getRawControlEdgeSize() {
        return rawControlEdgeSize;
    }
}
