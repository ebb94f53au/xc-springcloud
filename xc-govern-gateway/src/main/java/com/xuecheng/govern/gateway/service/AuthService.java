package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author siyang
 * @create 2020-04-05 19:06
 */
@Service
public class AuthService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //查询身份令牌
    public String getTokenFromCookie(HttpServletRequest request){
        Map<String, String> uid = CookieUtil.readCookie(request, "uid");
        String access_token = uid.get("uid");
        if(StringUtils.isEmpty(access_token)){
            return null;
        }
        return access_token;
    }
    //从header中查询jwt令牌
    public String getJwtFromHeader(HttpServletRequest request){
        String authorization = request.getHeader("Authorization");
        if(!StringUtils.isEmpty(authorization)&&authorization.startsWith("Bearer ")){
            return authorization;
        }
        return null;
    }
    //查询令牌的有效期
    public long getExpire(String access_token) {
        String key = "user_token:"+access_token;
        Long expire = stringRedisTemplate.getExpire(key);
        return expire;
    }
}
