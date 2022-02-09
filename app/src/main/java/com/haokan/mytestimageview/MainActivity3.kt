package com.haokan.mytestimageview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.haokan.mytestimageview.retrofittest.NetService
import com.haokan.mytestimageview.retrofittest.bean.NetResponse
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.internal.operators.single.SingleJust
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.function.Function

//retrofit 的测试使用
class MainActivity3 : AppCompatActivity() {
    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        var retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()

        var netApi = retrofit.create(NetService::class.java)// kotlin 写法


//        netApi.getRepos("rengwuxian")
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(object : SingleObserver<MutableList<NetResponse>>{
//                override fun onSubscribe(d: Disposable?) {//Disposable 取消订阅
//                    //是在订阅产生的时候 就会调用的 可以用来做初始化工作
//                    disposable=d
//                    text.text = "正在请求"
//                }
//
//                override fun onSuccess(response: MutableList<NetResponse>) {
//                    text.text = "resulr:${response[0].name}"
//                }
//
//                override fun onError(e: Throwable) {
//                    text.text = e.message ?: e.javaClass.name
//                }
//
//            })

        //反射




    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }
}