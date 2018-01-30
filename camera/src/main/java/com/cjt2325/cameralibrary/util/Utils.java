package com.cjt2325.cameralibrary.util;

import android.graphics.Bitmap;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by wangxu on 2017/9/25.
 */

public class Utils {

    public static boolean saveBitmap(Bitmap bitmap, String filePath, Bitmap.CompressFormat format, int quality) {
        if (bitmap == null || TextUtils.isEmpty(filePath)) {
            return false;
        }
        File bitmapFile = new File(filePath);
        if (quality <= 0 || quality > 100)
            quality = 100;
        File parentFile = bitmapFile.getParentFile();
        if (parentFile != null && !parentFile.getAbsoluteFile().exists()) {
            parentFile.mkdirs();
        }
        if (bitmapFile.exists()) {
            bitmapFile.delete();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(bitmapFile);
            bitmap.compress(format, quality, fos);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
