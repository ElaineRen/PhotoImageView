package com.haokan.mytestimageview.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.haokan.mytestimageview.R;

import java.util.LinkedList;
import java.util.List;

public class MyPagerAdapter extends PagerAdapter {
    private List<String> mData;
    private Context mContext;
    private LinkedList<View> mCacheViews = null;
    private onPagerClickListener mOnClickListener;

    public MyPagerAdapter(Context context, List<String> data) {
        this.mContext = context;
        this.mData = data;
        mCacheViews = new LinkedList<>();

    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object == view;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        View view = (View) object;
        ImageView imageView = ((ViewHolder) (view.getTag())).mImageView;
        if (imageView != null) {
            container.removeView(view);
            mCacheViews.add(view);
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        String url = mData.get(position);
        View covertView = null;
        ViewHolder viewHolder = null;
        if (mCacheViews.size() == 0) {
            viewHolder = new ViewHolder();
            covertView = View.inflate(mContext, R.layout.item_image_pager, null);
            viewHolder.mImageView = covertView.findViewById(R.id.itemIv);
            covertView.setTag(viewHolder);
        } else {
            covertView = mCacheViews.removeFirst();
            viewHolder = (ViewHolder) covertView.getTag();
        }
        Glide.with(mContext).load(url).into(viewHolder.mImageView);
        ViewHolder finalViewHolder = viewHolder;
        viewHolder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener!=null){
                    mOnClickListener.onItemClickListener(finalViewHolder.mImageView,position);
                }
            }
        });
        container.addView(covertView);
        return covertView;
    }
    private View mCurrentView;

    @Override
    public void setPrimaryItem(@NonNull  ViewGroup container, int position, @NonNull  Object object) {
        mCurrentView= (View) object;
    }
    public View getCurrentView(){
        return  mCurrentView;
    }

    public class ViewHolder {
        public ImageView mImageView;
    }
    public void setOnClickListener(onPagerClickListener clickListener){
        this.mOnClickListener=clickListener;
    }
    public interface onPagerClickListener{
        void onItemClickListener(ImageView view,int innerPosition);
    }
}
