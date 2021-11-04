package com.denglitong.zuulservice.filters;

/**
 * @author litong.deng@foxmail.com
 * @date 2021/11/3
 */
public enum FilterType {
    PRE("pre"),
    POST("post"),
    ROUTE("route");

    String value;

    FilterType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
