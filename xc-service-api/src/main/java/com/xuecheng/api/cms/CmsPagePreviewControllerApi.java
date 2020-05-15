package com.xuecheng.api.cms;

import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author siyang
 * @create 2020-03-10 18:00
 */
public interface CmsPagePreviewControllerApi {

    public void preview(String pageId);

}
