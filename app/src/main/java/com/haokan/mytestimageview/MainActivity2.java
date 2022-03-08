package com.haokan.mytestimageview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.haokan.mytestimageview.adapter.MyPagerAdapter;
import com.haokan.mytestimageview.adapter.PhotoAdapter1;
import com.haokan.mytestimageview.adapter.PhotoAdapter2;
import com.haokan.mytestimageview.custom.listener.OnPagerItemPositionListener;
import com.haokan.mytestimageview.custom.preview.IndicatorType;
import com.haokan.mytestimageview.custom.preview.PhotoPreview;
import com.haokan.mytestimageview.custom.preview.ShapeTransformType;
import com.haokan.wallpaper.DefiniteTimeWallpaperService;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        RecyclerView recyclerView = findViewById(R.id.rv);
        recyclerView.setVisibility(View.GONE);
        //LinearLayoutManager layoutManager = new GridLayoutManager(this, 3);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        PhotoAdapter1 adapter = new PhotoAdapter1(this, Arrays.asList(MainActivity.picDataMore), ImageView.ScaleType.CENTER_CROP, true);
        recyclerView.setAdapter(adapter);
        adapter.setOnClickListener(new PhotoAdapter1.OnClickListener() {
            @Override
            public void onItemClick(int position, ImageView imageView) {
                PhotoPreview.with(MainActivity2.this)
                        .indicatorType(IndicatorType.TEXT)
                        .selectIndicatorColor(0xffEE3E3E)
                        .normalIndicatorColor(0xff3954A0)
                        .delayShowProgressTime(200)
                        .shapeTransformType(ShapeTransformType.CIRCLE)
                        .imageLoader((position1, url, imageView1) ->
                                Glide.with(MainActivity2.this)
                                        .load(((String) url))
                                        // .override(Target.SIZE_ORIGINAL)
                                        .into(imageView1))
                        .sources(Arrays.asList(MainActivity.picDataMore))
                        .defaultShowPosition(position)
                        .animDuration(350L)
                        .build()
                        .show(position1 -> layoutManager.findViewByPosition(position1).findViewById(R.id.itemIv));
            }
        });


        RecyclerView recyclerView2 = findViewById(R.id.rv2);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(RecyclerView.VERTICAL);
        recyclerView2.setLayoutManager(layoutManager2);
        PhotoAdapter2 adapter2 = new PhotoAdapter2(this, Arrays.asList(MainActivity.picDataMore), ImageView.ScaleType.CENTER_CROP, null);
        recyclerView2.setAdapter(adapter2);
        adapter2.setOnClickListener(new PhotoAdapter2.OnClickListener() {
            @Override
            public void onItemClick(int position, int innerPosition, ImageView imageView) {
                PhotoPreview.with(MainActivity2.this)
                        .indicatorType(IndicatorType.DOT)
                        .selectIndicatorColor(0xffEE3E3E)
                        .normalIndicatorColor(0xff3954A0)
                        .delayShowProgressTime(200)
                        .imageLoader((position1, url, imageView1) ->
                                Glide.with(MainActivity2.this)
                                        .load(((String) url))
                                        .into(imageView1))
                        .sources(Arrays.asList(MainActivity.picDataMore))
                        .flowPosition(position)
                        .defaultShowPosition(innerPosition)
                        .animDuration(350L)
                        .onPagerItemClickListener(new OnPagerItemPositionListener() {
                            @Override
                            public void onItemPositionWithInnerPosition(int position, int innerPosition) {
                                //
                                View viewByPosition1 = layoutManager2.findViewByPosition(position);
                                if (viewByPosition1 != null) {
                                    ViewPager viewPager = viewByPosition1.findViewById(R.id.viewpager);
                                    if (viewPager != null) {
                                        viewPager.setCurrentItem(innerPosition);
                                    }
                                }
                            }
                        })
                        .build()
                        .show(innerPosition1 -> {
                            View viewByPosition = layoutManager2.findViewByPosition(position);
                            if (viewByPosition == null) {
                                return null;
                            }
                            View viewByPosition1 = layoutManager2.findViewByPosition(position);
                            ViewPager viewPager = viewByPosition1.findViewById(R.id.viewpager);
                            MyPagerAdapter adapter1 = (MyPagerAdapter) viewPager.getAdapter();
                            View currentView = adapter1.getCurrentView();
                            View viewById = currentView.findViewById(R.id.itemIv);
                            if (viewById == null) {
                                return null;
                            }
                            return viewById;
                        });
            }
        });

        //adapter2.setOnItemClickListener((adapter1, view, position) -> {
        //    PhotoPreview.with(MainActivity2.this)
        //            .indicatorType(IndicatorType.DOT)
        //            .selectIndicatorColor(0xffEE3E3E)
        //            .normalIndicatorColor(0xff3954A0)
        //            .delayShowProgressTime(200)
        //            // .shapeTransformType(ShapeTransformType.ROUND_RECT)
        //            // .shapeCornerRadius(100)
        //            .imageLoader((position1, url, imageView1) ->
        //                    Glide.with(MainActivity2.this)
        //                            .load(((String) url))
        //                            // .override(Target.SIZE_ORIGINAL)
        //                            .into(imageView1))
        //            .sources(Arrays.asList(MainActivity.picDataMore))
        //            .defaultShowPosition(position)
        //            .animDuration(350L)
        //            .build()
        //            .show(position1 -> {
        //                View viewByPosition = layoutManager2.findViewByPosition(position1);
        //                if (viewByPosition == null) {
        //                    return null;
        //                }
        //                return viewByPosition.findViewById(R.id.itemIv);
        //            });
        //});
        startLiveWallpaperPreView(getPackageName(), DefiniteTimeWallpaperService.class.getName());
    }

    public void startLiveWallpaperPreView(String packageName, String classFullName) {
        ComponentName componentName = new ComponentName(packageName, classFullName);
        Intent intent;
        if (android.os.Build.VERSION.SDK_INT < 16) {
            intent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
        } else {
            intent = new Intent("android.service.wallpaper.CHANGE_LIVE_WALLPAPER");
            intent.putExtra("android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT", componentName);
        }
        startActivity(intent);
    }
}