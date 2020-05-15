package com.xuecheng.framework.domain.ucenter.ext;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by mrt on 2018/5/21.
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuthToken {
    String access_token;//访问token
    String refresh_token;//刷新token
    String jwt_token;//jwt令牌
}
