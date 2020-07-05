package com.tingsic.API;

public interface UploadListener {

    void onProgressUpdate(int percentage);
    void onError();
    void onFinish();
    void onUploadStart();

}
