package com.haokan.mytestimageview

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.haokan.wallpaper.DefiniteTimeWallpaperService
import com.haokan.wallpaper.dialog.WallpaperStartServiceDialog
import kotlinx.android.synthetic.main.wallpaper_set_activity_layout.*
import java.io.IOException


class WallpaperSetActivity : AppCompatActivity() {
    var homeWallpaperId = 101
    var lockWallpaperId = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wallpaper_set_activity_layout)
        tv_btn_set_wallpaper.setOnClickListener {
            // onclick 操作
            setWallpaperFun()

        }
        tv_btn_clear_wallpaper.setOnClickListener {
            //清除桌面壁纸
            clearWallpaer(WallpaperManager.FLAG_SYSTEM)
        }
        tv_btn_set_lock_wallpaper.setOnClickListener {
//            setLockWallpaper()
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                setLockWallpapperWithFlag()
            } else {
                //待定
            }
        }
        tv_btn_clear_lock_wallpaper.setOnClickListener {
            // 清除 锁屏壁纸
            clearWallpaer(WallpaperManager.FLAG_LOCK)
        }

        tv_btn_get_flag.setOnClickListener {
            getWallpaperId()
        }

        tv_btn_dynic_wallpaper.setOnClickListener {
            // 1，允许开启定时服务
            // 2，不允许开启定时服务
            if (isNeedStartWallpaperService()) {
                //当前动态壁纸的服务不是我们的服务 或者无动态壁纸服务，需要开启壁纸服务
                showWallpaperServiceDialog()
            } else {
                //如果当前动态壁纸的服务是我们的服务
                startLiveWallpaperPreView(packageName,
                    DefiniteTimeWallpaperService::class.java.name)
            }
        }
    }

    fun isNeedStartWallpaperService(): Boolean {
        var mWallpaperManager: WallpaperManager = WallpaperManager.getInstance(this)
        var wallpaperInfo = mWallpaperManager.wallpaperInfo
        Log.d("setWallpaper", ("wallpaperInfo:  " + (wallpaperInfo == null)).toString())
        Log.d("setWallpaper", ("packageName isNeedStartWallpaperService:  " + ( wallpaperInfo?.packageName)))
        //当前动态壁纸的服务不是我们的服务 或者无动态壁纸服务，需要开启壁纸服务
        return wallpaperInfo == null || (!packageName.equals(wallpaperInfo?.packageName))
    }

    private fun showWallpaperServiceDialog() {
        WallpaperStartServiceDialog.Builder(this)
            .setMessage("")
            .setPositiveButton("启用", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                startLiveWallpaperPreView(packageName,
                    DefiniteTimeWallpaperService::class.java.name)
            })
            .setNegativeButton("取消", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            }).onCreate().show()
    }


    /**
     * 若不点击此方法
     */
    fun startLiveWallpaperPreView(packageName: String?, classFullName: String?) {
        // 若不走此方法
        val componentName = ComponentName(packageName!!, classFullName!!)
        val intent: Intent

        if (Build.VERSION.SDK_INT < 16) {
            intent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
        } else {
            intent = Intent("android.service.wallpaper.CHANGE_LIVE_WALLPAPER")
            intent.putExtra("android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT",
                componentName)
        }
        startActivity(intent)
    }

    private fun getWallpaperId() {
        var mWallpaperManager: WallpaperManager = WallpaperManager.getInstance(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            var str =
                "lock:" + mWallpaperManager.getWallpaperId(WallpaperManager.FLAG_LOCK) + ",home:" + mWallpaperManager.getWallpaperId(
                    WallpaperManager.FLAG_SYSTEM)
            tv_get_flag.setText(str)
        }

    }

    @SuppressLint("MissingPermission")
    private fun clearWallpaer(which: Int) {
        var mWallpaperManager: WallpaperManager = WallpaperManager.getInstance(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mWallpaperManager.clear(which)
        } else {
            mWallpaperManager.clear()
        }
    }

    /**
     * 设置桌面壁纸
     */
    @SuppressLint("MissingPermission")
    fun setWallpaperFun() {
        var filePath = ""
        var mWallpaperManager: WallpaperManager = WallpaperManager.getInstance(this)
        try {
            var bitmap = Utils.decodeBitmap(this, R.drawable.test, 1080, 1920);
            mWallpaperManager.setBitmap(bitmap)
            mWallpaperManager.wallpaperInfo
            Log.d("setWallpaper", "set wallpaper success")
            Toast.makeText(this, "壁纸设置成功", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //
    fun setLockWallpaper() {
        try {
            var mWallpaperManager = WallpaperManager.getInstance(this)
            var class1 = mWallpaperManager.javaClass//获取类名
            var method = class1.getMethod("setBitmapToLockWallpaper", Bitmap::class.java);
            var bitmap = Utils.decodeBitmap(this, R.drawable.test, 1080, 1920);
            method.invoke(mWallpaperManager, bitmap)
            Log.d("setWallpaper", "setLockWallpaper  success")
            Toast.makeText(this, "锁屏壁纸设置成功", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.N)
    fun setLockWallpapperWithFlag() {
        try {
            var mWallpaperManager = WallpaperManager.getInstance(this)

            //系统锁屏壁纸变化后，先获取系统默认壁纸，然后设置默认锁屏壁纸
//            Drawable drawable = wallpaperManager.getDrawable();/根据项目需要自行选择那种默认壁纸
            var bitmap = Utils.decodeBitmap(this, R.drawable.test, 1080, 1920)
            mWallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
            Log.d("setWallpaper", "setLockWallpaper  success")
            Toast.makeText(this, "锁屏壁纸设置成功", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace();
        }
    }


}