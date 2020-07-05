package com.tingsic.Listner;

import com.tingsic.POJO.Video.Video;

import java.util.List;

public interface OnVideoListener {
    void onVideoSelected(int position, List<Video> videos);
}
