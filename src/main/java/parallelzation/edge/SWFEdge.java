package parallelzation.edge;


import parallelzation.node.SWFNode;

/**
 * @author kuangzengxiong
 * @date 2019/3/8 15:34
 */
public class SWFEdge {
    private SWFNode leftNode;   // 左节点

    private SWFNode rightNode;  // 右节点

    public SWFEdge(SWFNode leftNode, SWFNode rightNode) {
        super();
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }

    public SWFNode getLeftNode() {
        return leftNode;
    }

    public void setLeftNode(SWFNode leftNode) {
        this.leftNode = leftNode;
    }

    public SWFNode getRightNode() {
        return rightNode;
    }

    public void setRightNode(SWFNode rightNode) {
        this.rightNode = rightNode;
    }
}
