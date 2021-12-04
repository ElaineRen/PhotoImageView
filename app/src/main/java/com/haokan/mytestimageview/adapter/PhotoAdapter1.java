package com.haokan.mytestimageview.adapter;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView;
import android.widget.ImageView.ScaleType;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.haokan.mytestimageview.R;
import com.haokan.mytestimageview.utils.Util;

import java.util.List;

public class PhotoAdapter1 extends RecyclerView.Adapter<PhotoAdapter1.MyViewHolder> {
    private ScaleType mScaleType;
    private Boolean mClipCircle;
    private Context mContext;
    private List<String> mData;


    public PhotoAdapter1(Context context, @Nullable List<String> data, ScaleType scaleType, Boolean clipCircle) {
        mContext = context;
        mScaleType = scaleType;
        mClipCircle = clipCircle;
        mData = data;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_img, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoAdapter1.MyViewHolder holder, int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        ImageView view = myViewHolder.mImageView;
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (mClipCircle == null || !mClipCircle) {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            layoutParams.width = Util.dp2px(view.getContext(), 100);
        }

        ScaleType scaleType = mScaleType == null ? ScaleType.FIT_CENTER : mScaleType;
        view.setScaleType(scaleType);
        RequestOptions options;
        if (mClipCircle != null) {
            if (mClipCircle) {
                if (scaleType == ScaleType.CENTER_CROP) {
                    options = new RequestOptions().transform(new CenterCrop(), new CircleCrop());
                } else if (scaleType == ScaleType.CENTER_INSIDE) {
                    options = new RequestOptions().transform(new CenterInside(), new CircleCrop());
                } else {
                    options = new RequestOptions().transform(new FitCenter(), new CircleCrop());
                }
            } else {
                if (scaleType == ScaleType.CENTER_CROP) {
                    options = new RequestOptions().transform(new CenterCrop(), new RoundedCorners(100));
                } else if (scaleType == ScaleType.CENTER_INSIDE) {
                    options = new RequestOptions().transform(new CenterInside(), new RoundedCorners(100));
                } else {
                    options = new RequestOptions().transform(new FitCenter(), new RoundedCorners(100));
                }
            }
        } else {
            options = new RequestOptions();
        }


        Glide.with(mContext)
                .load(mData.get(position))
                .apply(options)
                // .override(Target.SIZE_ORIGINAL)
                .into(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null) {
                    mOnClickListener.onItemClick(position, view);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.itemIv);
        }

    }

    private OnClickListener mOnClickListener;

    public void setOnClickListener(OnClickListener clickListener) {
        this.mOnClickListener = clickListener;
    }

    public interface OnClickListener {
        void onItemClick(int position, ImageView imageView);

    }
}
