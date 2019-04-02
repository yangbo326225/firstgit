package com.xuecheng.manage_cms_client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class PageService {

    private static  final Logger LOGGER= LoggerFactory.getLogger(PageService.class);
    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    CmsSiteRepository cmsSiteRepository;

    //保存页面静态文件到服务器物理路径
    public void  savePageToServerPath(String pageId){

        //由pageId从mongodb中获取html文件的名称htmlFileId
        CmsPage cmsPage = findById(pageId);
        String fileId = cmsPage.getHtmlFileId();
        if(StringUtils.isEmpty(fileId)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        //有htmlFileId获取GridFs中html文件
        InputStream inputStream = finfFileById(fileId);

        if(inputStream==null){
            LOGGER.error("htmlFileId获取GridFs中html文件有问题，fileId:{}",fileId);
            return;
        }
        //得到站点的物理路径
        String siteId = cmsPage.getSiteId();
        CmsSite cmsSite = findPathById(siteId);
        String sitePhysicalPath = cmsSite.getSitePhysicalPath();
        //得到页面的物理路径
     String path=sitePhysicalPath+cmsPage.getPagePhysicalPath()+cmsPage.getPageName();

        //将html文件保存到本地服务器物理路径上
        FileOutputStream fileOutputStream=null;
        try {
            fileOutputStream=new FileOutputStream(new File(path));
            IOUtils.copy(inputStream,fileOutputStream);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    //由pageId从mongodb中获取html文件的名称htmlFileId
    public CmsPage findById(String pageId){
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
            if(optional.isPresent()){
                return optional.get();
            }
        return null;
    }

    //有htmlFileId获取GridFs中html文件
    public InputStream finfFileById(String fileId){
        try {
        GridFSFile gridFSFile=gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        GridFSDownloadStream gridFSDownloadStream=gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        GridFsResource gridFsResource=new GridFsResource(gridFSFile,gridFSDownloadStream);

            return gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //得到站点的信息
    public CmsSite findPathById(String siteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if(optional.isPresent()){
            CmsSite cmsSite = optional.get();
            return cmsSite;
        }
        return null;
    }
}
