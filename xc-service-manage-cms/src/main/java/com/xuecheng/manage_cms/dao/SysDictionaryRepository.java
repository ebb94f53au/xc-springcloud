package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.system.SysDictionary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by Administrator.
 */
public interface SysDictionaryRepository extends MongoRepository<SysDictionary,String> {

    public List<SysDictionary> findByDType(String type);
}
