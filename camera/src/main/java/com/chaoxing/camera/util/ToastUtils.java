package com.chaoxing.camera.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by wangxu on 2017/9/25.
 */

public class ToastUtils {
    private static Toast mToast;
    public static void showShortText(Context context,String msg){
        if (mToast == null) {
            mToast = Toast.makeText(context,msg,Toast.LENGTH_SHORT);
        }else {
            mToast.setText(msg);
        }
        mToast.show();
    }    public static void showLongText(Context context,String msg){
        if (mToast == null) {
            mToast = Toast.makeText(context,msg,Toast.LENGTH_LONG);
        }else {
            mToast.setText(msg);
        }
        mToast.show();
    }
}
