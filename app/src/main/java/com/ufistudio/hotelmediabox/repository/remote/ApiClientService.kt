package com.ufistudio.hotelmediabox.repository.remote

import com.ufistudio.hotelmediabox.repository.data.*
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiClientService {
    @Streaming
    @GET
    fun download(@Url url: String): Single<ResponseBody>

    @GET
    fun softwareUpdate(@Url url: String): Single<Broadcast>

    @POST
    fun checkStatus(
            @Url url: String,
            @Query("mac", encoded = true) mac: String,
            @Query("ip", encoded = true) ip: String,
            @Query("room", encoded = true) room: String,
            @Query("status", encoded = true) status: String,
            @Query("tar_version", encoded = true) tarVersion: String,
            @Query("j_version", encoded = true) jVersion: String,
            @Query("apk_version",encoded = true) apkVersion: String,
            @Query("static",encoded = true) static: String,
            @Query("mask",encoded = true) mask: String
    ): Single<ResponseBody>

    @Multipart
    @POST
    fun postChannelList(
            @Url url: String,
//            @Body request: BroadcastRequest,
            @Part file: MultipartBody.Part
    ): Single<ResponseBody>

    @GET
    fun getWeatherInfo(
            @Url url: String,
            @Query("city", encoded = true) city: String
    ): Single<WeatherInfo>

    @GET
    fun getInitialData(
            @Url url: String,
            @Query("mac", encoded = true) mac: String
    ): Single<InitialData>

    @GET
    fun getStaticIp(
            @Url url: String,
            @Query("mac", encoded = true) mac: String
    ): Single<StaticIpData>
}