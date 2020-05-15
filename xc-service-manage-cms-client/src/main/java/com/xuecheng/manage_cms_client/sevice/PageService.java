package com.xuecheng.manage_cms_client.sevice;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import sun.nio.ch.IOUtil;

import java.io.*;
import java.util.Optional;

/**
 * @author siyang
 * @create 2020-03-12 16:20
 */
@Service
public class PageService {

    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    CmsSiteRepository cmsSiteRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;

    //将页面html保存到页面物理路径
    public void savePageToServerPath(String pageId){
        Optional<CmsPage> byId = cmsPageRepository.findById(pageId);
        if(!byId.isPresent()){
            throw new CustomException(CmsCode.CMS_GENERATEHTML_ISNULL);
        }
        CmsPage cmsPage = byId.get();
        //得到html文件的id
        String htmlFileId = cmsPage.getHtmlFileId();
        //得到文件
        InputStream inputStream = this.getFileById(htmlFileId);

        if(inputStream == null){
            throw new CustomException(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        //页面所属站点
        CmsSite cmsSite = this.getCmsSiteById(cmsPage.getSiteId());
        // 页面物理路径
        String pagePath = cmsSite.getSitePhysicalPath() + cmsPage.getPagePhysicalPath() + cmsPage.getPageName();
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(new File(pagePath));
            //复制到物理路径
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                //关闭通道
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    //根据文件id获取文件内容
    public InputStream getFileById(String fileId){
        if(StringUtils.isEmpty(fileId)){
            throw new CustomException(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        InputStream inputStream=null;
        GridFSFile gfs = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gfs.getObjectId());
        GridFsResource gridFsResource = new GridFsResource(gfs, gridFSDownloadStream);
        try {
            inputStream = gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputStream;

    }


    //根据站点id得到站点
    public CmsSite getCmsSiteById(String siteId){
        Optional<CmsSite> byId = cmsSiteRepository.findById(siteId);
        if(byId.isPresent()){
            CmsSite cmsSite = byId.get();
            return cmsSite;
        }
        return null;
    }
}
