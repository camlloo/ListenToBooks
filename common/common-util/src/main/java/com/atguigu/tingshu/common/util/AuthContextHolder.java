package com.atguigu.tingshu.common.util;

/**
 * 获取当前用户信息帮助类
 */
public class AuthContextHolder {

    private static ThreadLocal<Long> userId = new ThreadLocal<Long>();
    private static ThreadLocal<String> username = new ThreadLocal<String>();

    public static void setUserId(Long _userId) {
        userId.set(_userId);
    }

    public static Long getUserId() {
        //TODO 暂时写为固定ID，做完登录后修改
        return 1L;
    }

    public static void removeUserId() {
        userId.remove();
    }

    public static void setUsername(String _username) {
        username.set(_username);
    }

    public static String getUsername() {
        return username.get();
    }

    public static void removeUsername() {
        username.remove();
    }

}
