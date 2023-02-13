package com.example.chat.utils;

import org.springframework.util.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class MD5Util {

    //盐值
    public static final String salt = "forum_chat";

    /**
     * Spring自带实现
     *
     * @param str
     * @return
     */
    public static String getMD5(String str) {
        String md5;
        str += salt;
        try {
            md5 = DigestUtils.md5DigestAsHex(str.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return md5;
    }


    /**
     * Java自带实现
     *
     * @param str
     * @return
     */
    public static String getMD5ByJava(String str) {
        StringBuffer hash = new StringBuffer();
        try {
            //创建具有指定算法名称的摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            //使用指定的字节数组更新摘要
            md.update(str.getBytes(StandardCharsets.UTF_8));
            //进行哈希运算返回一个数组
            byte mdBytes[] = md.digest();
            System.out.println(Arrays.toString(mdBytes));
            //转变为16进制
            for (int i = 0; i < mdBytes.length; i++) {
                int temp;
                //有小于0的字节转换为正数
                if (mdBytes[i] < 0) {
                    temp = 256 + mdBytes[i];
                } else {
                    temp = mdBytes[i];
                }
                //小于16则添加前置0
                if (temp < 16) {
                    hash.append("0");
                }
                hash.append(Integer.toHexString(temp));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash.toString();
    }

}
