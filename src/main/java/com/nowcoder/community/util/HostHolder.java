package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @Date: create in 15:47 2023/3/10
 * @describe: 容器,持有用户的信息   用于代替session对象
 */

@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUsers(User user){
        users.set(user);
    }
    public User getUser(){
        return users.get();
    }

    //清除user线程
    public void clear(){
        users.remove();
    }
}
