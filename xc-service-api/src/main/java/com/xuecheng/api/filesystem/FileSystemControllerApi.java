package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import org.springframework.web.multipart.MultipartFile;

@Api(value="文件管理接口",description = "文件管理接口，提供文件的增、删、改、查。")
public interface FileSystemControllerApi {
    /*
    *
    *  @param multipartFile 文件
     * @param filetag 文件标签
     * @param businesskey 业务key
     * @param metedata 元信息,json格式
     * @return
    * */
    public UploadFileResult upload( MultipartFile multipartFile ,
                                    String filetag,
                                    String businesskey,
                                    String metadata
                                    );

}
