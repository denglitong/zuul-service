package com.denglitong.zuulservice.context;

import org.apache.logging.log4j.util.Strings;

/**
 * @author litong.deng@foxmail.com
 * @date 2021/11/3
 */
public class UserContext {

    private String correlationId = Strings.EMPTY;
    private String authToken = Strings.EMPTY;
    private String userId = Strings.EMPTY;
    private String orgId = Strings.EMPTY;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}
