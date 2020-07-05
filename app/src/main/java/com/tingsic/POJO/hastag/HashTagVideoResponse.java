package com.tingsic.POJO.hastag;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tingsic.POJO.Video.Video;

import java.util.List;

public class HashTagVideoResponse {

    @SerializedName("success")
    @Expose
    private Integer success;
    @SerializedName("videos_url")
    @Expose
    private String videosUrl;
    @SerializedName("profile_url")
    @Expose
    private String profileUrl;
    @SerializedName("data")
    @Expose
    private List<Video> videos;

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public String getVideosUrl() {
        return videosUrl;
    }

    public void setVideosUrl(String videosUrl) {
        this.videosUrl = videosUrl;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}