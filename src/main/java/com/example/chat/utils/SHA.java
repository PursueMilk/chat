package com.example.chat.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SHA {
    public static String getSHA(String str) {
        StringBuffer hash = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");                    //创建具有指定算法名称的摘要
            md.update(str.getBytes(StandardCharsets.UTF_8));                //使用指定的字节数组更新摘要
            byte mdBytes[] = md.digest();                                   //进行哈希运算返回一个数组
            for (int i = 0; i < mdBytes.length; i++) {                   //相应的转换
                int temp;
                if (mdBytes[i] < 0) {
                    temp = 256 + mdBytes[i];                             //有小于0的字节转换为正数
                } else {
                    temp = mdBytes[i];
                }
                if (temp < 16) {
                    hash.append("0");                                  //小于16则添加前置0
                }
                hash.append(Integer.toHexString(temp));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash.toString();
    }

}
