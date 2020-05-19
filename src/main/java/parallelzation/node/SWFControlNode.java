package parallelzation.node;

/**
 * @author kuangzengxiong
 * @date 2019/3/8 15:32
 */
public class SWFControlNode extends SWFFlowNode {
    private String oneType;
    private String pairType;

    /**
     * nodeDesp记录控制流节点对的编号
     * controlType记录控制流节点的类型，比如split，join
     * pairType记录控制流节点对的类型，如and, or, xor, loop
     */
    public SWFControlNode(String nodeType, String nodeDescp,String pairType,String oneType) {
        super(nodeType, nodeDescp);
        // TODO Auto-generated constructor stub
        this.oneType=oneType;
        this.pairType=pairType;
    }

    public String getOneType() {
        return oneType;
    }


    public String getPairType() {
        return pairType;
    }
}
