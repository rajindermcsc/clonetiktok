package com.tingsic.API;

public interface ImageUploadListener {

    void onImageProgressUpdate(int percentage);
    void onIUploadError();
    void onIUploadFinish();
    void onIUploadStart();

}
