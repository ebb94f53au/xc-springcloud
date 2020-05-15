package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.request.QueryTemplateRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsTemplateResult;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.IOUtils;

import java.io.*;
import java.util.Optional;

/**
 * @author siyang
 * @create 2020-03-17 18:18
 */
@Service
public class TemplateService {
    @Autowired
    CmsTemplateRepository cmsTemplateRepository;
    @Autowired
    GridFsTemplate gridFsTemplate;

    //分页查询
    public QueryResponseResult findList(int page, int size, QueryTemplateRequest queryTemplateRequest){

        //条件匹配器
        // 页面名称模糊查询，需要自定义字符串的匹配器实现模糊查询
        ExampleMatcher exampleMatcher = ExampleMatcher.matching() .withMatcher("templateName", ExampleMatcher.GenericPropertyMatchers.contains());
        //条件值
        CmsTemplate cmsTemplate =new CmsTemplate();

        if (queryTemplateRequest == null){
            queryTemplateRequest = new QueryTemplateRequest();
        }
        //站点ID
        if(StringUtils.isNotEmpty(queryTemplateRequest.getSiteId())){
            cmsTemplate.setSiteId(queryTemplateRequest.getSiteId());
        }
        // 模板名称
        if(StringUtils.isNotEmpty(queryTemplateRequest.getTemplateName())){
            cmsTemplate.setTemplateName(queryTemplateRequest.getTemplateName());
        }
        //创建条件实例
        Example<CmsTemplate> of = Example.of(cmsTemplate, exampleMatcher);

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

        Page<CmsTemplate> all = cmsTemplateRepository.findAll(of,pr);
        //列表总数
        return new QueryResponseResult(CommonCode.SUCCESS,new QueryResult(all.getContent(),all.getTotalElements()));

    }

    /**
     * 根据id查找信息
     * @param id
     * @return
     */
    public CmsTemplateResult getById(String id){
        Optional<CmsTemplate> byId = cmsTemplateRepository.findById(id);
        //如果不存在 抛出异常
        if(!byId.isPresent()){
            throw new CustomException(CmsCode.CMS_TEMPLATE_ISNULL);
        }
        return new CmsTemplateResult(CommonCode.SUCCESS,byId.get());

    }

    /**
     * 添加cms信息
     * @param cmsTemplate
     * @return
     */
    public CmsTemplateResult add(MultipartFile multipartFile,CmsTemplate cmsTemplate){
        //验证外联表数据是否合法

        CmsTemplate cTemplate = cmsTemplateRepository.findBySiteIdAndTemplateNameAndTemplateParameter
                (cmsTemplate.getSiteId(),cmsTemplate.getTemplateName(),cmsTemplate.getTemplateParameter());
        if(cTemplate!=null)
            throw new CustomException(CmsCode.CMS_TEMPLATE_ISNOTNULL);
        //id默认由spring data自动生成
        cmsTemplate.setTemplateId(null);
        //获得templateFileId
        String templateFileId = this.addTemplate2Mongo(multipartFile, cmsTemplate.getTemplateName());
        cmsTemplate.setTemplateFileId(templateFileId);
        //保存
        cmsTemplateRepository.save(cmsTemplate);
        return new CmsTemplateResult(CommonCode.SUCCESS,cmsTemplate);
    }
    /**
     * 删除
     *
     * 开启事务
     * @param id
     * @return
     */
    @Transactional
    public ResponseResult delete(String id){
        try {
            CmsTemplate one = this.getById(id).getCmsTemplate();
            String templateFileId = one.getTemplateFileId();
            //删除分布式文件
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(templateFileId)));
            //删除保存信息
            cmsTemplateRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(CommonCode.FAIL);
        }

    }

    //添加模板到mongo
    private String addTemplate2Mongo(MultipartFile multipartFile,String templateName){
        try {
            byte[] bytes = multipartFile.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            ObjectId store = gridFsTemplate.store(inputStream, templateName,"");
            return store.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw  new CustomException(CmsCode.CMS_TEMPLATE_SAVE_FAIL);
        }

    }


}
