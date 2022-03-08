package com.haokan.wallpaper.others;

import android.app.Application;
import android.app.Service;

import com.haokan.wallpaper.DefiniteTimeWallpaperService;
import com.haokan.wallpaper.callback.BaseWallpaperCallback;
import com.haokan.wallpaper.callback.InterfaceD;
import com.haokan.wallpaper.callback.LiveWallpaperServicecallback;

public class GWallpaper implements BaseWallpaperCallback<Object> {
    public Service mService;
    public Object mObject;

    public interface AInterface {
        InterfaceD D();
    }

    public GWallpaper(Service service) {
        this.mService = service;
    }

    @Override
    public Object wallpaperCallback() {
        if (this.mObject==null){
            Application application= this.mService.getApplication();
            //获取一个callback
            return new LiveWallpaperServicecallback() {
                @Override
                public void callback(DefiniteTimeWallpaperService wallpaperService) {

                }
            };
        }
        return this.mObject;
    }
}
