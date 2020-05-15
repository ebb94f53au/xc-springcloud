package com.xuecheng.framework.domain.cms.response;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author siyang
 * @create 2020-03-22 15:05
 *
 * 页面Url= cmsSite.siteDomain+cmsSite.siteWebPath+ cmsPage.pageWebPath + cmsPage.pageName
 */
@Data
@NoArgsConstructor
public class CmsPostPageResult extends ResponseResult {
    String url;

    public CmsPostPageResult(ResultCode resultCode, String url) {
        super(resultCode);
        this.url = url;
    }
}
