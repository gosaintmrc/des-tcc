package com.cmos.destcc.com.cmos.service;

import java.util.List;

import com.cmos.destcc.com.cmos.domain.User;

/**
 * @author gosaint
 * @Description:
 * @Date Created in 15:05 2018/11/30
 * @Modified By:
 */
public interface UserService {
    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    User queryindUserById(Long id);

    void addUserById(User user);

    User queryUserById(final Long id);
    User queryUserById2(final Long id);
    User queryUserById3(final Long id);
    User queryUserByIdToZk(final  Long id);
    List<User> getAllUser();
    //User queryUserByName(String username);
    Long limitQPS(String key);
}
