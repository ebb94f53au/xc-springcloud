package com.xuecheng.framework.domain.learning.response;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.model.response.ResultCode;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author siyang
 * @create 2020-03-31 17:39
 */
public enum  LearningCode implements ResultCode {

    LEARNING_GETMEDIA_ERROR(false,23002,"获得媒资信息失败"),
    CHOOSECOURSE_USERISNULL(false,23003,"获得课程用户为空"),
    CHOOSECOURSE_TASKISNULL(false,23004,"获得课程任务为空");

    //操作代码
    @ApiModelProperty(value = "操作是否成功", example = "true", required = true)
    boolean success;

    //操作代码
    @ApiModelProperty(value = "操作代码", example = "22001", required = true)
    int code;
    //提示信息
    @ApiModelProperty(value = "操作提示", example = "操作过于频繁！", required = true)
    String message;

    private LearningCode(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    private static final ImmutableMap<Integer, LearningCode> CACHE;

    static {
        final ImmutableMap.Builder<Integer, LearningCode> builder = ImmutableMap.builder();
        for (LearningCode learningCode : values()) {
            builder.put(learningCode.code(), learningCode);
        }
        CACHE = builder.build();
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
