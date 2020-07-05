
package com.tingsic.POJO.hastag;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class HashTagRequest {

    @Expose
    private Data data;
    @SerializedName("request")
    @Expose
    private HashTagRequest hashTagRequest;
    @Expose
    private String service;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public HashTagRequest getHashTagRequest() {
        return hashTagRequest;
    }

    public void setHashTagRequest(HashTagRequest hashTagRequest) {
        this.hashTagRequest = hashTagRequest;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public static class Data {

        @Expose
        private String hashtag;
        @Expose
        private String page;

        public String getHashtag() {
            return hashtag;
        }

        public void setHashtag(String hashtag) {
            this.hashtag = hashtag;
        }

        public String getPage() {
            return page;
        }

        public void setPage(String page) {
            this.page = page;
        }

    }
}
