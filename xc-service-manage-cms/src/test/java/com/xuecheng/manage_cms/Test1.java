package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.model.GridFSFile;
import javafx.application.Application;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author siyang
 * @create 2020-03-10 18:46
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class Test1 {

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Test
    public void demo1(){
        File file = new File("E:\\IDEA\\workspace\\xc-springcloud\\xc-ui-pc-static-portal\\include\\index_banner-template.html");
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectId store = gridFsTemplate.store(fileInputStream, "轮播图模板.html","");
            System.out.println("发布id为"+store.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

}
