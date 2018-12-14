package com.cmos.destcc;


import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.cmos.destcc.com.cmos.domain.User;
import com.cmos.destcc.com.cmos.service.UserService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DesTccApplicationTests {

    private final static Logger logger = LoggerFactory.getLogger(DesTccApplicationTests.class);
    CountDownLatch countDownLatch = new CountDownLatch(1);
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    @Resource
    private DruidDataSource dataSource;
    @Resource
    private RedisTemplate<Object,Object> redisTemplate;

    @Autowired
    private UserService userService;
    @Test
    public void contextLoads() throws SQLException {

        DruidPooledConnection connection = dataSource.getConnection();
        int maxActive = dataSource.getMaxActive();
        logger.info("最大连接数"+maxActive);
    }

    @Test
    public void userServiceTest() throws SQLException {
        final User user=new User();
        user.setEmail("gosaintmrc@aliyun.com");
        user.setPassword("!QAZ1qaz");
        user.setUsername("caozg");
        userService.addUserById(user);
    }

    @Test
    public void userServiceTestId() throws SQLException {
        User user = userService.queryindUserById(1L);
        logger.info("用户："+user);

    }

    @Test
    public void testRedis() throws Exception{
        /** key-value存储*/
        redisTemplate.opsForValue().set("caozg","a");
        String caozg = (String)redisTemplate.opsForValue().get("caozg");
        logger.info(caozg);
    }

    @Test
    public void testRedisCache() throws Exception{
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("======");
                        /** 等待所有线程启动，模拟并发*/
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("99999999999");
                    for(int i=0;i<10;i++){
                        User user = userService.queryindUserById(1L);
                        logger.info("user="+user);
                    }
                    countDownLatch.countDown();
                }
            });
        }
}
