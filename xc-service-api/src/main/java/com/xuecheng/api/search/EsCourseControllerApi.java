package com.xuecheng.api.search;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="ES查询接口",description = "查询接口")
public interface EsCourseControllerApi {
    //查询
    @ApiOperation("课程搜索信息")
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam);
}
