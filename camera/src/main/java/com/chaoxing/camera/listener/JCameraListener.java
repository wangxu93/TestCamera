package com.chaoxing.camera.listener;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.List;

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/4/26
 * 描    述：
 * =====================================
 */
public interface JCameraListener {

    void captureSuccess(Bitmap bitmap);

    void recordSuccess(String url, Bitmap firstFrame);

    void quit();

    void captureSuccess(List<Uri> bitmaps);

    void editImage(Bitmap bitmap);

    void onQuickCapture(Bitmap bitmap);

}
