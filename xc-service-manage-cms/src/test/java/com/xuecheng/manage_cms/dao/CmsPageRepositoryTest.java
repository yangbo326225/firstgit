package com.xuecheng.manage_cms.dao;


import com.xuecheng.framework.domain.cms.CmsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {
    @Autowired
    CmsPageRepository cmsPageRepository;
    @Test
    public void testFindPage() {
        int page = 0;//从0开始
        int size = 10;//每页记录数
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        System.out.println(all);
    }

  /*  站点Id：精确匹配
    模板Id：精确匹配
    页面别名：模糊匹配*/
  @Test
  public void testFindByExample(){
      int page = 0;//从0开始
      int size = 10;//每页记录数
      Pageable pageable = PageRequest.of(page,size);
   //创建条件对象
       CmsPage cmsPage=new CmsPage();
       //对象中添加条件
      //cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
          cmsPage.setPageAliase("轮播");

      //条件匹配器
      ExampleMatcher exampleMatche=ExampleMatcher.matching();
      exampleMatche= exampleMatche.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
      //定义
      Example<CmsPage> example=Example.of(cmsPage,exampleMatche);
      Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
      List<CmsPage> content = all.getContent();
      System.out.println("显示-----------"+content);
  }

    //修改
    @Test
    public void updateCms(){
        //查到
        //返回optional对象是jdk1.8特性  也就是防止空指针  让你判断一下是不是为空  标准化了而已
        Optional<CmsPage> optional = cmsPageRepository.findById("5abefd525b05aa293098fca6");
        if(optional.isPresent()){
            CmsPage cmsPage = optional.get();
            //修改
            cmsPage.setPageAliase("CCC");
            //保存
            cmsPageRepository.save(cmsPage);
        }
    }




}
