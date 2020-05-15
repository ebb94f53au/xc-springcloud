package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created by Administrator.
 */
@Mapper
public interface CategoryMapper {

   public CategoryNode selectList();

   //定义方法根据课程id和父结点id查询出结点列表，可以使用此方法实现查询根结点
   public List<Teachplan> findByCourseidAndParentid(String courseId, String parentId);
}
