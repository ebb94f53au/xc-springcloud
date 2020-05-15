package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author siyang
 * @create 2020-03-29 16:46
 */
@Component
public class MediaProcessTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaProcessTask.class);

    @Value("${xc-service-manage-media.video-location}")
    String videoLocation;
    @Value("${xc-service-manage-media.ffmpeg-path}")
    String ffmpegPath;

    @Autowired
    MediaFileRepository mediaFileRepository;


    @RabbitListener(queues = {"${xc-service-manage-media.mq.queue-media-video-processor}"})
    public void receiveMediaProcessTask(String msg) throws IOException {
        //解析json
        Map map = JSON.parseObject(msg, Map.class);
        String mediaId = (String) map.get("mediaId");

        Optional<MediaFile> optionalMediaFile = mediaFileRepository.findById(mediaId);
        if (!optionalMediaFile.isPresent()) {

            return;
        }
        MediaFile mediaFile = optionalMediaFile.get();
        //媒资转换格式mp4
        MediaFile mediaChange = this.mediaChange(mediaFile);
        if (mediaChange == null) {
            return;
        }
        MediaFile mediaFile1 = this.mp4ToM3u8(mediaChange);
        if(mediaFile1 ==null){
            return ;
        }

    }

    //将MP4转换为m3u8
    private MediaFile mp4ToM3u8(MediaFile mediaFile) {

        //构建路径
        String video_path = videoLocation + mediaFile.getFilePath() + mediaFile.getFileId() + ".mp4";
        String m3u8_name = mediaFile.getFileId() + ".m3u8";
        String m3u8folder_path = videoLocation + mediaFile.getFilePath()+"hls/";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpegPath, video_path, m3u8_name, m3u8folder_path);
        //生成m3u8
        String res = hlsVideoUtil.generateM3u8();
        if (res == null || !res.equals("success")) {
            //失败写入状态
            mediaFile.setProcessStatus("303003");
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(res);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return null;
        }
        //获得ts文件目录
        List<String> ts_list = hlsVideoUtil.get_ts_list();

        //将状态成功 和  ts文件目录写入数据库
        mediaFile.setProcessStatus("303002");
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setErrormsg(res);
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        mediaFile.setFileUrl(mediaFile.getFilePath()+"hls/"+m3u8_name);
        mediaFileRepository.save(mediaFile);


        return mediaFile;
    }

    //媒资数据转换mp4
    private MediaFile mediaChange(MediaFile mediaFile) {

        if (mediaFile.getMimeType().contains("avi")) {
            //如果是avi则进行处理 设置状态为未处理
            mediaFile.setProcessStatus("303001");
            mediaFileRepository.save(mediaFile);
        } else {
            //如果文件格式不为avi 设置为无需处理（暂时）
            mediaFile.setProcessStatus("303004");
            mediaFileRepository.save(mediaFile);
            return null;
        }
        //生成mp4
        String videoPath = videoLocation + mediaFile.getFilePath() + mediaFile.getFileName();
        String mp4Name = mediaFile.getFileId() + ".mp4";
        String mp4FolderPath = videoLocation + mediaFile.getFilePath();

        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegPath, videoPath, mp4Name, mp4FolderPath);
        //生成结果
        String res = mp4VideoUtil.generateMp4();
        if (res == null || !res.equals("success")) {
            //失败写入状态
            mediaFile.setProcessStatus("303003");
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(res);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return null;
        }
        return mediaFile;

    }
}
