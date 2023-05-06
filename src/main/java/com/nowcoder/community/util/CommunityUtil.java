package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Date: create in 14:34 2023/3/8
 * @describe:
 */
public class CommunityUtil {

    /**
    * @Description: 生成随机字符串
    * @Date: 2023/3/8 14:35
    */
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**
    * @Description: MD5加密
    * @Date: 2023/3/8 14:36
    */
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
           return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
    * @Description:
    * @Date: 2023/3/12 15:38
    */
    public static String getJSONString(int code, String msg, Map<String,Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);

        if(map != null){
            for (String key : map.keySet()) {
                json.put(key,map.get(key));
            }
        }
        return json.toJSONString();
    }
    public static String getJSONString(int code) {
        return getJSONString(code,null,null);
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code,msg,null);
    }
}
