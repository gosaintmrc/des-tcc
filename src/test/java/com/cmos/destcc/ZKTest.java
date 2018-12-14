package com.cmos.destcc;

import com.google.common.hash.BloomFilter;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import redis.clients.jedis.JedisPool;

/**
 * @Authgor: gosaint
 * @Description:
 * @Date Created in 21:35 2018/12/11
 * @Modified By:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ZKTest {
    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private ZooKeeper zooKeeper;
    @Test
    public void test(){
        System.out.println(zooKeeper);
    }

    @Autowired
    private CuratorFramework curatorFramework;

    /**
     * 同步模式下的创建永久节点
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void createNodeSync() throws KeeperException, InterruptedException{
        System.out.println(curatorFramework);

    }

    /**
     * 同步方式创建临时节点
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void createNodeSyncTemp() throws KeeperException, InterruptedException{
        final String path="/syncTemp";
        String nodePath = zooKeeper.create(path, "sync123".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        System.out.println(nodePath);
    }
    @Autowired
    private BloomFilter bloomFilter;


  /*  *//**
     * 异步模式下创建节点
     *//*
    private static void createNodeAsync() {
        String path = "/poype_node2";
        ZooKeeper zooKeeper = zkClient();
        zooKeeper.create(path, "123".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT,
                new MyStringCallBack(), "create");
    }
    static class MyStringCallBack implements AsyncCallback.StringCallback{
        @Override
        public void processResult(final int rc, final String path, final Object ctx, final String name) {
            System.out.println("异步创建回调结果：状态：" + rc +"；创建路径：" +
                    path + "；传递信息：" + ctx + "；实际节点名称：" + name);
        }
    }

    public static void getDataSync() throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        byte[] data = zkClient().getData("/poype_node", true, stat);
        System.out.println(new String(data));
        System.out.println(stat);
        *//**
         * cZxid = 156
         * ctime = 2018-11-05 15:12:32
         * mZxid = 156
         * mtime = 2018-11-05 15:12:32
         * pZxid = 156
         * cversion = 0
         * dataVersion = 0
         * aclVersion = 0
         * ephemeralOwner = 0
         * dataLength = 3
         *//*
    }
    public static void getDataAsync() {
        zkClient().getData("/poype_node", true, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int resultCode, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println(resultCode);
                System.out.println(path);
                System.out.println(ctx);
                System.out.println(new String(data));
                System.out.println(stat);
            }
        }, "异步获取节点的数据值");
    }

    *//**
     * 同步方式获取子节点列表
     * @throws KeeperException
     * @throws InterruptedException
     *//*
    public static void getChildrenSync() throws KeeperException, InterruptedException {
        List<String> childrenList = zkClient().getChildren("/", true);
        for(String child:childrenList){
            System.out.println(child);
        }
    }

    *//**
     * 同步的方式查看一个节点是否存在
     * @throws KeeperException
     * @throws InterruptedException
     *//*
    public static void existSync() throws KeeperException, InterruptedException {
        Stat stat = zkClient().exists("/poype_node2", true);
        if(stat!=null){
            System.out.println("节点存在"+stat);
        }
    }
    public static void setDataSync() throws KeeperException, InterruptedException {
        Stat stat = zkClient().setData("/poype_node2", "9888328".getBytes(), 1);
        System.out.println(stat);
    }
    public static void deleteSync() throws KeeperException, InterruptedException {
        zkClient().delete("/poype_node2",0);
    }*/

}
