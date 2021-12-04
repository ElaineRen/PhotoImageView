package com.haokan.mytestimageview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.haokan.mytestimageview.R;

import java.util.List;

import android.widget.ImageView.ScaleType;

public class PhotoAdapter2 extends RecyclerView.Adapter<PhotoAdapter2.MyViewHolder> {
    private ScaleType mScaleType;
    private Boolean mClipCircle;
    private Context mContext;
    private List<String> mData;

    public PhotoAdapter2(Context context, @Nullable List<String> data, ScaleType scaleType, Boolean clipCircle) {
        mContext = context;
        mScaleType = scaleType;
        mClipCircle = clipCircle;
        mData = data;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_image2, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoAdapter2.MyViewHolder holder, int position) {
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(mContext, mData);
        holder.mViewPager.setAdapter(myPagerAdapter);
        holder.mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        myPagerAdapter.setOnClickListener(new MyPagerAdapter.onPagerClickListener() {
            @Override
            public void onItemClickListener(ImageView view,int innerPosition) {
                if (mOnClickListener != null) {
                    mOnClickListener.onItemClick(position, innerPosition, view);
                }
            }
        });


    }

    public ImageView getImageView() {
        return null;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ViewPager mViewPager;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mViewPager = itemView.findViewById(R.id.viewpager);
        }

    }

    private OnClickListener mOnClickListener;

    public void setOnClickListener(OnClickListener clickListener) {
        this.mOnClickListener = clickListener;
    }

    public interface OnClickListener {
        void onItemClick(int position, int innerPosition, ImageView imageView);
    }
}
