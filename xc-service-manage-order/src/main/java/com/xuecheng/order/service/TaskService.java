package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    @Autowired
    XcTaskRepository xcTaskRepository;
    @Autowired
    RabbitTemplate rabbitTemplate;

    //取出前n条任务,取出指定时间之前处理的任务
    public List<XcTask> findTaskList(Date updateTime, int n) {
        Pageable pageable = new PageRequest(0, n);
        Page<XcTask> xctask = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        //返回task
        return xctask.getContent();
    }

    /*
     * 向mq发送消息
     * @param xcTask 任务对象
     * @param ex 交换机id
     * @param routingKey
     * */
@Transactional
    public void publish(XcTask xcTask, String ex,String routingKey){
    String taskId = xcTask.getId();
    Optional<XcTask> optionalTask = xcTaskRepository.findById((taskId));
    if(optionalTask.isPresent()){
        XcTask task = optionalTask.get();
        rabbitTemplate.convertAndSend(ex,routingKey,task);
        //更新任务时间
        xcTask.setUpdateTime(new Date());
        xcTaskRepository.save(xcTask);
    }
    }
    /*
    * 乐观锁
    * */
    @Transactional
    public int getTask(String taskId,int version){
        int i = xcTaskRepository.updateTaskVersion(taskId, version);
        return i;
    }


}

