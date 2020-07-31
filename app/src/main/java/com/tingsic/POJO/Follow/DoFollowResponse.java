package com.tingsic.POJO.Follow;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DoFollowResponse {

    @SerializedName("message_id")
    @Expose
    private Integer message;
    @SerializedName("success")
    @Expose
    private Integer success;

    public Integer getMessage() {
        return message;
    }

    public void setMessage(Integer message) {
        this.message = message;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

}
