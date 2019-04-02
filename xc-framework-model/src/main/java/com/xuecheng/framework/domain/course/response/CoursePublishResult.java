package com.xuecheng.framework.domain.course.response;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;

public class CoursePublishResult extends ResponseResult {
    String url;
    public  CoursePublishResult(ResultCode resultCode,String url){
       super(resultCode);
       this.url=url;
    }
}
