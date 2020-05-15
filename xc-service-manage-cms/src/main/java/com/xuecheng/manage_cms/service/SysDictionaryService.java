package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.dao.SysDictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author siyang
 * @create 2020-03-13 17:59
 */
@Service
public class SysDictionaryService {


    @Autowired
    SysDictionaryRepository sysDictionaryRepository;

    public SysDictionary findByType(String type){
        List<SysDictionary> byDType = sysDictionaryRepository.findByDType(type);
        return byDType.get(0);
    }


}
