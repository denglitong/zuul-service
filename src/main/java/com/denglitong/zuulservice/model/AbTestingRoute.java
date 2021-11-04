package com.denglitong.zuulservice.model;

/**
 * @author litong.deng@foxmail.com
 * @date 2021/11/3
 */
public class AbTestingRoute {

    private String serviceName;
    private String active;
    private String endPoint;
    private Integer weight;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
