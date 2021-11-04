package com.denglitong.zuulservice.filters;

/**
 * @author litong.deng@foxmail.com
 * @date 2021/11/3
 */
public enum FilterHeaderKey {
    CORRELATION_ID("tmx-correlation-id"),
    AUTH_TOKEN("tmx-auth-token"),
    USER_ID("tmx-user-id"),
    ORG_ID("tmx-org-id");

    private String name;

    FilterHeaderKey(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
