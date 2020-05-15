package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author siyang
 * @create 2020-03-10 16:27
 */
@Service
public class CmsConfigService {
    @Autowired
    CmsConfigRepository cmsConfigRepository;

    //根据id查询配置管理信息
    public CmsConfig getConfigById(String id){
        Optional<CmsConfig> byId = cmsConfigRepository.findById(id);
        if(byId.isPresent()){
            return byId.get();
        }
        return null;
    }

}
