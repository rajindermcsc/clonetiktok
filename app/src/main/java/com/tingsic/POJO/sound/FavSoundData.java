package com.tingsic.POJO.sound;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tingsic.POJO.service.Data;

public class FavSoundData extends Data {

    @SerializedName("fb_id")
    @Expose
    private String fbId;

    public String getFbId() {
        return fbId;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }

}
