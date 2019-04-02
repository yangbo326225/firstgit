package com.xuecheng.manage_cms.controller;


import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.CmsPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class CmsPageController implements CmsPageControllerApi {
    @Autowired
    CmsPageService cmsPageService;

    @Override
    @GetMapping("/cms/page/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") int page, @PathVariable("size") int size, QueryPageRequest queryPageRequest) {

        return cmsPageService.findList(page,size,queryPageRequest);
    }


    @Override
    @PostMapping("/add")
    public CmsPageResult add(@RequestBody CmsPage cmsPage) {
        CmsPageResult result = cmsPageService.add(cmsPage);
        return result;
    }


    @GetMapping("/get/{id}")
    @Override
    public CmsPage findById(@PathVariable("id") String Id) {

        return cmsPageService.findById(Id);
    }
  @PutMapping("/edit/{id}")
    @Override
    public CmsPageResult edit(@PathVariable("id") String Id, @RequestBody CmsPage cmsPage) {
        return cmsPageService.edit(Id,cmsPage);
    }
@DeleteMapping("/delete/{id}")
    @Override
    public ResponseResult deleteByid(@PathVariable("id") String Id) {
        return cmsPageService.deleteByid(Id);
    }

    @Override
    @PostMapping("/postPage/{pageId}")
    public ResponseResult post(@PathVariable("pageId") String pageId) {
        return  cmsPageService.postPage(pageId);
    }

    @Override
    @PostMapping("/save")
    public  CmsPageResult  save(@RequestBody CmsPage cmsPage) {
       return cmsPageService.save(cmsPage);
    }

    @Override
    @PostMapping("/postPageQuick")
    public CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage) {
        return  cmsPageService.postPageQuick(cmsPage);

    }
}
