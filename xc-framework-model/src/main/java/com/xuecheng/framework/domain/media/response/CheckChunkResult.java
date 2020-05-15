package com.xuecheng.framework.domain.media.response;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by admin on 2018/3/5.
 */
@Data
@ToString
@NoArgsConstructor
public class CheckChunkResult extends ResponseResult{

    public CheckChunkResult(ResultCode resultCode, boolean ifExist) {
        super(resultCode);
        this.ifExist = ifExist;
    }
    @ApiModelProperty(value = "文件分块存在标记", example = "true", required = true)
    boolean ifExist;
}
