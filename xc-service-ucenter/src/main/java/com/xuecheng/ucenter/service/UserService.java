package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author siyang
 * @create 2020-04-03 16:44
 */
@Service
public class UserService {

    @Autowired
    XcUserRepository xcUserRepository;
    @Autowired
    XcCompanyUserRepository xcCompanyUserRepository;
    @Autowired
    XcMenuMapper xcMenuMapper;

    //通过用户名得到用户信息
    public XcUserExt getUserext(String username) {

        //获得公司信息
        XcUser xcUser = xcUserRepository.findXcUserByUsername(username);
        if(xcUser==null){
            return null;
        }

        XcUserExt xcUserExt = new XcUserExt();
        //复制信息
        BeanUtils.copyProperties(xcUser,xcUserExt);

        //公司信息
        XcCompanyUser companyUser = xcCompanyUserRepository.findByUserId(xcUser.getId());
        if(companyUser!=null){
            xcUserExt.setCompanyId(companyUser.getCompanyId());
        }
        //获得权限信息

        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUser.getId());
        if(xcMenus.size()!=0){
            xcUserExt.setPermissions(xcMenus);
        }

        return xcUserExt;

    }


}
