package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author siyang
 * @create 2020-04-03 16:43
 */
public interface XcUserRepository extends JpaRepository<XcUser, String> {
    XcUser findXcUserByUsername(String username);
}
