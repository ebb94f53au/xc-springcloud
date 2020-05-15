package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author siyang
 * @create 2020-03-06 18:20
 */
@Service
public class PageService {

    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    CmsTemplateRepository cmsTemplateRepository;
    @Autowired
    CmsSiteRepository cmsSiteRepository;
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    RabbitTemplate rabbitTemplate;
    /*** 页面列表分页查询
     * 添加 别名 条件 模糊查询
     * * @param page 当前页码
     * @param size 页面显示个数
     * @param queryPageRequest 查询条件
     * @return 页面列表
     * */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest){
        //条件匹配器
        // 页面名称模糊查询，需要自定义字符串的匹配器实现模糊查询
        ExampleMatcher exampleMatcher = ExampleMatcher.matching() .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        //条件值
        CmsPage cmsPage = new CmsPage();
        //站点ID
        if(StringUtils.isNotEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        // 页面别名
        if(StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //创建条件实例
        Example<CmsPage> of = Example.of(cmsPage, exampleMatcher);


        if (queryPageRequest == null){
            queryPageRequest = new QueryPageRequest();
        }
        if (page <= 0) {
            page = 1;
        }
        page = page - 1;
        //为了适应mongodb的接口将页码减1
        if (size <= 0) {
            size = 20;
        }
        //分页对象
        PageRequest pr = PageRequest.of(page, size);

        Page<CmsPage> all = cmsPageRepository.findAll(of,pr);
        //列表总数
        return new QueryResponseResult(CommonCode.SUCCESS,new QueryResult(all.getContent(),all.getTotalElements()));

    }

    /**
     * 添加cms信息
     * @param cmsPage
     * @return
     */
    public CmsPageResult add(CmsPage cmsPage){
        CmsPage cPage = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath
                (cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(cPage!=null)
            throw new CustomException(CmsCode.CMS_GENERATEHTML_ISNOTNULL);
        //id默认由spring data自动生成
        cmsPage.setPageId(null);
        //保存
        cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS,cmsPage);
    }

    /**
     * 根据id查找信息
     * @param id
     * @return
     */
    public CmsPageResult getById(String id){
        Optional<CmsPage> byId = cmsPageRepository.findById(id);
        //如果不存在 抛出异常
        if(!byId.isPresent()){
            throw new CustomException(CmsCode.CMS_GENERATEHTML_ISNULL);
        }
        return new CmsPageResult(CommonCode.SUCCESS,byId.get());

    }

    /**
     * 更新
     * @param id
     * @param cmsPage
     * @return
     */
    public CmsPageResult update(String id, CmsPage cmsPage){
        CmsPage one = this.getById(id).getCmsPage();


        //这里只更新了cmspage的一部分
        //更新模板id
        one.setTemplateId(cmsPage.getTemplateId());
        //更新所属站点
        one.setSiteId(cmsPage.getSiteId());
        // 更新页面别名
        one.setPageAliase(cmsPage.getPageAliase());
        // 更新页面名称
        one.setPageName(cmsPage.getPageName());
        // 更新访问路径
        one.setPageWebPath(cmsPage.getPageWebPath());
        // 更新物理路径
        one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
        //更新dataUrl
        one.setDataUrl(cmsPage.getDataUrl());
        // 执行更新
        CmsPage save = cmsPageRepository.save(one);
        if(save!=null){
            return new CmsPageResult(CommonCode.SUCCESS,save);
        }else{
            return new CmsPageResult(CommonCode.FAIL,null);
        }


    }

    /**
     * 删除
     * @param id
     * @return
     */
    public ResponseResult delete(String id){
        CmsPage one = this.getById(id).getCmsPage();

        cmsPageRepository.deleteById(id);
        return new ResponseResult(CommonCode.SUCCESS);

    }

    /**
     *  页面静态化
     */
    public String getPageHtml(String pageId){
        //获取页面模型数据
        Map body = this.getModelByPageId(pageId);
        if(body ==null ){
            throw new CustomException(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //获取页面模板
        String template = this.getTemplateByPageId(pageId);
        if(StringUtils.isEmpty(template)){
            throw new CustomException(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //执行静态化
        String html = this.generateHtml(template, body);
        if(StringUtils.isEmpty(html)){
            throw new CustomException(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        return html;
    }

    //获取页面模板
    private String getTemplateByPageId(String pageId){
        CmsPage cmsPage = this.getById(pageId).getCmsPage();
        Optional<CmsTemplate> byId = cmsTemplateRepository.findById(cmsPage.getTemplateId());
        if(!byId.isPresent()){
            new CustomException(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //模板文件id
        String templateFileId = byId.get().getTemplateFileId();

        //根据id查询文件
        GridFSFile gridFSFile =
                gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
        //打开下载流对象
        GridFSDownloadStream gridFSDownloadStream =
                gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //创建gridFsResource，用于获取流对象
        GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
        //获取流中的数据
        try {
            String s = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");

            return  s;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //获取页面模型数据
    private Map getModelByPageId(String pageId){
        CmsPage cmsPage = this.getById(pageId).getCmsPage();
        String dataUrl = cmsPage.getDataUrl();
        if(StringUtils.isEmpty(dataUrl)){
            throw new CustomException(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        return forEntity.getBody();

    }
    //根据模板和数据生成html
    private String generateHtml(String templateString,Map model){
        try {
            //创建配置类
            Configuration configuration=new Configuration(Configuration.getVersion());
            //模板加载器
            StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
            stringTemplateLoader.putTemplate("template",templateString);
            configuration.setTemplateLoader(stringTemplateLoader);
            //得到模板
            Template template = configuration.getTemplate("template","utf-8");

            //静态化
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            //静态化内容
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }

    /**
     * 页面发布
     * @param pageId
     * @return
     */
    public ResponseResult postPage(String pageId){
        //执行静态化
        String pageHtml = this.getPageHtml(pageId);
        //保存静态化文件
        CmsPage cmsPage = this.saveHtml(pageId, pageHtml);
        //发送消息
        this.sendPostPage(pageId);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //保存静态页面内容
    private CmsPage saveHtml(String pageId,String content){
        Optional<CmsPage> byId = cmsPageRepository.findById(pageId);
        if(!byId.isPresent()){
            throw new CustomException(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = byId.get();
        //存储之前先删除
        String htmlFileId = cmsPage.getHtmlFileId();
        if(StringUtils.isNotEmpty(htmlFileId)){
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(htmlFileId)));
        }
        //保存文件到GridFs中
        InputStream inputStream = IOUtils.toInputStream(content);
        ObjectId store = gridFsTemplate.store(inputStream, cmsPage.getPageName());

        //保存文件id
        String id = store.toString();
        cmsPage.setHtmlFileId(id);
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }
    //发送页面发布消息
    private void sendPostPage(String pageId){
        Optional<CmsPage> byId = cmsPageRepository.findById(pageId);
        if(!byId.isPresent()){
            throw new CustomException(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = byId.get();
        Map<String, String> map = new HashMap<>();
        map.put("pageId",pageId);

        //转换为json字符串
        String msg = JSON.toJSONString(map);
        //发送消息，站点id为 routerkey
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,cmsPage.getSiteId(),msg);
    }

    //添加页面，如果已存在则更新页面
    public CmsPageResult save(CmsPage cmsPage){
        //校验页面是否存在，根据页面名称、站点Id、页面webpath查询
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(),
                cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(cmsPage1 !=null){
            //有则更新
            return this.update(cmsPage1.getPageId(),cmsPage);
        }else{
            //无则添加
            return this.add(cmsPage);
        }
    }

    //一键发布页面
    public CmsPostPageResult postPageQuick(CmsPage cmsPage){
        //更新或添加
        CmsPageResult save = this.save(cmsPage);
        if(!save.isSuccess()){
            return new CmsPostPageResult(CommonCode.FAIL,null);
        }
        CmsPage cp = save.getCmsPage();
        //发布页面
        ResponseResult responseResult = this.postPage(cp.getPageId());
        if(!responseResult.isSuccess()){
            return new CmsPostPageResult(CommonCode.FAIL,null);
        }
        Optional<CmsSite> op = cmsSiteRepository.findById(cp.getSiteId());
        if(!op.isPresent()){
            throw new CustomException(CmsCode.CMS_SITE_ISNULL);
        }
        CmsSite cmsSite = op.get();
        //将请求路径返回
        String url =cmsSite.getSiteDomain()+cmsSite.getSiteWebPath()+cmsPage.getPageWebPath()+cmsPage.getPageName();
        return new CmsPostPageResult(CommonCode.SUCCESS,url);
    }

}
