package com.xuecheng.framework.domain.cms;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;

/**
 * @Author: mrt.
 * @Description:
 * @Date:Created in 2018/1/24 10:04.
 * @Modified By:
 */
@Data
@ToString
@Document(collection = "cms_template")
public class CmsTemplate {
    //模版ID
    @Id
    private String templateId;
    //站点ID
    @NotBlank
    private String siteId;
    //模版名称
    @NotBlank
    private String templateName;
    //模版参数
    private String templateParameter;

    //模版文件Id
    private String templateFileId;
}
