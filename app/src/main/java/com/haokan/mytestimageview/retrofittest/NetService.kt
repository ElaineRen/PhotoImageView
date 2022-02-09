package com.haokan.mytestimageview.retrofittest

import com.haokan.mytestimageview.retrofittest.bean.NetResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface NetService {

    @GET("cozing")
     fun serviceApi(): Call<NetResponse>

    @GET("users/{username}/repos")
     fun getRepos(@Path("username") username: String): Single<MutableList<NetResponse>>

}