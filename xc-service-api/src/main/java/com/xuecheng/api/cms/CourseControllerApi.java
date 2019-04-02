package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;

@Api(value="课程管理接口",description = "课程管理接口，提供页面的增、删、改、查")
public interface CourseControllerApi {
    @ApiOperation("课程计划查询")
    public TeachplanNode findTeachplanList(String courseId);
    @ApiOperation("添加课程计划")
    public ResponseResult addTeachplan(Teachplan teachplan);

    @ApiOperation("添加课程图片")
    public ResponseResult addCoursePic(String CourseId, String fileId);

    @ApiOperation("查询课程视图")
    public CourseView  courseview(String id);

    @ApiOperation("预览课程")//(String id 课程id)
    public CoursePublishResult preview(String id);
    @ApiOperation("发布课程")//(String id 课程id)
    public CoursePublishResult publish(@PathVariable String id);
}
