package com.xuecheng.manage_media;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.manage_media.service.MediaUploadService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * @author siyang
 * @create 2020-03-28 16:39
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class Test {
    @Autowired
    MediaUploadService mediaUploadService;

    @org.junit.Test
    public void chunks() {
        String inputPath = "F:\\迅雷下载\\计算机视频\\Springcloud\\cms微服务\\day13 在线学习 HLS\\资料\\lucene.mp4";
        String outputPath = "F:\\迅雷下载\\计算机视频\\Springcloud\\cms微服务\\day13 在线学习 HLS\\资料\\chunks\\";
        File input = new File(inputPath);
        File output = new File(outputPath);
        if (!output.exists()) {
            output.mkdirs();
        }
        int chunkSize = 1024 * 1024;
        //分块数量
        long chunkNum = (long) Math.ceil(input.length() * 1.0 / chunkSize);
        //缓冲区
        byte[] b = new byte[1024];
        try {
            RandomAccessFile read = new RandomAccessFile(inputPath, "r");
            for (int i = 0; i < chunkNum; i++) {
                //创建分块文件
                File file = new File(outputPath + i);
                file.createNewFile();

                RandomAccessFile write = new RandomAccessFile(file, "rw");
                int len = -1;
                while ((len = read.read(b)) != -1) {
                    write.write(b, 0, len);
                    if (file.length() > chunkSize) {
                        break;
                    }
                }
                System.out.println("完成分块：" + i);
                write.close();
            }
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @org.junit.Test
    public void merge() throws Exception {
        String inputPath = "F:\\迅雷下载\\计算机视频\\Springcloud\\cms微服务\\day13 在线学习 HLS\\资料\\chunks\\";
        String outputPath = "F:\\迅雷下载\\计算机视频\\Springcloud\\cms微服务\\day13 在线学习 HLS\\资料\\chunks\\lucene.mp4";
        File input = new File(inputPath);
        File output = new File(outputPath);
        if (output.exists()) {
            output.delete();
        }
        File[] files = input.listFiles();
        List<File> fileList = Arrays.asList(files);
        //排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        output.createNewFile();
        RandomAccessFile write = new RandomAccessFile(output, "rw");

        //缓冲区
        byte[] b = new byte[1024];

        for (File chunkFile : fileList) {
            RandomAccessFile read = new RandomAccessFile(chunkFile, "r");
            int len = -1;
            while ((len = read.read(b)) != -1) {
                write.write(b, 0, len);
            }
            System.out.println("读取分区：" + chunkFile.getName());
            read.close();
        }
        write.close();


    }
    @org.junit.Test
    public void demo3 (){
        CheckChunkResult asdhjzxc1123 = mediaUploadService.checkchunk("asdhjzxc1123", 1, 222);
        System.out.printf(asdhjzxc1123.toString());

    }
}
