package com.nowcoder.community.utils;


import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/*
*  持有用户信息，用于代替session对象
*  使用ThreadLocal<T>实现
* */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    // 清理
    public void clear() {
        users.remove();
    }
}
