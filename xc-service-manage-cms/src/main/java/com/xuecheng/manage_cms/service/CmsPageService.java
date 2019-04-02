package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

public interface CmsPageService {
    //分页查询
    public QueryResponseResult findList(int page,int size, QueryPageRequest queryPageRequest);

    //添加页面
    public CmsPageResult add(CmsPage cmsPage);

    // 修改页面
    public CmsPage findById(String Id);
    //修改
    public CmsPageResult edit(String Id,CmsPage cmsPage);
    //删除
    public ResponseResult deleteByid(String Id);

    //根据CmsConfig 中id 查询
    public CmsConfig findByConigId(String id);

    public String getPageHtml(String pageId);

    public ResponseResult  postPage(String pageId);


   public CmsPageResult  save(CmsPage cmsPage);

    public CmsPostPageResult postPageQuick(CmsPage cmsPage);
}

