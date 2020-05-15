package com.xuecheng.framework.domain.ucenter.request;

import com.xuecheng.framework.model.request.RequestData;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * Created by admin on 2018/3/5.
 */
@Data
@ToString
public class LoginRequest extends RequestData {

    @NotBlank
    String username;
    @NotBlank
    String password;
    @NotBlank
    String verifycode;

}
