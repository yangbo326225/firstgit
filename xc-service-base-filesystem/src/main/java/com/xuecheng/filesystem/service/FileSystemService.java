package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class FileSystemService {

    @Value("${xuecheng.fastdfs.tracker_servers}")
    String tracker_servers;
    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    int connect_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    int network_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.charset}")
    String charset;

    @Autowired
    FileSystemRepository fileSystemRepository;

    //文件上传
    public UploadFileResult upload(MultipartFile multipartFile,
                                   String filetag,
                                   String businesskey,
                                   String metadata
    ) {
        if (multipartFile == null) {
            //非法参数
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);

        }
        //上传文件到fastdfs
        String fileId = fdfs_upload(multipartFile);

        //保存基本信息到mongodb数据库
        FileSystem fileSystem = new FileSystem();
        //文件id
        fileSystem.setFileId(fileId);
        //文件在文件系统中的路径
        fileSystem.setFilePath(fileId);
        //业务标识
        fileSystem.setBusinesskey(businesskey);
        //标签
        fileSystem.setFiletag(filetag);
        //metadata
        if(StringUtils.isNotEmpty(metadata)){
            //由json转化为Map
            Map metadatamap = JSON.parseObject(metadata, Map.class);
            fileSystem.setMetadata(metadatamap);
        }
        FileSystem fileSystem1 = fileSystemRepository.save(fileSystem);
        return new UploadFileResult(CommonCode.SUCCESS,fileSystem1);

    }

    //上传文件到fastdfs
    public String fdfs_upload(MultipartFile multipartFile) {

        try {
            //初始化fastDFS环境
            innit();
            //创建TrackerClient
            TrackerClient trackerClient = new TrackerClient();
            //连接  获取 tracker_server
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取storage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建StoragerClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storeStorage);
            //上传文件
            //把文件转化为字节
            byte[] fileBytes = multipartFile.getBytes();
            //获取文件的名称
            String originalFilename = multipartFile.getOriginalFilename();
            String fileId = storageClient1.upload_file1(fileBytes, originalFilename, null);
            return fileId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //初始化
    public void innit() {
        try {
            ClientGlobal.initByTrackers(tracker_servers);
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
            ClientGlobal.setG_charset(charset);
        } catch (Exception e) {
            e.printStackTrace();
            //初始化失败
            ExceptionCast.cast(FileSystemCode.FS_SERVERFAIL_INIT_FAIL);

        }
    }
}
