package com.cmos.destcc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;

import com.cmos.destcc.com.cmos.config.JedisUtil;
import com.cmos.destcc.com.cmos.domain.User;
import com.cmos.destcc.com.cmos.service.UserService;
import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.google.common.util.concurrent.RateLimiter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import redis.clients.jedis.Jedis;

/**
 * @author gosaint
 * @Description:
 * @Date Created in 23:37 2018/11/30
 * @Modified By:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisCacheTest {
    @Resource
    private UserService userService;
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                   // userService.queryUserByIdToZk(55L);
                    userService.limitQPS("idss");
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


    @Test
    public void redisSetNx() throws Exception{
        userService.queryUserById(8L);
    }
    @Autowired
    private Jedis jedis;
    @Test
    public void jedisTest() throws Exception{
        jedis.set("bbb", "123");
        String bbb = jedis.get("bbb");
        System.out.println(bbb);
    }

    @Test
    public void jedisTests() throws Exception{
        Jedis jedis = JedisUtil.getJedis();
        System.out.println(jedis);
    }

    @Test
    public void testGetUser(){
        List<User> allUser = userService.getAllUser();
        System.out.println(allUser.size());
    }

   /* @Test
   public void testGetUser2(){
       User user = userService.queryUserByName("drge");
       System.out.println(user);
    }*/
   @Autowired
   private BloomFilter bloomFilter;
    @Test
    public void isExistsKey() throws Exception{
        ////这里存在问题：数据同步问题
        //ES elasticsearch-jdbc 独立三方插件 mysql和ES实时同步
        List<User> userList = userService.getAllUser();
        if(userList==null || userList.size()==0){
            System.out.println("没有数据");
        }
        BloomFilter<CharSequence> bloomFilter = BloomFilter
                .create(Funnels.stringFunnel(Charsets.UTF_8), userList.size());
        for(User user:userList){
            bloomFilter.put(user.getUsername());
        }
        boolean contain = bloomFilter.mightContain("的女");
        if(contain){
            System.out.println("包含热点KEY");
        }else {
            System.out.println("不包含热点KEY");
        }
    }

    /**
     * 令牌桶算法----限流
     */
    @Test
    public void bulkToRoken() throws Exception{
        //每秒限制0.5个并发 即2s放过一个请求
        RateLimiter limiter=RateLimiter.create(0.5);
        for(int i = 0;i< threadNum;i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cdl.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //此处限流
                    System.out.println("等待时间：" + limiter.acquire());
                    userService.limitQPS("RequestId"+dateFormate());
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

    //时间格式化
    public String dateFormate(){
        Date date=new Date();
        SimpleDateFormat format=new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(date.getTime());
    }
}
