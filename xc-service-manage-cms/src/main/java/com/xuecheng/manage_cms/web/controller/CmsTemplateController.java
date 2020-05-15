package com.xuecheng.manage_cms.web.controller;

import com.xuecheng.api.cms.CmsTemplateControllerApi;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryTemplateRequest;
import com.xuecheng.framework.domain.cms.response.CmsTemplateResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author siyang
 * @create 2020-03-17 18:17
 */
@RestController
@RequestMapping("/cms/template")
public class CmsTemplateController implements CmsTemplateControllerApi {

    @Autowired
    TemplateService templateService;
    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") int page,@PathVariable("size") int size, QueryTemplateRequest queryTemplateRequest) {
        return templateService.findList(page,size,queryTemplateRequest);
    }

    @Override
    @GetMapping("/allList")
    public QueryResponseResult findAllList() {
        return templateService.findList(0,9999,null);
    }

    @Override
    @DeleteMapping("/del/{id}")
    public ResponseResult delete(@PathVariable("id") String id) {
        return templateService.delete(id);
    }

    @Override
    @PostMapping("/add")
    public CmsTemplateResult add(@RequestParam("file") MultipartFile multipartFile,@Validated CmsTemplate cmsTemplate) {
        return templateService.add(multipartFile,cmsTemplate);
    }
}
