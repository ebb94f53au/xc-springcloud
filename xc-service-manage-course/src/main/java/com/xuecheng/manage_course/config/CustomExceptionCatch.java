package com.xuecheng.manage_course.config;

import com.xuecheng.framework.exception.ExceptionCatch;
import com.xuecheng.framework.model.response.CommonCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * @author siyang
 * @create 2020-04-06 19:19
 */

@ControllerAdvice
public class CustomExceptionCatch extends ExceptionCatch {

    /**
     * 这段写在这主要是AccessDeniedException是security的一个类。如果此类的父类import了这个异常
     *  需要导入security的包。会自动对common包进行保护。从而所有调用common的包都会被保护
     */
    static {
        builder.put(AccessDeniedException.class, CommonCode.UNAUTHORISE);
    }
}
