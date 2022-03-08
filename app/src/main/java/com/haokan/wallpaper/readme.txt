//
如何保证 服务始终运行

work manager

1 使用workManager 定时任务
2，wallpaper services
借助系统的动态壁纸接口，实现了不借助其他 app、不需要常驻后台的自动换壁纸功能。
难点：网络请求接口时机
设置壁纸方法， engine 如何设置网络图片为壁纸


3，利用各种系统广播
   息屏，灭屏
   time Changed
   date changed
   timezoneChanged
   <action android:name="android.intent.action.SCREEN_OFF" />
                   <action android:name="android.intent.action.SCREEN_ON" />
                   <action android:name="android.intent.action.TIME_CHANGED" />
                   <action android:name="android.intent.action.DATE_CHANGED" />
                   <action android:name="android.intent.action.TIMEZONE_CHANGED" />

                   RECEIVE_BOOT_COMPLETED

  4，alarm manager