package com.leyou.order.task;

import com.leyou.order.service.TbOrderService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CloseOrderTask {

    /**
     * 定时任务的频率，30分钟
     */
    private static final long TASK_INTERVAL = 1800000;
    /**
     * 定时任务的锁自动释放时间
     * 一般只要大于各服务器的时钟飘移时长+任务执行时长即可
     * 此处默认120秒
     */
    private static final long TASK_LEASE_TIME = 120;
    /**
     * 订单超时的期限，1小时
     */
    private static final int OVERDUE_SECONDS = 3600;

    private static final String LOCK_KEY = "close:order:task:lock";

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private TbOrderService orderService;
    /**
     * 定时任务
     */
    @Scheduled(fixedDelay = TASK_INTERVAL)
    public void closeOrder(){
//        1、获取分布式锁，只有拿到锁的定时任务才能往下执行
        RLock lock = redissonClient.getLock(LOCK_KEY);
        try {
            if(!lock.tryLock(0,TASK_LEASE_TIME, TimeUnit.SECONDS)){
                log.error("没有获取到锁");
                return ;
            }
            // 2、根据订单超时的期限，计算需要被找到的订单创建时间 的 阈值
            Date overTimeDate = DateTime.now().minusSeconds(OVERDUE_SECONDS).toDate();

//        3、调用orderService中的方法，更新订单，恢复库存
            orderService.closeOverTimeOrder(overTimeDate);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            //        4、释放锁
            lock.unlock();
        }


    }
}
