package com.leyou.common.threadlocatls;

public class UserHolder {

    private static final ThreadLocal<Long> tl = new ThreadLocal();

    public static void setUser(Long userId){
        tl.set(userId);
    }

    public static Long getUser(){
        return tl.get();
    }

    public static void deleteUser(){
        tl.remove();
    }
}
