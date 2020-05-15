package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author siyang
 * @create 2020-03-14 16:48
 */
@Api(value="cms课程分类接口",description = "cms课程分类管理")
public interface CategoryControllerApi {
    @ApiOperation("查询分类")
    public CategoryNode findList();
}
