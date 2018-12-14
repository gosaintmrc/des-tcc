package com.cmos.destcc.com.cmos.config;

import java.util.List;

import com.cmos.destcc.com.cmos.domain.User;
import com.cmos.destcc.com.cmos.service.UserService;
import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gosaint
 * @Description:
 * @Date Created in 17:45 2018/12/13
 * @Modified By:
 */
@Configuration
public class BoomFilterConfig {

    @Autowired
    private UserService userService;

    @Bean
    public BloomFilter getBloomContainer(){
        //将数据库数据key保存在容器中
        List<User> userList = userService.getAllUser();
        System.out.println(userList.size()+"jjjjjjjj");
        System.out.println("线程"+Thread.currentThread().getName()+"去bloomFilter查询");
        if(userList==null || userList.size()==0){
            return null;
        }
        //创建布隆过滤器
        BloomFilter<CharSequence> bloomFilter = BloomFilter
                .create(Funnels.stringFunnel(Charsets.UTF_8), userList.size());
        for(User user:userList){
            bloomFilter.put(user.getUsername());
        }
        return bloomFilter;
    }
}
