package com.tingsic.VideoCompress;

public interface CompressListener {
    void onExecSuccess(String message);
    void onExecFail(String reason);
    void onExecProgress(String message);
    void onFinish();
}
