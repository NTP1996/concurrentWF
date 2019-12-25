package parallelzation.node;

/**
 * @author kuangzengxiong
 * @date 2019/3/8 15:31
 */
public class SWFNode{
    private String nodeId="";

    private String nodeType;  //节点类型

    private String nodeDescp; //节点的语义描述

    public SWFNode(String nodeType, String nodeDescp) {
        super();
        this.nodeType = nodeType;
        this.nodeDescp = nodeDescp;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeDescp() {
        return nodeDescp;
    }

    public void setNodeDescp(String nodeDescp) {
        this.nodeDescp = nodeDescp;
    }
}
