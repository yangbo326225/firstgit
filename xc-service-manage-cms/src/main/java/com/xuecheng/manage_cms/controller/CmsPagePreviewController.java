package com.xuecheng.manage_cms.controller;


import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.CmsPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletOutputStream;
import java.io.IOException;


@Controller//需要返回的是html  所以使用@controller  继承 BaseController
public class CmsPagePreviewController extends BaseController {

    @Autowired
    private CmsPageService cmsPageService;

    @RequestMapping(value = "/cms/preview/{pageId}",method = RequestMethod.GET)
    public void  preview(@PathVariable("pageId") String pageId){
         String html = cmsPageService.getPageHtml(pageId);
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            response.setHeader("Content-type","text/html;charset=utf-8");
            outputStream.write(html.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    }
