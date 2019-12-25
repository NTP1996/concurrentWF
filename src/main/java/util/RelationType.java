package util;

/**
 * @author kuangzengxiong
 * @date 2019/3/8 16:55
 */
public interface RelationType {

    //任务执行关系的定义
    public final String PERFORM="Perform_Relation";
    public final String TRANSITIVE_PERFORM="Transitive_Perform_Relation";
    public final String PARALLEL="Parallel_Relation";
    public final String MUTEX="Mutex_Relation";
    public final String SINGLELOOP="Single_Loop_Relation";
    public final String TWOLOOP="Two_Loop_Relation";

    //基于数据依赖的关系
    //基于数据依赖的因果关系
    public final String CAUSALITY="Causality_Relation";
    // 基于数据依赖的传递因果关系
    public final String PASS_CAUSALITY="Pass_Causality_Relation";
    // 基于数据依赖的关键因果关系
    public final String KEY_CAUSALITY="Key_Causality_Relation";

    public final String NOT_RELATION="Not_Relation";
}
