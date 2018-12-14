package com.cmos.destcc.com.cmos.config;
 
import org.redisson.api.RLock;
 
import java.util.concurrent.TimeUnit;
 
/**
 * xuhaixing
 * 2018/7/2 17:56
 **/
public interface DistributedLocker {
    RLock lock(String lockKey);
    RLock lock(String lockKey,int timeout);
    RLock lock(String lockKey, TimeUnit unit,int timeout);
    boolean tryLock(String lockKey,TimeUnit unit,int waitTime,int leaseTime);
    void unlock(String lockKey);
    void unlock(RLock lock);
}
