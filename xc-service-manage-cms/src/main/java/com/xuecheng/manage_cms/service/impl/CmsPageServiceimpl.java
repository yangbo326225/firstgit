package com.xuecheng.manage_cms.service.impl;


import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTempleteRepository;
import com.xuecheng.manage_cms.service.CmsPageService;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 分页查询
 */
@Service
public class CmsPageServiceimpl implements CmsPageService {
    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    CmsConfigRepository cmsConfigRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    CmsTempleteRepository cmsTempleteRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    CmsSiteRepository cmsSiteRepository;


    @Override
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
       /*  站点Id：精确匹配
           模板Id：精确匹配
           页面别名：模糊匹配*/
        //自定义条件查询
        //防止传来的值为null后面取值时报空指针
        if (queryPageRequest == null) {
            QueryPageRequest queryPageRequest1 = new QueryPageRequest();

        }
        //条件匹配器
        ExampleMatcher exampleMatche = ExampleMatcher.matching();
        exampleMatche = exampleMatche.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());

        //条件值对象
        CmsPage cmsPage = new CmsPage();
        //设置传递过来的条件值
        if (StringUtils.isNoneEmpty(queryPageRequest.getSiteId())) {
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        if (StringUtils.isNoneEmpty(queryPageRequest.getTemplateId())) {
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        if (StringUtils.isNoneEmpty(queryPageRequest.getPageAliase())) {
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //创建模板
        Example<CmsPage> example = Example.of(cmsPage, exampleMatche);

        /*
         * 分页查询
         * */

        //传递的page 一般用户体验为第一页为第一页，但是CmsPageRepository定义为0为第一页，所以重新设置
        if (page <= 0) {
            page = 1;
        }
        page = page - 1;
        if (size <= 0) {
            size = 10;
        }
        PageRequest pageable = PageRequest.of(page, size);
        //条件查询以及分页查询
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);

        //要求返回的是QueryResponseResult
        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }

    /*
     * 添加页面
     * */
    @Override

    public CmsPageResult add(CmsPage cmsPage) {

        if (cmsPage == null) {
            //抛出 非法请求异常
            ExceptionCast.cast(CommonCode.FAIL);
        }

        //确定传入的页面唯一性，使用三个属性值 siteId，pageName ，webPath
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        //先判断异类异常情况
        if (cmsPage1 != null) {
            //页面已经存在
            //抛出异常  内容  页面已经存在
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

        //无论用户是否填写id  都是用mongodb自生成的
        cmsPage.setSiteId(null);
        CmsPage save = cmsPageRepository.save(cmsPage);
        CmsPageResult cmsPageResult = new CmsPageResult(CommonCode.SUCCESS, cmsPage);
        return cmsPageResult;


    }

    /*
     * 修改页面
     * */
    @Override
    public CmsPage findById(String Id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(Id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
//更新
    @Override
    public CmsPageResult edit(String Id, CmsPage cmsPage) {
        CmsPage one = this.findById(Id);
        if (one != null) {
            //设置修改项
            //更新模板id
            one.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            one.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            one.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            one.setPageName(cmsPage.getPageName());
            //更新访问路径
            one.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //修改dataUrl
            one.setDataUrl(cmsPage.getDataUrl());
            //执行更新
            CmsPage save = cmsPageRepository.save(one);
            if (save != null) {
                //返回成功
                return new CmsPageResult(CommonCode.SUCCESS, save);
            }

        }
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    /*
     * 删除
     * */
    @Override
    public ResponseResult deleteByid(String Id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(Id);
        if (optional.isPresent()) {
            cmsPageRepository.deleteById(Id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }


    /*
     *    CmsConfig  通过id查询
     *
     * */
    @Override
    public CmsConfig findByConigId(String id) {
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if (optional.isPresent()) {
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;

    }



    /*
    * 获取页面的DataUrl
    * 远程请求DataUrl获取数据模型。
    * 获取页面的模板信息
     * 执行页面静态化
    *
    * */
    @Override
    public String  getPageHtml(String pageId){
        //获取数据模型
        Map model = getModel(pageId);
        //获取模板信息
       String content= getTemplete(pageId);
       //页面的静态化
        String html = generationHtml(model, content);
        if (StringUtils.isEmpty(html)){
          ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        return html;
    }

    //获取数据模型
    private Map getModel(String pageId){
        //获取页面的DataUrl
        CmsPage cmsPage = this.findById(pageId);
        if(cmsPage==null){
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_NOEXISTSNAME);
        }
        String dataUrl = cmsPage.getDataUrl();
        if(StringUtils.isEmpty(dataUrl)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //远程请求DataUrl获取数据模型。
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }



    //获取模板
    private  String getTemplete(String pageId){
        CmsPage cmsPage = this.findById(pageId);
        if(cmsPage==null){
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_NOEXISTSNAME);
        }
        String templateId = cmsPage.getTemplateId();
        if(StringUtils.isEmpty(templateId)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //获取GridFs chunk中模板的信息
        //首先去找CMSTemplate中的templeteFileId
        Optional<CmsTemplate> optional = cmsTempleteRepository.findById(templateId);
        if(optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            String templateFileId = cmsTemplate.getTemplateFileId();
            //取出GridFs文件服务器中的模板 fs.files对象中的主键id就是templeteFileId
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流对象  fs.chunks对象 的主键 就是 fs.files中htmlFileId
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建gridFsResource，用于获取流对象
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
            //获取流中的数据
            try{
                String s = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
                return s;
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return null;
    }



    //页面的静态化
    private String generationHtml( Map model,String content){

            //生成配置类
            Configuration configuration =new Configuration(Configuration.getVersion());
            //模板加载器
            StringTemplateLoader  stringTemplateLoader=new StringTemplateLoader();
            stringTemplateLoader.putTemplate("template",content);
            //向configuration配置模板加载器
            configuration.setTemplateLoader(stringTemplateLoader);
        try {
            //获取模板
            Template template = configuration.getTemplate("template");

            //调用api进行静态化
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
              return html;
        }catch (Exception e){
            e.printStackTrace();
        }

        return  null;
    }

  //页面发布
    public ResponseResult  postPage(String pageId){

        //发布静态页面
        String pageHtml = this.getPageHtml(pageId);
        if(StringUtils.isEmpty(pageHtml)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        //向文件服务器保存静态页面
        CmsPage cmsPage = saveToGridFs(pageId, pageHtml);
        //向MQ发送消息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);

    }



    //向文件服务器保存静态页面
     private CmsPage saveToGridFs( String pageId,String pageHtml){
        //获取页面名称
         CmsPage cmsPage = this.findById(pageId);
         if(cmsPage==null){
             ExceptionCast.cast(CmsCode.CMS_ADDPAGE_NOEXISTSNAME);
         }
         //使用流将文件保存
         InputStream inputStream = null;
         ObjectId objectId =null;
         try {
             inputStream = IOUtils.toInputStream(pageHtml,"utf-8");
             objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
         } catch (IOException e) {
             e.printStackTrace();
         }
         //将html文件的id 保存到数据库中
         cmsPage.setHtmlFileId(objectId.toString());
         CmsPage cms = cmsPageRepository.save(cmsPage);
         return cms;
     }
    //向MQ发送消息
    private  void sendPostPage(String pageId){
        //获取站点id作为routingkey
        CmsPage cmsPage = findById(pageId);
        //获取消息内容 pageId  转为json
        Map<String,String> map=new HashMap<>();
        map.put("pageId",pageId);
        String msg = JSON.toJSONString(map);


        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,cmsPage.getSiteId(),msg);
    }

    @Override
    //有就更新  没有就创建
    public CmsPageResult save(CmsPage cmsPage) {
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(),
                cmsPage.getSiteId(), cmsPage.getPageWebPath());
    //存在
        if(cmsPage1!=null){
            this.edit(cmsPage1.getPageId(),cmsPage);
        }
    //不存在

        return  this.save(cmsPage);
    }

    @Override
    //一键发布
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //将页面保存到cms_page中  （有 更新，没有保存）
        CmsPageResult save = this.save(cmsPage);
        if(!save.isSuccess()){
             //不成功
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //得到pageid
        CmsPage cmsPage1 = save.getCmsPage();
        String pageId = cmsPage1.getPageId();
        //执行页面静态化 保存GRIDFS 向MQ发消息
        ResponseResult responseResult = this.postPage(pageId);
        if(!responseResult.isSuccess()){
            //不成功
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //拼接url  页面Url= cmsSite.siteDomain+cmsSite.siteWebPath+ cmsPage.pageWebPath + cmsPage.pageName
        //获取cmssite
        String siteId = cmsPage1.getSiteId();
        CmsSite cmsSiteById = findCmsSiteById(siteId);
        String url=cmsSiteById.getSiteDomain()+cmsSiteById.getSiteWebPath()+cmsPage1.getPageWebPath()+cmsPage1.getPageName();
        return new CmsPostPageResult(CommonCode.SUCCESS,url);
    }
    public CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optionalSite = cmsSiteRepository.findById(siteId);
        if(optionalSite.isPresent()){
            CmsSite cmsSite = optionalSite.get();
            return cmsSite;
        }
        return null;
    }
}
