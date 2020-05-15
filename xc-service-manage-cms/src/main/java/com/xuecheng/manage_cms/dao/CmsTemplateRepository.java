package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author siyang
 * @create 2020-03-06 18:17
 */
public interface CmsTemplateRepository extends MongoRepository<CmsTemplate,String> {

    public CmsTemplate findBySiteIdAndTemplateNameAndTemplateParameter(String siteId,String tempalteName,String templateParameter);
}
