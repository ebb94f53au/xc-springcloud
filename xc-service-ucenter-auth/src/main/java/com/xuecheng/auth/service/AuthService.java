package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author siyang
 * @create 2020-04-02 17:50
 */
@Service
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;

    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;



    public AuthToken login(String username,String password,String clientId,String clientPwd){
       //登录认证
        AuthToken authToken = this.applyToken(username, password, clientId, clientPwd);
        //redis保存
        boolean b = this.saveToken(authToken.getAccess_token(), authToken, tokenValiditySeconds);
        if(!b){
            throw new  CustomException(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }

        return authToken;
    }
    //登录认证
    private AuthToken applyToken(String username,String password,String clientId,String clientPwd){
        //从注册中心中获得  认证微服务的相关信息
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);

        if (serviceInstance == null) {
            LOGGER.error("choose an auth instance fail");
            throw new  CustomException(AuthCode.AUTH_LOGIN_AUTHSERVER_NOTFOUND);
        }
        //拼写出url =》 http://localhost:posrt/auth/oauth/token
        String url =serviceInstance.getUri()+"/auth/oauth/token";

        //构建header
        MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        //httpbasic 认证
        headerMap.add("Authorization",httpbasic(clientId,clientPwd));

        //构建body
        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();
        //使用密码模式
        bodyMap.add("grant_type","password");
        bodyMap.add("username",username);
        bodyMap.add("password",password);


        //使用header和body 构建
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(bodyMap,headerMap);

        //设置400 401忽略报错
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //当响应的值为400或401时候也要正常响应，不要抛出异常
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });

        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);

        Map body = exchange.getBody();

        if(body == null || body.get("access_token") == null || body.get("refresh_token") == null || body.get("jti") == null){
            //jti是jwt令牌的唯一标识作为用户身份令牌
            LOGGER.error("login restTemplate is null");

            String error_description = (String) body.get("error_description");
            if(StringUtils.isNotEmpty(error_description)){
                if(error_description.equals("坏的凭证")){
                    throw new CustomException(AuthCode.AUTH_CREDENTIAL_ERROR);
                }else if(error_description.indexOf("UserDetailsService returned null")>=0){
                    throw new CustomException(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                }
            }
            throw new CustomException(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }

        //jti，作为用户的身份标识
        String access_token = (String) body.get("jti");
        //刷新令牌(jwt)
        String refresh_token = (String) body.get("refresh_token");
        String jwt_token = (String) body.get("access_token");

        return  new AuthToken(access_token,refresh_token,jwt_token);
    }

    //存储令牌到redis
    private boolean saveToken(String access_token,AuthToken authToken,long ttl){
        //令牌名称
        String name = "user_token:" + access_token;

        String content = JSON.toJSONString(authToken);
        //保存到令牌到redis
        stringRedisTemplate.boundValueOps(name).set(content,ttl, TimeUnit.SECONDS);
        //获取过期时间
        Long expire = stringRedisTemplate.getExpire(name);
        return expire>0;
    }

    //httpbasic构建
    private String httpbasic(String clientId,String clientSecret){
        //将客户端id和客户端密码拼接，按“客户端id:客户端密码”
        String string = clientId+":"+clientSecret;
        //进行base64编码
        byte[] encode = Base64.encode(string.getBytes());
        return "Basic "+new String(encode);
    }

    //退出
    public boolean delToken(String access_token){
        Boolean delete = stringRedisTemplate.delete("user_token:" + access_token);
        return delete;
    }

    //从redis中取数据
    public AuthToken getUserToken(String token){
        String userToken = "user_token:"+token;
        String userTokenString = stringRedisTemplate.opsForValue().get(userToken);
        if(userToken!=null){
            AuthToken authToken = null;
            try {
                authToken = JSON.parseObject(userTokenString, AuthToken.class);
            } catch (Exception e) {
                LOGGER.error("getUserToken from redis and execute JSON.parseObject error {}",e.getMessage());
                e.printStackTrace();
            }
            return authToken;
        }
            return null;
    }

}
