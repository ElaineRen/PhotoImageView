package com.haokan.wallpaper;

import android.service.wallpaper.WallpaperService;

import com.haokan.wallpaper.callback.BaseWallpaperCallback;
import com.haokan.wallpaper.callback.LiveWallpaperServicecallback;
import com.haokan.wallpaper.others.GWallpaper;

public abstract class BaseWallpaperService extends WallpaperService implements BaseWallpaperCallback {
    public volatile GWallpaper mGWallpaper;
    public  final Object  mObject= new Object();
    public  boolean isBoolean=false;


    @Override
    public Object wallpaperCallback() {
        if (mGWallpaper==null){
            synchronized (this.mObject){
                if (mGWallpaper==null){
                    mGWallpaper = new GWallpaper(this);
                }
            }
        }
        return this.mGWallpaper.wallpaperCallback();
    }

    @Override
    public void onCreate() {
        if (!isBoolean){
            this.isBoolean=true;
            ((LiveWallpaperServicecallback)wallpaperCallback()).callback((DefiniteTimeWallpaperService)this);
        }
        super.onCreate();
    }
}
