package com.xuecheng.framework.domain.cms.request;

import com.xuecheng.framework.model.request.RequestData;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author siyang
 * @create 2020-03-17 18:14
 */
@Data
public class QueryTemplateRequest  extends RequestData {

    @ApiModelProperty("站点id")
    private String siteId;
    @ApiModelProperty("模板名称")
    private String templateName;
    @ApiModelProperty("模版参数")
    private String templateParameter;
}
