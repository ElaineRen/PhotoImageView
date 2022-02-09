package com.haokan.mytestimageview.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class HKWebView extends WebView {

    private static final String TAG = "HKWebView";

    private Context mContext;
    private Activity mActivity;

    /*Must*/
    public void setmActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

    private ProgressBar mProgressHorizontal;

    /*Must*/
    public void setmProgressHorizontal(ProgressBar mProgressHorizontal) {
        this.mProgressHorizontal = mProgressHorizontal;
    }

    private ViewGroup mBigViedioParent;
    private View mBigVidioView = null;

    /*Must*/
    public void setmBigViedioParent(ViewGroup mBigViedioParent) {
        this.mBigViedioParent = mBigViedioParent;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public HKWebView(@NonNull Context context) {
        super(context);
        this.mContext = context;
        initWebClient();
    }

    public HKWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initWebClient();
    }

    public HKWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initWebClient();
    }

    public HKWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        initWebClient();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebClient() {
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        String userAgent = ";from=magazine"
                + ";versionCode=" + "1.0"
                + ";countryCode=" + "zh"
                + ";languageCode=" + "zh"
                + ";cid=21"//realme
                + ";pid=369"//realme锁屏杂志客户端
                + ";eid=138005"//Android默认打开
         ;
        settings.setUserAgentString(settings.getUserAgentString() + userAgent);
        addJavascriptInterface(new MagazineJs(), "magazine_sbridge");
        // 不调用第三方浏览器即可进行页面反应
        getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, "shouldOverrideUrlLoading url:" + url);
                if (!android.text.TextUtils.isEmpty(url)) {
                    view.loadUrl(url);
                    return true;
                }
                return true;
                //return super.shouldOverrideUrlLoading(view, url);
            }
        });

        setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (mProgressHorizontal == null)
                    return;
                if (newProgress > 0 && newProgress < 90) {
                    mProgressHorizontal.setVisibility(View.VISIBLE);
                    mProgressHorizontal.setProgress(newProgress);
                } else {
                    mProgressHorizontal.setVisibility(View.GONE);
                }
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            //*******全屏播放视频设置相关begin*********
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (mBigVidioView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                mBigVidioView = view;
                mBigViedioParent.setVisibility(View.VISIBLE);
                mBigViedioParent.addView(view);
                /*setVisibility(View.GONE);*/
                if (mActivity != null)
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            @Override
            public void onHideCustomView() {
                setVisibility(View.VISIBLE);
                if (mBigVidioView != null) {
                    mBigViedioParent.removeView(mBigVidioView);
                    mBigVidioView = null;
                }
                mBigViedioParent.setVisibility(View.GONE);
                if (mActivity != null)
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });




    }

    public class MagazineJs{
        @JavascriptInterface
        public void login() {
        }
    }

}
