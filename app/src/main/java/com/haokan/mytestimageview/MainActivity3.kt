package com.haokan.mytestimageview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.haokan.mytestimageview.retrofittest.NetService
import com.haokan.mytestimageview.retrofittest.bean.NetResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

//retrofit 的测试使用
class MainActivity3 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        var retrofit = Retrofit.Builder()
            .baseUrl("https://github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var netApi = retrofit.create(NetService::class.java)// kotlin 写法

        val call = netApi.serviceApi()
//        try {
//            // 1, 同步请求
//            var response = call.execute()
//            Log.d("retrofit_test", "execute response:${response.body().toString()}")
//        } catch (e: IOException) {
//            Log.d("retrofit_test", "failed:${e.message}")
//        }

        // 2，异步请求
        call.enqueue(object : Callback<NetResponse> {
            override fun onResponse(call: Call<NetResponse>, response: Response<NetResponse>) {
                Log.d("retrofit_test", "enqueue response:${response.body().toString()}")
            }

            override fun onFailure(call: Call<NetResponse>, t: Throwable) {
                Log.d("retrofit_test", "failed:${t.message}")
            }

        })
    }
}