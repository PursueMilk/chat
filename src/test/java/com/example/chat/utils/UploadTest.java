package com.example.chat.utils;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class UploadTest {

    @Test
    void uploadTest() throws IOException {
        File file=new File("D:\\Program\\Info\\test.jpg");
        FileInputStream inputStream=new FileInputStream(file);
        MultipartFile file1=new MockMultipartFile(file.getName(),inputStream);
/*        ImgUtil.upload("20","user",file1);*/
        System.out.println(file1.getOriginalFilename());
    }


    @Test
    void httpUrlTest() throws UnknownHostException {
    }
}
