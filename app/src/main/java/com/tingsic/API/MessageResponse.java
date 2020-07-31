package com.tingsic.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tingsic.POJO.SignUp.Response.Data;

public class MessageResponse {
    @SerializedName("success")
    @Expose
    private String success;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @SerializedName("message")
    @Expose

    private String message;

    @SerializedName("data")
    @Expose
    private String data;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }





}
