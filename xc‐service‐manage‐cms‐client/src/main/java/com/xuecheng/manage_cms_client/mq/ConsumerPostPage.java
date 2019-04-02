package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.service.PageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConsumerPostPage {
    private static final Logger LOGGER= LoggerFactory.getLogger(ConsumerPostPage.class);
    @Autowired
    PageService pageService;
    @RabbitListener(queues = {"${xuecheng.mq.queue}"})
    public void postPage(String msg) {
        //解析消息  msg由文档为json数据  转为map
        Map map = JSON.parseObject(msg, Map.class);
        String pageId = (String) map.get("pageId");
        //验证pageId 是否是正确的
        CmsPage cmsPage = pageService.findById(pageId);
        if(cmsPage==null){
            LOGGER.error("此pageId没有对应的页面，pageId：{}",pageId);
            return;
        }
        //由pageId，从GridFs文件服务器中下载页面

        pageService.savePageToServerPath(pageId);

    }
}
