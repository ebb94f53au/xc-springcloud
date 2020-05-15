package com.xuecheng.auth.feign;

import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author siyang
 * @create 2020-04-03 17:02
 */
@FeignClient("XC-SERVICE-UCENTER")
@RequestMapping("/ucenter")
public interface UserClient {

    @GetMapping("/getuserext")
    public XcUserExt getUserext(@RequestParam("username") String username);

}
