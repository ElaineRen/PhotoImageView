package com.haokan.wallpaper;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class DefiniteTimeWallpaperService extends BaseWallpaperService {

    @Override
    public Engine onCreateEngine() {
        Log.d("setWallpaper", "DefiniteTimeWallpaperService onCreateEngine,");
        return new WallpaperEngine();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("setWallpaper", "DefiniteTimeWallpaperService onStartCommand,");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("setWallpaper", "DefiniteTimeWallpaperService onUnbind,");
        return super.onUnbind(intent);
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        Log.d("setWallpaper", "DefiniteTimeWallpaperService unbindService,");
        super.unbindService(conn);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("setWallpaper", "DefiniteTimeWallpaperService onCreate,");
    }

    @Override
    public void onDestroy() {
        Log.d("setWallpaper", "DefiniteTimeWallpaperService onDestroy,");

        super.onDestroy();
    }
    public final  MyInterFace mMyInterFace=

    public class WallpaperEngine extends Engine {
        //AlarmManager mManager;
        public final  class CustomBroadReceiver extends BroadcastReceiver{

            private DefiniteTimeWallpaperService mWallpaperService;
            private WallpaperEngine mWallpaperEngine;
            public CustomBroadReceiver(DefiniteTimeWallpaperService service, WallpaperEngine wallpaperEngine){
                this.mWallpaperEngine=wallpaperEngine;
                this.mWallpaperService=service;

            }
            @Override
            public void onReceive(Context context, Intent intent) {
                if (("com.sspai.cuto.android.change_wallpaper").equals(intent.getAction())){

                }


            }
        }
        public WallpaperEngine(){
            //mManager= (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        }
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            Log.d("setWallpaper", "WallpaperEngine onCreate,");
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            Canvas canvas = holder.lockCanvas();
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setStrokeWidth(10);
            p.setColor(Color.BLUE);
            canvas.drawRect(0, 0, 500, 500, p);
            holder.unlockCanvasAndPost(canvas);
            Log.d("setWallpaper", "WallpaperEngine onSurfaceCreated,");

        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            Log.d("setWallpaper", "WallpaperEngine onVisibilityChanged," + visible);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            Log.d("setWallpaper", "WallpaperEngine onSurfaceDestroyed,");

        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.d("setWallpaper", "WallpaperEngine onDestroy,");

        }
    }
}
