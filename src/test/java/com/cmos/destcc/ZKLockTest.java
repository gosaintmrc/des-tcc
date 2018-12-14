package com.cmos.destcc;

import java.util.Collections;
import java.util.List;

import org.apache.curator.shaded.com.google.common.base.Strings;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author gosaint
 * @Description:
 * @Date Created in 21:05 2018/12/12
 * @Modified By:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ZKLockTest {

    @Autowired
    private ZooKeeper zooKeeper;

    /**
     * 获取锁
     * @return
     */
    public boolean tryLock() {
        try {
            /**
             * 1 创建临时有序节点,返回节点路径
             * 2 根据Root节点获取子节点
             * 3 对所有节点排序获取最小的节点
             */

            String path=zooKeeper.create("/zkLock_","".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
            List<String> childrenNode = zooKeeper.getChildren(path, false);
            Collections.sort(childrenNode);
            if(path!=null && childrenNode.get(0)!=null){
                System.out.println("线程"+Thread.currentThread().getName()+"get Lock===========>>>>>>>>>>");
                return true;
            }
            //下述代码是在释放锁之后，为了避免羊群效应。只让次节点获取锁
            for(int i=childrenNode.size()-1;i>0;i--){
                /*if(childrenNode.get(i).compareTo()){

                }*/
            }

        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 释放锁
     */
    public void unlock() {

    }

}
