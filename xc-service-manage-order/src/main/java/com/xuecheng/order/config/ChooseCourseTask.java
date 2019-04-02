package com.xuecheng.order.config;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Component
public class ChooseCourseTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);
    /*
    * 每分钟执行任务，
    * 启动订单工程，
    * 观察定时发送消息日志，
    * 观察rabbitMQ队列中是否有消息
    * */
  @Autowired
    TaskService taskService;
  @Scheduled(fixedDelay = 60000)
  public void  sendChoosecourseTask(){
      //取出当前时间1分钟之前的时间
      Calendar calendar =new GregorianCalendar();
      calendar.setTime(new Date());
      calendar.add(GregorianCalendar.MINUTE,-1);
      Date time=calendar.getTime();
      List<XcTask> taskList = taskService.findTaskList(time, 1000);

      for(XcTask xcTask:taskList) {
          //发送选课消息
//版本号
          Integer version = xcTask.getVersion();
          String taskId = xcTask.getId();
          if (taskService.getTask(taskId, version) > 0) {

              taskService.publish(xcTask, xcTask.getMqRoutingkey(), xcTask.getMqExchange());
              LOGGER.info("send choose course task id:{}", taskId);
          }
      }
  }
}
