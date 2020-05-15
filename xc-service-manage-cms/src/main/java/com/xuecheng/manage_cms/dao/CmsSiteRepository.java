package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author siyang
 * @create 2020-03-06 18:17
 */
public interface CmsSiteRepository extends MongoRepository<CmsSite,String> {

}
