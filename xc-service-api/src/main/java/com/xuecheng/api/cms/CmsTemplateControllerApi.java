package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.request.QueryTemplateRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsTemplateResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author siyang
 * @create 2020-03-17 18:09
 */
@Api(value="cms模板管理接口",description = "cms模板管理接口，提供页面的增、删、查")
public interface CmsTemplateControllerApi {
    @ApiOperation("分页查询模板列表")
    public QueryResponseResult findList(int page, int size, QueryTemplateRequest queryTemplateRequest);

    @ApiOperation("查询所有模板列表")
    public QueryResponseResult findAllList();

    @ApiOperation("添加模板")
    public CmsTemplateResult add(MultipartFile multipartFile, CmsTemplate cmsTemplate);

    @ApiOperation("通过ID删除模板")
    public ResponseResult delete(String id);
}
