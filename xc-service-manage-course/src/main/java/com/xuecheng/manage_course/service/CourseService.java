package com.xuecheng.manage_course.service;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.*;
import com.xuecheng.manage_course.feign.CmsPageClient;
import org.hibernate.validator.constraints.pl.PESEL;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author siyang
 * @create 2020-03-13 17:59
 */
@Service
public class CourseService {

    @Autowired
    CourseMapper courseMapper;
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    CoursePubRepository coursePubRepository;
    @Autowired
    CoursePicRepository coursePicRepository;
    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;

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

    @Autowired
    CmsPageClient cmsPageClient;


    public TeachplanNode findTeachplanList(String courseId){

        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        return teachplanNode;
    }
    //获取课程计划根结点，如果没有则添加根结点
    public String getTeachplanRoot(String courseId){

        CourseBase courseBase = this.getCourseBaseById(courseId);

        //存在查找根节点
        List<Teachplan> list = teachplanRepository.findByCourseidAndParentid(courseBase.getId(), "0");
        //如果不存在根节点则添加
        if(list==null ||list.size()==0){
            Teachplan teachplan = new Teachplan();
            teachplan.setCourseid(courseBase.getId());
            teachplan.setPname(courseBase.getName());
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setStatus("0");
            Teachplan save = teachplanRepository.save(teachplan);
            return save.getId();
        }
        //存在根节点直接返回
        return list.get(0).getId();

    }

