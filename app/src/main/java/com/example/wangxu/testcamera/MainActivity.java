package com.example.wangxu.testcamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.chaoxing.camera.JCameraView;
import com.chaoxing.camera.listener.ErrorListener;
import com.chaoxing.camera.listener.JCameraListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private JCameraView jCameraView;
    private String schemeSpecificPart;
    private boolean resiveControl = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HHmmss");
        String takePhotofileName = df.format(new Date()) + ".jpg";
        File homeFolder =
                new File(Environment.getExternalStorageDirectory(), "chaoxing/chaoxingmobile/tempImages/");

        Uri uri= Uri.fromFile(new File(homeFolder,takePhotofileName));

        schemeSpecificPart = uri.getSchemeSpecificPart();

        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
        jCameraView = (JCameraView) findViewById(R.id.jcameraview);
        jCameraView.setDuration(3 *1000);

//设置视频保存路径
        jCameraView.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "JCamera");

//设置只能录像或只能拍照或两种都可以（默认两种都可以）
        jCameraView.setFeatures(JCameraView.BUTTON_STATE_BOTH);


//设置视频质量
        jCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE);
        jCameraView.setShowMode(JCameraView.SHOW_MODE_DEFAULT,5);

//JCameraView监听
        jCameraView.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                //打开Camera失败回调
                Log.i("CJT", "open camera error");
            }
            @Override
            public void AudioPermissionError(String str) {
                //没有录取权限回调
                Log.i("CJT", "AudioPermissionError");
            }

            @Override
            public void singerOptartionToast() {
                ToastUtils.showText(MainActivity.this,"不能拍照哦");
            }
        });

        jCameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                //获取图片bitmap
                Log.i("JCameraView", "bitmap = " + bitmap.getWidth());

            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                //获取视频路径
                Log.i("CJT", "url = " + url);
            }
            @Override
            public void quit() {
                //退出按钮
                MainActivity.this.finish();
            }

            @Override
            public void captureSuccess(List<Uri> bitmaps) {
                for (Uri bitmap : bitmaps) {
                    Toast.makeText(MainActivity.this, bitmap.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void editImage(Bitmap bitmap) {
                com.example.wangxu.testcamera.ToastUtils.showCenterText(MainActivity.this,bitmap.toString());
            }
        });
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resiveControl = false;
                jCameraView.setBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.img_0),true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (resiveControl) {
            jCameraView.onResume();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (resiveControl) {
            jCameraView.onPause();
        }
    }
}
