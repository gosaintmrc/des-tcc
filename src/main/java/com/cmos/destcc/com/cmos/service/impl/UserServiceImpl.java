package com.cmos.destcc.com.cmos.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.cmos.destcc.com.cmos.config.RedisLock;
import com.cmos.destcc.com.cmos.dao.UserMapper;
import com.cmos.destcc.com.cmos.domain.User;
import com.cmos.destcc.com.cmos.service.UserService;
import com.google.common.hash.BloomFilter;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author gosaint
 * @Description:
 * @Date Created in 15:06 2018/11/30
 * @Modified By:
 */
@Service
public class UserServiceImpl implements UserService {

    private final static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String SET_NX = "NX";
    private static final String EXPIRE_TIME = "PX";
    private static final String GET_LOCK_SUCCESS = "OK";
    private static final String LOCK_SUCCESS ="1" ;
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private Jedis jedis;
    @Autowired
    private JedisPool jedisPool;

    @Override
    public User queryindUserById(final Long id) {
        User user = (User) redisTemplate.opsForHash().get("USER", id);
        if (user == null) {
            logger.info("缓存中无该数据,线程" + Thread.currentThread().getName() + "去数据库查询");
            user = userMapper.findUserById(id);
            if (user == null) {
                logger.info("数据库无该数据");
                return null;
            } else {
                redisTemplate.opsForHash().put("USER", id, user);
                logger.info("数据库查询到了数据，并且放入Redis缓存");
                logger.info("线程" + Thread.currentThread().getName() + "从数据库中获取数据");
                return user;
            }
        } else {
            logger.info("线程" + Thread.currentThread().getName() + "从缓存中获取数据");
            return user;
        }
    }

    @Override
    public void addUserById(final User user) {
        userMapper.addUserById(user);
    }

    @Override
    public User queryUserById(final Long id) {
        User user = (User) redisTemplate.opsForHash().get("USER", id);
        if(user!=null){
            logger.info("线程" + Thread.currentThread().getName() + "从缓存中获取数据");
            return user;
        }
        RedisLock lock = new RedisLock(redisTemplate, "ID"+id, 1000, 2000);
        try {
            if(lock.lock()){
                logger.info(" 缓存中无该数据,线程" + Thread.currentThread().getName() + "去数据库查询");
                user = userMapper.findUserById(id);
                redisTemplate.opsForHash().put("USER", id, user);
                lock.unlock();
                return user;
            }else {
                Thread.sleep(3000);
                /** 模拟网络延迟*/
                user = (User) redisTemplate.opsForHash().get("USER", id);
                if(user!=null){
                    logger.info("线程" + Thread.currentThread().getName() + "再次从缓存中读取数据OK");
                    return user;
                }else {
                    logger.info("线程" + Thread.currentThread().getName() + "再次从缓存中读取数据failed");
                }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       return null;
    }

    /**
     * 加锁
     * @param jedis
     * @param lockKey
     * @param requestId
     * @param expireTime
     * @return
     */
    public  boolean tryGetDistributedLock(Jedis jedis, String lockKey, String requestId, int expireTime) {
        String result = jedis.set(lockKey, requestId, SET_NX, EXPIRE_TIME, expireTime);
//        jedis.setn
        if (GET_LOCK_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }

    @Autowired
    private RedissonClient redissonClient;
    @Override
    public User queryUserById2(final Long id) {
        User user = (User) redisTemplate.opsForHash().get("USER", id);
        if(user!=null){
            logger.info(" 线程" + Thread.currentThread().getName() + "从缓存中获取数据");
            return user;
        }
        RLock rLock = redissonClient.getLock("USER@");
        try {
            boolean flag = rLock.tryLock(3, 10, TimeUnit.SECONDS);
            if(flag){
                logger.info("缓存中无该数据,线程" + Thread.currentThread().getName() + "去数据库查询");
                user = userMapper.findUserById(id);
                redisTemplate.opsForHash().put("USER", id, user);
                rLock.unlock();
                return user;
            }else {
                Thread.sleep(3000);
                /** 模拟网络延迟*/
                user = (User) redisTemplate.opsForHash().get("USER", id);
                if(user!=null){
                    logger.info("线程" + Thread.currentThread().getName() + "再次从缓存中读取数据OK");
                    return user;
                }else {
                    logger.info("线程" + Thread.currentThread().getName() + "再次从缓存中读取数据failed");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final Long RELEASE_SUCCESS = 1L;
    public static boolean releaseDistributedLock(Jedis jedis, String lockKey, String requestId) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        if (RELEASE_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }


    @Override
    public User queryUserById3(final Long id) {
        User user = (User) redisTemplate.opsForHash().get("USER", id);
        if(user!=null){
            logger.info(" 线程" + Thread.currentThread().getName() + "从缓存中获取数据");
            return user;
        }
        String requestId = getUUID();
        //Jedis jd = jedisPool.getResource();
        Jedis jd = jedis;
        try {
            if(tryGetDistributedLock(jd,"ID"+id,requestId,3000)){
                logger.info(" 缓存中无该数据,线程" + Thread.currentThread().getName() + "去数据库查询");
                user = userMapper.findUserById(id);
                redisTemplate.opsForHash().put("USER", id, user);
                releaseDistributedLock(jd,"ID"+id,requestId);
                return user;
            }else {
                Thread.sleep(3000);
                /** 模拟网络延迟*/
                user = (User) redisTemplate.opsForHash().get("USER", id);
                if(user!=null){
                    logger.info("线程" + Thread.currentThread().getName() + "再次从缓存中读取数据OK");
                    return user;
                }else {
                    logger.info("线程" + Thread.currentThread().getName() + "再次从缓存中读取数据failed");
                }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Autowired
    private CuratorFramework curatorFramework;
    private static final String path = "/myLockToot";
    @Override
    public User queryUserByIdToZk(final Long id) {
        //获取分布式锁
        InterProcessMutex lock=new InterProcessMutex(curatorFramework,path);
        boolean acquire = false;
        try {
            acquire = lock.acquire(6, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(acquire){
            User user = (User) redisTemplate.opsForHash().get("USER", id);
            if(user!=null){
                logger.info("线程" + Thread.currentThread().getName() + "从缓存中获取数据");
                return user;
            }
            //获取锁成功
            System.out.println("线程"+Thread.currentThread().getName()+"获取锁成功");
            logger.info(" 缓存中无该数据,线程" + Thread.currentThread().getName() + "去数据库查询");
            user = userMapper.findUserById(id);
            redisTemplate.opsForHash().put("USER", id, user);

            int i = new Random().nextInt(10000);

            try {
                System.out.println("线程"+Thread.currentThread().getName()+"释放锁");
                lock.release();
                return user;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            System.out.println("线程"+Thread.currentThread().getName()+"没有获取到锁");
        }
        return null;
    }

    @Override
    public List<User> getAllUser() {
        return userMapper.getAll();
    }

    @Override
    public Long limitQPS(String key) {
        /*RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        System.out.println("==========>"+entityIdCounter.getAndIncrement());
        //数据复位
        entityIdCounter.getAndDecrement();*/
        System.out.println("===============>"+key);
        return null;
    }
   /*@Autowired
    private BloomFilter bloomFilter;
    @Override
    public User queryUserByName(final String username) {
        System.out.println("================>");
        System.out.println(bloomFilter);
        return null;
    }*/

    public static String getUUID() {
        UUID uuid =UUID.randomUUID();
        String str = uuid.toString();
        // 去掉"-"符号
        String temp = str.substring(0, 8) +str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) +str.substring(24);
        return  temp;
    }


}
