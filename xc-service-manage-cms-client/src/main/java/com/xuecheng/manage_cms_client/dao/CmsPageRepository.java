package com.xuecheng.manage_cms_client.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author siyang
 * @create 2020-03-06 18:17
 */
public interface CmsPageRepository extends MongoRepository<CmsPage,String> {

}
