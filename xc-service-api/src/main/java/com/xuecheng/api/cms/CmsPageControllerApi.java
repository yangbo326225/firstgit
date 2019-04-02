package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(value="cms页面管理接口",description = "cms页面管理接口，提供页面的增、删、改、查")
public interface CmsPageControllerApi {
@ApiOperation("分页查询页面列表")

    @ApiImplicitParams({
            @ApiImplicitParam(name="page",value = "页码",required=true,paramType="path",dataType="int"),
            @ApiImplicitParam(name="size",value = "每页记录数",required=true,paramType="path",dataType="int")
                    })
      public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);

/*
* 添加页面
* */
@ApiOperation("添加页面")
     public CmsPageResult add(CmsPage cmsPage);



/*
* 修改页面
* */
//查询
@ApiOperation("查询修改页面")
public CmsPage findById(String Id);
//修改
@ApiOperation("修改页面")
public CmsPageResult edit(String Id,CmsPage cmsPage);

//修改
@ApiOperation("删除页面")
public ResponseResult deleteByid(String Id);

@ApiOperation("发布页面")
public ResponseResult post(String pageId);

@ApiOperation("添加页面（课程预览）")
public  CmsPageResult  save(CmsPage cmsPage);

@ApiOperation("一键发布页面")
public CmsPostPageResult postPageQuick(CmsPage cmsPage);
}




