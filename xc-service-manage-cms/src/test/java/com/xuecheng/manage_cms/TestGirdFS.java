package com.xuecheng.manage_cms;

import com.xuecheng.manage_cms.service.CmsPageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestGirdFS {

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    CmsPageService cmsPageService;


    @Test
    public void testGridFs() throws FileNotFoundException {
//要存储的文件
        File file=new File("E:\\course.ftl");
//定义输入流
        FileInputStream  fileInputStream=new FileInputStream(file);
//向GridFS存储文件
        org.bson.types.ObjectId objectId = gridFsTemplate.store(fileInputStream, "index_banner.ftl");
//得到文件ID
        System.out.println(objectId);
    }

    @Test
    public void testGetHtml(){
        String html = cmsPageService.getPageHtml("5c7656db693f71589cc6b683");
   System.out.println(html);
    }

}
