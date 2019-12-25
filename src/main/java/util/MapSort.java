package util;





import parallelzation.node.SWFFlowNode;

import java.util.*;

/**
 * @author kuangzengxiong
 * @date 2019/3/30 21:41
 */
public class MapSort {

    //从小到大
    public static Map<SWFFlowNode,Integer> sortMapByValueSmall(Map<SWFFlowNode,Integer> map){
        if(map==null||map.isEmpty()){
            return null;
        }
        Map<SWFFlowNode,Integer> sortMap=new LinkedHashMap<>();
        List<Map.Entry<SWFFlowNode,Integer>> entryList=new ArrayList<>(map.entrySet());
        Collections.sort(entryList,new MapValueComparator());

        Iterator<Map.Entry<SWFFlowNode,Integer>> iterator=entryList.iterator();
        Map.Entry<SWFFlowNode,Integer> tempEntry=null;
        while (iterator.hasNext()){
            tempEntry=iterator.next();
            sortMap.put(tempEntry.getKey(),tempEntry.getValue());
        }
        return sortMap;
    }

//    //从大到小排序
//    public static Map<Integer,SWFFlowNode> sortMapByKeyBig(Map<Integer,SWFFlowNode> map){
//        if(map==null||map.isEmpty()){
//            return null ;
//        }
//        Map<Integer,SWFFlowNode> sortMap=new TreeMap<>(new Comparator<Integer>() {
//            @Override
//            public int compare(Integer o1, Integer o2) {
//                return o2.compareTo(o1);
//            }
//        });
//        sortMap.putAll(map);
//        return sortMap;
//    }
//
//    //从小到大排序
//    public static Map<Integer,SWFFlowNode> sortMapByKeySmall(Map<Integer,SWFFlowNode> map){
//        if(map==null||map.isEmpty()){
//            return null ;
//        }
//        Map<Integer,SWFFlowNode> sortMap=new TreeMap<>(new Comparator<Integer>() {
//            @Override
//            public int compare(Integer o1, Integer o2) {
//                return o1.compareTo(o2);
//            }
//        });
//        sortMap.putAll(map);
//        return sortMap;
//    }



}


class MapValueComparator implements Comparator<Map.Entry<SWFFlowNode, Integer>> {

    @Override
    public int compare(Map.Entry<SWFFlowNode, Integer> me1, Map.Entry<SWFFlowNode, Integer> me2) {
        return me1.getValue().compareTo(me2.getValue());
    }
}


