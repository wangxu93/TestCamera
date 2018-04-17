package com.example.wangxu.testcamera;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by wangxu on 2016/8/11.
 */
public class ToastUtils {
    private static Toast toast;

    public static void showText(Context context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
        }
        toast.show();
    }

    public static void showCenterText(Context context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
        }
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showBottomText(Activity context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
        }

        Display display = context.getWindowManager().getDefaultDisplay();
        // 获取屏幕高度
        int height = display.getHeight();
        // 这里给了一个1/4屏幕高度的y轴偏移量
        toast.setGravity(Gravity.TOP, 0, (height * 2) / 3);
        toast.show();

    }
}
