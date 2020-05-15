package com.xuecheng.order.mq;

import com.rabbitmq.client.Channel;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author siyang
 * @create 2020-04-09 17:37
 */
@Component
public class ChooseCourseTask {
    private final static Logger LOGGER= LoggerFactory.getLogger(ChooseCourseTask.class);
    @Autowired
    TaskService taskService;

    //一分钟执行一次
    @Scheduled(fixedDelay = 60000)
    public void sendChoosecourseTask (){
        //构建一分钟前
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE,-1);
        Date time = calendar.getTime();

        List<XcTask> taskList = taskService.findTaskList(time, 1000);

        //发送选课消息
        for (XcTask xcTask : taskList) {
            String id = xcTask.getId();
            Integer version = xcTask.getVersion();
            //乐观锁
            if(taskService.getTask(id,version)>0){
                taskService.publish(id,xcTask.getMqExchange(),xcTask.getMqRoutingkey());
                LOGGER.info("send choose course task id:{}",xcTask.getId());
            }
        }
    }
    /*** 接收选课响应结果 */
    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE})
    public void receiveFinishChoosecourseTask(XcTask task, Message message, Channel channel) throws IOException {
        LOGGER.info("receiveChoosecourseTask...{}",task.getId());
        //接收到 的消息id
        String id = task.getId();
        //删除任务，添加历史任务
        taskService.finishTask(id);
    }
}
