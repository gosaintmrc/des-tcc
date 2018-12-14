package com.cmos.destcc;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

import com.cmos.destcc.com.cmos.domain.User;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Authgor: gosaint
 * @Description:
 * @Date Created in 15:39 2018/12/1
 * @Modified By:
 */
public class FutureTaskConnTest {

    private ConcurrentHashMap<Long, FutureTask<User>> userMaps = new ConcurrentHashMap<Long, FutureTask<User>>();
    private final static Logger logger = LoggerFactory.getLogger(DesTccApplicationTests.class);
    public User getUserByCache(Long id) throws Exception {
        FutureTask<User> userTask = userMaps.get(id);
        if (userTask != null) {
            synchronized (this){
                logger.info("线程"+Thread.currentThread().getName()+"从缓存中获取数据");
            }
            return userTask.get();
        } else {
            Callable<User> callable = new Callable<User>() {
                @Override
                public User call() throws Exception {
                    return getUser();
                }
            };
            FutureTask<User> newTask = new FutureTask<User>(callable);
            userTask = userMaps.putIfAbsent(id, newTask);
            if (userTask == null) {
                userTask = newTask;
                userTask.run();
            }
            synchronized (this){
                logger.info("线程"+Thread.currentThread()+"从数据库中获取数据");
            }
            return userTask.get();
        }
    }

    //获取用户
    private User getUser() {
        User user=new User();
        user.setUsername("caozg");
        user.setPassword("1222");
        user.setEmail("shs@aks.com");
        user.setId(2L);
        return user;
    }

    //并发数
    private static final int threadNum = 20;
    //倒计时数 用于制造线程的并发执行
    private static CountDownLatch cdl = new CountDownLatch(threadNum);
    @Test
    public void test() {
        for(int i = 0;i< threadNum;i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cdl.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        getUserByCache(2L);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            cdl.countDown();
        }
        try {
            //主线程 等待子线程执行完
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