    //添加课程计划
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan){
        //校验课程id和课程计划名称
        if(teachplan == null ||StringUtils.isEmpty(teachplan.getCourseid()) ||
                StringUtils.isEmpty(teachplan.getPname())){
            throw new CustomException(CommonCode.INVALID_PARAM);
        }

        //取出课程id
        String courseid = teachplan.getCourseid();
        // 取出父结点id
        String parentid = teachplan.getParentid();

        //如果为空代表没有父节点。看是否需要创建根节点
        if(StringUtils.isEmpty(parentid)){
            parentid = this.getTeachplanRoot(courseid);
        }
        //设置父结点
        teachplan.setParentid(parentid);
        if(StringUtils.isEmpty(teachplan.getGrade())){
            teachplan.setStatus("0");//未发布
        }

        //获得父节点信息
        Optional<Teachplan> byId = teachplanRepository.findById(parentid);
        if(!byId.isPresent()){
            throw new CustomException(CommonCode.INVALID_PARAM);
        }
        Teachplan teachplan1 = byId.get();
        String grade = teachplan1.getGrade();
        //通过父类设置等级
        if(grade.equals("1")){
            teachplan.setGrade("2");
        }else if(grade.equals("2")){
            teachplan.setGrade("3");
        }

        //保存
        teachplanRepository.save(teachplan);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //分页得到课程列表
    public QueryResponseResult getCourseListLimit(int page, int size, CourseListRequest courseListRequest){
        //分页
        PageHelper.startPage(page,size);
        Page<CourseInfo> coursePage = courseMapper.getCourseList(courseListRequest);
        //构建返回
        QueryResult<CourseInfo> courseInfoQueryResult = new QueryResult<>(coursePage.getResult(),coursePage.getTotal());
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,courseInfoQueryResult);
        return queryResponseResult;
    }
    //添加课程
    public ResponseResult  addCourse(CourseBase courseBase){

       //使用服务中相互调用 检查 courseBase 中 grade 和 studymode 是否合法

        courseBaseRepository.save(courseBase);
        return new ResponseResult(CommonCode.SUCCESS);

    }

    //根据课程id 查找课程
    public CourseBase getCourseBaseById(String courseId){
        if(StringUtils.isEmpty(courseId)){
            throw new CustomException(CommonCode.INVALID_PARAM);
        }
        Optional<CourseBase> byId = courseBaseRepository.findById(courseId);
        if(!byId.isPresent()){
            throw new CustomException(CommonCode.INVALID_PARAM);
        }

        return  byId.get();
    }

    //更新课程
    public ResponseResult updateCourseBase(String id, CourseBase courseBase){
        CourseBase cb = this.getCourseBaseById(id);

        //修改

        cb.setName(courseBase.getName());
        cb.setUsers(courseBase.getUsers());
        cb.setSt(courseBase.getSt());
        cb.setMt(courseBase.getMt());
        cb.setGrade(courseBase.getGrade());
        cb.setStudymodel(courseBase.getStudymodel());
        cb.setDescription(courseBase.getDescription());

        courseBaseRepository.save(cb);
        return new ResponseResult(CommonCode.SUCCESS);

    }

    //根据课程id 查找营销计划
    public CourseMarket getCourseMarketById(String courseId){
        Optional<CourseMarket> byId = courseMarketRepository.findById(courseId);
        if(!byId.isPresent()){
            throw new CustomException(CommonCode.INVALID_PARAM);
        }

        return  byId.get();

    }

    //更新课程营销计划
    public ResponseResult updateCourseMarket(String id, CourseMarket courseMarket){

        Optional<CourseMarket> byId = courseMarketRepository.findById(id);
        CourseMarket cm =courseMarket;
        //如果存在 更新相应的信息，如果不存在直接保存信息。
        if(byId.isPresent()){
            //修改
            cm = byId.get();
            cm.setCharge(courseMarket.getCharge());
            cm.setValid(courseMarket.getValid());
            cm.setQq(courseMarket.getQq());
        }

        courseMarketRepository.save(cm);
        return new ResponseResult(CommonCode.SUCCESS);

    }

    //保存课程图片
    @Transactional
    public ResponseResult saveCoursePic(String courseId,String pic){
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if(picOptional.isPresent()){
            coursePic = picOptional.get();
            coursePic.setPic(pic);
        }
        // 没有课程图片则新建对象
        if(coursePic == null){
            coursePic = new CoursePic(courseId, pic);
        }
        //保存
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);

    }
    //获得课程图片
    public CoursePic findCoursepic(String courseId) {
        Optional<CoursePic> byId = coursePicRepository.findById(courseId);
        if(byId.isPresent()){

            return byId.get();
        }
        return null;
    }
    //删除课程图片
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        //执行删除，返回1表示删除成功，返回0表示删除失败
        long result = coursePicRepository.deleteByCourseid(courseId);
        if(result>0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //根据courseid获取所有课程信息
    public CourseView getCourseViewInfo(String courseId){
        CourseView courseView = new CourseView();
        //查询课程基本信息
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            courseView.setCourseBase(courseBase);
        }
        //查询课程营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(courseId);
        if(courseMarketOptional.isPresent()){
            CourseMarket courseMarket = courseMarketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }
        //查询课程图片信息
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            courseView.setCoursePic(picOptional.get());
        }
        //查询课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;

    }
    //课程预览
    public CoursePublishResult preview(String courseId){
        CourseBase courseBase = this.getCourseBaseById(courseId);
        //请求cms添加页面

        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        // 模板
        cmsPage.setTemplateId(publish_templateId);
        // 页面名称
        cmsPage.setPageName(courseId+".html");
        // 页面别名
        cmsPage.setPageAliase(courseBase.getName());
        // 页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        // 页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        // 数据url
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);
        // 远程请求cms保存页面信息
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if(!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //页面id
        String pageId = cmsPageResult.getCmsPage().getPageId();
        // 页面url
        String pageUrl = previewUrl+pageId;
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);

    }

    //课程发布
    public CoursePublishResult publish(String courseId){

        //课程页面发布
        CmsPostPageResult cmsPostPageResult = this.publish_page(courseId);
        if(!cmsPostPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        //更新课程状态
        CourseBase courseBase = this.saveCoursePubState(courseId);
        //课程索引...
        //创建课程pub信息
        CoursePub coursePub = this.createCoursePub(courseId);
        //保存课程pub信息
        this.saveCoursePub(courseId, coursePub);

        // 课程缓存...
        this.saveTeachplanMediaPub(courseId);
        //返回url
        String url = cmsPostPageResult.getUrl();
        return  new CoursePublishResult(CommonCode.SUCCESS,url);

    }

    /**
     * 保存CoursePub，有则更新，无则添加
     * @param id
     */
    private CoursePub saveCoursePub(String id,CoursePub coursePub){
        if(StringUtils.isEmpty(id)){
            new CustomException(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }

        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(id);

        CoursePub coursePubNew =null;
        if(coursePubOptional.isPresent()){
            coursePubNew=coursePubOptional.get();
        }else{
            coursePubNew =new CoursePub();
        }
        //拷贝数据
        BeanUtils.copyProperties(coursePub,coursePubNew);

        //原对象中有id值
        //  coursePubNew.setId(id);

        //更新时间
        coursePubNew.setTimestamp(new Date());
        //更新发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(date);

        coursePubRepository.save(coursePubNew);


        return coursePubNew;


    }

    //创建课程pub信息
    private CoursePub createCoursePub(String id){
        CoursePub coursePub = new CoursePub();
        coursePub.setId(id);
        //基础信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if(courseBaseOptional.isPresent()){
            CourseBase courseBase = courseBaseOptional.get();
            BeanUtils.copyProperties(courseBase, coursePub);
        }
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }
        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }

        //课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        String teachPlan = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(teachPlan);
        return coursePub;
    }

    //更新课程发布状态
    private CourseBase saveCoursePubState(String courseId){
        CourseBase courseBase = this.getCourseBaseById(courseId);
        //更新发布状态
        courseBase.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseBase);
        return save;
    }
    //课程发布页面
    public CmsPostPageResult publish_page(String courseId){
        CourseBase courseBase = this.getCourseBaseById(courseId);
        //请求cms添加页面

        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        // 模板
        cmsPage.setTemplateId(publish_templateId);
        // 页面名称
        cmsPage.setPageName(courseId+".html");
        // 页面别名
        cmsPage.setPageAliase(courseBase.getName());
        // 页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        // 页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        // 数据url
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);

        // 远程请求cms发布页面信息
        return cmsPageClient.postPageQuick(cmsPage);


    }

    //保存媒资信息
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if(teachplanMedia == null){
            throw new CustomException(CommonCode.INVALID_PARAM);
        }
        //课程计划
        String teachplanId = teachplanMedia.getTeachplanId();
        //查询课程计划
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if(!optional.isPresent()){
            throw new CustomException(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        Teachplan teachplan = optional.get();
        //只允许为叶子结点课程计划选择视频
        String grade = teachplan.getGrade();
        if(StringUtils.isEmpty(grade) || !grade.equals("3")){
            throw new CustomException(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        TeachplanMedia one = null;
        Optional<TeachplanMedia> teachplanMediaOptional = teachplanMediaRepository.findById(teachplanId);
        if(!teachplanMediaOptional.isPresent()){
            one = new TeachplanMedia();
        }else{
            one = teachplanMediaOptional.get();
        }
        //保存媒资信息与课程计划信息
        one.setTeachplanId(teachplanId);
        one.setCourseId(teachplanMedia.getCourseId());
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaId(teachplanMedia.getMediaId());
        one.setMediaUrl(teachplanMedia.getMediaUrl());
        teachplanMediaRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //保存课程计划媒资信息
    private void saveTeachplanMediaPub(String courseId){
        //查询所有此课程的媒资信息
        List<TeachplanMedia> byCourseId = teachplanMediaRepository.findByCourseId(courseId);
        //删除pub中所有此课程的媒资信息
        teachplanMediaPubRepository.deleteByCourseId(courseId);

        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for (TeachplanMedia teachplanMedia : byCourseId) {
            //转换为pub 并保存
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        teachplanMediaPubRepository.saveAll(teachplanMediaPubList);
    }

}
