package util;

import java.util.UUID;

/**
 * @author kuangzengxiong
 * @date 2019/4/13 16:16
 */
public class MyUUID {
    public static String getUUID32(){
        String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        return uuid;
    }

}
