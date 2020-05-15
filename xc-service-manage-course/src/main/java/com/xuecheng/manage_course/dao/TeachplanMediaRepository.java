package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.TeachplanMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author siyang
 * @create 2020-03-29 19:15
 */
public interface TeachplanMediaRepository extends JpaRepository<TeachplanMedia,String> {

    List<TeachplanMedia> findByCourseId(String courseId);
}
