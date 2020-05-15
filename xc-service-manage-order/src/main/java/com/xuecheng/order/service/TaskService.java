package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author siyang
 * @create 2020-04-09 17:31
 */
@Service
public class TaskService {
    @Autowired
    XcTaskRepository xcTaskRepository;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    //取出前n条任务,取出指定时间之前处理的任务
    public List<XcTask> findTaskList(Date updateTime, int n){

        PageRequest pageRequest = new PageRequest(0, 5);
        Page<XcTask> byUpdateTimeBefore = xcTaskRepository.findByUpdateTimeBefore(pageRequest, updateTime);

        return byUpdateTimeBefore.getContent();
    }

    /**
     * 发布信息
     * @param taskId
     * @param exchange
     * @param routingKey
     */
    @Transactional
    public void publish(String taskId,String exchange,String routingKey){

        Optional<XcTask> taskOptional = xcTaskRepository.findById(taskId);
        if(taskOptional.isPresent()){

            XcTask xcTask = taskOptional.get();
            //发送信息
            rabbitTemplate.convertAndSend(exchange,routingKey,xcTask);
            //修改更新时间并保存。
            xcTask.setUpdateTime(new Date());
            xcTaskRepository.save(xcTask);

        }
    }

    /**
     * 乐观锁 版本更新检测
     * @param taskId
     * @param version
     * @return
     */
    @Transactional
    public int getTask(String taskId,int version){
        int i = xcTaskRepository.updateTaskVersion(taskId, version);
        return i;
    }

    //删除任务
    @Transactional
    public void finishTask(String taskId){
        Optional<XcTask> taskOptional = xcTaskRepository.findById(taskId);
        if(taskOptional.isPresent()){
            XcTask xcTask = taskOptional.get();
            xcTask.setDeleteTime(new Date());
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask, xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
            xcTaskRepository.delete(xcTask);
        }
    }
}
