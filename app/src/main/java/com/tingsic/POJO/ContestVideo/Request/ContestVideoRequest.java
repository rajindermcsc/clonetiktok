package com.tingsic.POJO.ContestVideo.Request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ContestVideoRequest {

    @SerializedName("request")
    @Expose
    private Request request;
    @SerializedName("service")
    @Expose
    private String service;

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

}
