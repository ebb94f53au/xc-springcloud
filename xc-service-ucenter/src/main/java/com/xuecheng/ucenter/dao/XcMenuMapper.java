package com.xuecheng.ucenter.dao;


import com.xuecheng.framework.domain.ucenter.XcMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author siyang
 * @create 2020-04-06 18:34
 */
@Mapper
public interface XcMenuMapper {

    //通过userId得到所有权限
    List<XcMenu> selectPermissionByUserId(String userId);
}
