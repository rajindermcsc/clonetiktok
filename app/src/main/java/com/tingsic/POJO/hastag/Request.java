
package com.tingsic.POJO.hastag;

import com.google.gson.annotations.SerializedName;

public class Request {

    @SerializedName("data")
    private Data mData;
    @SerializedName("request")
    private Request mRequest;
    @SerializedName("service")
    private String mService;

    public Data getData() {
        return mData;
    }

    public void setData(Data data) {
        mData = data;
    }

    public Request getRequest() {
        return mRequest;
    }

    public void setRequest(Request request) {
        mRequest = request;
    }

    public String getService() {
        return mService;
    }

    public void setService(String service) {
        mService = service;
    }

}
