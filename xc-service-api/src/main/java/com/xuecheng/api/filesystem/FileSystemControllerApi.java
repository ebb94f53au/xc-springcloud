package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author siyang
 * @create 2020-03-16 16:56
 */
@Api(value = "文件系统管理接口",description = "提供文件系统管理接口的管理、查询功能")
public interface FileSystemControllerApi {

    /*** 上传文件
     *  @param multipartFile 文件
     *  @param filetag 文件标签
     *  @param businesskey 业务key
     *  @param metadata 元信息,json格式
     *  @return
     **/
    @ApiOperation(value="文件系统上传接口")
    public UploadFileResult upload(MultipartFile multipartFile, String filetag, String businesskey, String metadata);


}
