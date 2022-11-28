package com.example.chat.utils;

import cn.hutool.core.util.IdUtil;
import com.example.chat.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class ImgUtil {

    private static String localUrl;

    private static String httpUrl;

    static {
        localUrl = System.getProperty("user.dir") + File.separator + "img";
        httpUrl="http://172.30.192.192:";
/*        try {
            httpUrl = "http://" + InetAddress.getLocalHost().getHostAddress() + ":";
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }*/
    }

    public static String upload(String port, String content, MultipartFile file) {
        //获取上传文件的真实名称
        String fileName = file.getOriginalFilename();
        log.info("文件名称{}", fileName);
        //截取文件的后缀名
        String imgSuffix = fileName.substring(fileName.lastIndexOf("."));
        //生成唯一的文件名
        String newFile = IdUtil.simpleUUID() + imgSuffix;
        String fileUrl = localUrl + File.separator + content;
        //创建文件夹
        File realFile = new File(fileUrl);
        if (!realFile.exists()) {
            realFile.mkdirs();
        }
        //指定文件上传的目录
        File realFileName = new File(realFile, newFile);
        //上传目录
        try {
            file.transferTo(realFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //添加图片地址
        String imgUrl = httpUrl + port + "/img/" + content + "/" + newFile;
        log.info("生成的文件名{}", realFileName);
        log.info("访问图片地址{}", imgUrl);
        return imgUrl;
    }

    public static String getHttpUrl() {
        return httpUrl;
    }


    public static String getLocalUrl() {
        return localUrl;
    }
}
