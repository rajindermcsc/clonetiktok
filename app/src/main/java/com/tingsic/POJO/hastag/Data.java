
package com.tingsic.POJO.hastag;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("hashtag")
    private String mHashtag;
    @SerializedName("page")
    private String mPage;

    public String getHashtag() {
        return mHashtag;
    }

    public void setHashtag(String hashtag) {
        mHashtag = hashtag;
    }

    public String getPage() {
        return mPage;
    }

    public void setPage(String page) {
        mPage = page;
    }

}
