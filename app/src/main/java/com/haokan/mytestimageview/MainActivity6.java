package com.haokan.mytestimageview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.haokan.mytestimageview.view.HKWebView;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity6 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);
        //HKWebView webView= findViewById(R.id.webview);
        //webView.setmActivity(this);
        //ViewGroup viewGroup= findViewById(R.id.root);
        //webView.setmBigViedioParent(viewGroup);
        //ProgressBar progressBar= findViewById(R.id.progress);
        //webView.setmProgressHorizontal(progressBar);
        //String url ="https://web.levect.com/page/find";
        ////"https://www.baidu.com/"
        //webView.loadUrl(url);
        //反射
        try {
            Class utilsClass=Utils.class;
            Object utils = utilsClass.newInstance();
            //Class utilsClass=Class.forName("com.haokan.mytestimageview.Utils");
            //Constructor utilsConstructor=utilsClass.getDeclaredConstructor()[0];
            //Object utils = utilsClass.newInstance();

            Method shoutMethod = utilsClass.getDeclaredMethod("shout");
            shoutMethod.invoke(utils);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}