package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/*
* 自定义异常捕获
* */
@ControllerAdvice//控制器增强
public class ExceptionCatch {

    //添加日志
    private static final Logger LOGGER= org.slf4j.LoggerFactory.getLogger(ExceptionCatch.class);

    //只要遇到CustomException 此注解就可以捕获
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseResult customException(CustomException customException){
        //记录日志
        LOGGER.error("catch customException",customException.getMessage());
        ResultCode resultCode = customException.getResultCode();
        return  new ResponseResult(resultCode);
    }
}
