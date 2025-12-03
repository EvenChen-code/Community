package com.nowcoder.community.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {
    // 生成一个随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /*
    * md5只能加密，不能解密
    * 比较简单的加密算法，如果原始密码很简单，则每次加密出来的密码都是一样的，很容易被撞库破解
    * 数据库表中会设置一个salt字段，是一个随机字符串，跟原始密码拼接后再进行加密
    * */
    // MD5加密
    public static String md5(String key){
        // 空格，空字符串，null都判定为空
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

}
