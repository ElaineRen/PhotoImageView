package com.haokan.mytestimageview.retrofittest

import com.haokan.mytestimageview.retrofittest.bean.NetResponse
import retrofit2.Call
import retrofit2.http.GET

interface NetService {

    @GET("cozing")
    fun serviceApi(): Call<NetResponse>

}