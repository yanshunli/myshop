package com.qf.aop;

import com.qf.entity.User;

public class UserHolder {

    private static ThreadLocal<User> user = new ThreadLocal<>();

    public static boolean isLogin(){
        return user.get() != null;
    }

    public static void setUser(User user){
        UserHolder.user.set(user);
    }

    public static User getUser(){
        return user.get();
    }

}
