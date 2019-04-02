package com.xuecheng.manage_course.service;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CoursepicRepository coursepicRepository;
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    CoursePubRepository coursePubRepository;

    @Autowired
    CmsPageClient cmsPageClient;

    public TeachplanNode selectList(String courseId) {
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);

        return teachplanNode;

    }

    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;

    //获取根节点   没有 则添加
    public String getTeachplanRoot(String courseId) {
//校验课程id
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            return null;
        }
        CourseBase courseBase = optional.get();
//取出课程计划根结点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId,
                "0");
        if (teachplanList == null || teachplanList.size() == 0) {
//新增一个根结点
            Teachplan teachplanRoot = new Teachplan();
            teachplanRoot.setCourseid(courseId);
            teachplanRoot.setPname(courseBase.getName());
            teachplanRoot.setParentid("0");
            teachplanRoot.setGrade("1");//1级
            teachplanRoot.setStatus("0");//未发布
            teachplanRepository.save(teachplanRoot);
            return teachplanRoot.getId();
        }
        Teachplan teachplan = teachplanList.get(0);
        return teachplan.getId();
    }

    /*
     *  添加课程计划
     * */
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
//校验课程id和课程计划名称
        if (teachplan == null ||
                StringUtils.isEmpty(teachplan.getCourseid()) ||
                StringUtils.isEmpty(teachplan.getPname())) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
//取出课程id
        String courseid = teachplan.getCourseid();
//取出父结点id
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)) {
//如果父结点为空则获取根结点
            parentid = getTeachplanRoot(courseid);
        }
//取出父结点信息
        Optional<Teachplan> teachplanOptional = teachplanRepository.findById(parentid);
        if (!teachplanOptional.isPresent()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
//父结点
        Teachplan teachplanParent = teachplanOptional.get();
//父结点级别
        String parentGrade = teachplanParent.getGrade();
//设置父结点
        teachplan.setParentid(parentid);
        teachplan.setStatus("0");//未发布
//子结点的级别，根据父结点来判断
        if (parentGrade.equals("1")) {
            teachplan.setGrade("2");
        } else if (parentGrade.equals("2")) {
            teachplan.setGrade("3");
        }
//设置课程id
        teachplan.setCourseid(teachplanParent.getCourseid());
        teachplanRepository.save(teachplan);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /*
     * 保存图片
     * */
    @Transactional
    public CoursePic addCousePic(String courseId, String fileId) {
        //如果存在就更新
        Optional<CoursePic> optional = coursepicRepository.findById(courseId);
        if (optional.isPresent()) {
            CoursePic coursePic = optional.get();
            coursePic.setPic(fileId);
        }
        //不催在
        CoursePic coursePic = new CoursePic();
        coursePic.setCourseid(courseId);
        coursePic.setPic(fileId);
        CoursePic coursePic1 = coursepicRepository.save(coursePic);
        return coursePic1;
    }


    /*
     * 获取课程视图
     * */
    public CourseView getCoruseView(String id) {

        CourseView courseView = new CourseView();
        //基础信息
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(id);
        if (optionalCourseBase.isPresent()) {
            CourseBase courseBase = optionalCourseBase.get();
            courseView.setCourseBase(courseBase);
        }
        //课程营销
        Optional<CourseMarket> optionalCourseMarket = courseMarketRepository.findById(id);
        if (optionalCourseMarket.isPresent()) {
            CourseMarket courseMarket = optionalCourseMarket.get();
            courseView.setCourseMarket(courseMarket);
        }

        //课程图片
        Optional<CoursePic> optionalCoursePic = coursepicRepository.findById(id);
        if (optionalCoursePic.isPresent()) {
            CoursePic coursePic = optionalCoursePic.get();
            courseView.setCoursePic(coursePic);
        }
        //教学计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);

        return courseView;
    }

    /*
     *  页面预览(String id 课程id)
     * */

    public CoursePublishResult preview(String id) {
        CmsPage cmsPage = new CmsPage();
        CourseBase one = findCourseBaseById(id);
        //课程预览站点
        cmsPage.setSiteId(publish_siteId);
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(id + ".html");
        //页面别名(课程名称)
        cmsPage.setPageAliase(one.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre + id);
        //远程请求cms添加页面
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if (!cmsPageResult.isSuccess()) {
            //保存失败
            return new CoursePublishResult(CommonCode.FAIL, null);
        }
        //返回页面id
        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();
        //拼装URL
        String url = previewUrl + pageId;

        //返回CoursePublishResult 包含url
        return new CoursePublishResult(CommonCode.SUCCESS, url);
    }

    //根据id查询课程基本信息
    public CourseBase findCourseBaseById(String courseId) {
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if (baseOptional.isPresent()) {
            CourseBase courseBase = baseOptional.get();
            return courseBase;
        }
        ExceptionCast.cast(CommonCode.FAIL);
        return null;
    }

    /*
     * 一键发布
     * */
    @Transactional
    public CoursePublishResult publish(String id) {
        //准备页面
        CmsPage cmsPage = new CmsPage();
        CourseBase one = findCourseBaseById(id);
        cmsPage.setSiteId(publish_siteId);
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(id + ".html");
        //页面别名(课程名称)
        cmsPage.setPageAliase(one.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre + id);
        //首先调用cms一键发布将课程详情页面发布到服务器
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (!cmsPostPageResult.isSuccess()) {
            //保存失败
            return new CoursePublishResult(CommonCode.FAIL, null);
        }
        //更改课程状态为已发布
        CourseBase courseBase = changStatueById(id);
        //保存课程索引信息
           //创建对象
        createCoursePub(id);
          //保存索引
        CoursePub coursePub = saveCoursePub(id, courseBase);
        if(coursePub==null){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //缓存课程信息

        //返回页面URL
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }

    //更改课程发布状态
    private CourseBase changStatueById(String id) {
        CourseBase courseBaseById = findCourseBaseById(id);
        courseBaseById.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseBaseById);
        return save;
    }

    //保存课程索引信息
    public CoursePub  createCoursePub(String id) {
        CoursePub coursePub=new CoursePub();
        //基础信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if (courseBaseOptional == null) {
            CourseBase courseBase = courseBaseOptional.get();
            BeanUtils.copyProperties(courseBase, coursePub);
        }
       //查询课程图片
        Optional<CoursePic> picOptional = coursepicRepository.findById(id);
        if (picOptional.isPresent()) {
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }
        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if (marketOptional.isPresent()) {
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }
        //课程计划
        TeachplanNode node = teachplanMapper.selectList(id);
        //转化为json保存
        String string = JSON.toJSONString(node);
        coursePub.setTeachplan(string);
     return coursePub;
    }

    //保存索引
    @Transactional
    public CoursePub saveCoursePub(String id, CourseBase courseBase) {
        CoursePub coursePubnew =null;
          //查询是否存在
        Optional<CoursePub> optionalCoursePub = coursePubRepository.findById(id);

        if(optionalCoursePub.isPresent()){
            //存在
            CoursePub coursePub = optionalCoursePub.get();
            BeanUtils.copyProperties(coursePub,coursePubnew);
            return coursePub;
        }else{
            //不存在
            //设置主键
            coursePubnew.setId(id);
            //更新时间戳为最新时间
            coursePubnew.setTimestamp(new Date());
            //发布时间
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY‐MM‐dd HH:mm:ss");
            String date = simpleDateFormat.format(new Date());
            coursePubnew.setPubTime(date);
            CoursePub coursePub = coursePubRepository.save(coursePubnew);
            return coursePub;

        }


    }
}
