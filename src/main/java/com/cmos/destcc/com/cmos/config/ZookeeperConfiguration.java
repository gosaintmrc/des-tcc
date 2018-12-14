package com.cmos.destcc.com.cmos.config;

import java.io.IOException;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gosaint
 */
@Configuration
public class ZookeeperConfiguration {

    @Value("${zookeeper.connectURL}")
    private String connectURL;
    @Value("${zookeeper.sessionTimeoutMs}")
    private String sessionTimeoutMs;
    @Value("${zookeeper.connectionTimeoutMs}")
    private String connectionTimeoutMs;

    @Bean
    public ZooKeeper zkClient(){
        System.out.println(connectURL);
        final String connectString=connectURL;
        final int sessionTimeout=5000;
        ZooKeeper zooKeeper=null;
        try {
            zooKeeper=new ZooKeeper(connectString, sessionTimeout, new Watcher() {
                @Override
                public void process(final WatchedEvent watchedEvent) {
                    /** 判断是否和服务器之间取得了连接*/
                    if(watchedEvent.getState()==Event.KeeperState.SyncConnected) {
                        System.out.println("已经触发了" + watchedEvent.getType() + "事件！");
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        return zooKeeper;
    }

    @Bean
    public CuratorFramework getZk(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(connectURL)
                .retryPolicy(retryPolicy)
                .sessionTimeoutMs(Integer.parseInt(sessionTimeoutMs))
                .connectionTimeoutMs(Integer.parseInt(connectionTimeoutMs))
                .namespace("demo")
                .build();
        client.start();
        return client;
    }

 }
