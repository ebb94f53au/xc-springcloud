package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author siyang
 * @create 2020-04-03 16:43
 */
public interface XcCompanyUserRepository extends JpaRepository<XcCompanyUser, String> {

    //根据用户id查询所属企业id
    XcCompanyUser findByUserId(String userId);
}
