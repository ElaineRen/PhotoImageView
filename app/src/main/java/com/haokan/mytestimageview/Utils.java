package com.haokan.mytestimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

public class Utils {
     void shout(){
        System.out.print("I am shouting" );
    }
    /**
     * @param context 设备上下文
     * @param resId 资源ID
     * @param width
     * @param height
     * @return
     */
    public static Bitmap decodeBitmap(Context context, int resId, int width, int height) {
        InputStream inputStream = context.getResources().openRawResource(resId);
        BitmapFactory.Options op = new BitmapFactory.Options();
        // inJustDecodeBounds如果设置为true,仅仅返回图片实际的宽和高,宽和高是赋值给opts.outWidth,opts.outHeight;
        op.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeStream(inputStream, null, op); //获取尺寸信息
        //获取比例大小
        int wRatio = (int) Math.ceil(op.outWidth / width);
        int hRatio = (int) Math.ceil(op.outHeight / height);
        //如果超出指定大小，则缩小相应的比例
        if (wRatio > 1 && hRatio > 1) {
            if (wRatio > hRatio) {
                op.inSampleSize = wRatio;
            } else {
                op.inSampleSize = hRatio;
            }
        }
        inputStream = context.getResources().openRawResource(resId);
        op.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(inputStream, null, op);
    }
}
