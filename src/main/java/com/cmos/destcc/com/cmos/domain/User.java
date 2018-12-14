package com.cmos.destcc.com.cmos.domain;

import java.io.Serializable;

/**
 * @author gosaint
 * @Description:
 * @Date Created in 14:55 2018/11/30
 * @Modified By:
 */
public class User implements Serializable {

    private Long id;
    private String username;
    private String password;
    private String email;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
