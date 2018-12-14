package com.cmos.destcc.com.cmos.dao;

import java.util.List;

import com.cmos.destcc.com.cmos.domain.User;

/**
 * @Authgor: gosaint
 * @Description:
 * @Date Created in 14:57 2018/11/30
 * @Modified By:
 */
public interface UserMapper {

    User findUserById(Long id);

    void addUserById(User user);

    List<User> getAll();

    User queryByName(String username);
}
