package com.denglitong.zuulservice.context;

import org.springframework.util.Assert;

/**
 * @author litong.deng@foxmail.com
 * @date 2021/11/3
 */
public class UserContextHolder {

    private static final ThreadLocal<UserContext> userContext = new ThreadLocal<>();

    public static final UserContext getContext() {
        if (userContext.get() == null) {
            userContext.set(createEmptyContext());
        }
        return userContext.get();
    }

    public static void setContext(UserContext context) {
        Assert.notNull(context, "Only non-null UserContext instance are permitted");
        userContext.set(context);
    }

    public static final UserContext createEmptyContext() {
        return new UserContext();
    }
}
