package com.xuecheng.manage_course.client;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value="XC-SERVICE-MANAGE-CMS")
public interface CmsPageClient {
      //远程调用cms新创建页面
    @PostMapping("/save")//CmsPageResult 接口可以看到返回类型
    public CmsPageResult save(@RequestBody CmsPage cmsPage);
    //远程调用  一键发布
    @PostMapping("/postPageQuick")//CmsPostPageResult 接口可以看到返回类型
    public CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage);
}
