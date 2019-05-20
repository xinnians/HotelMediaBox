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
            @Query("mac") mac: String,
            @Query("ip") ip: String,
            @Query("room") room: String,
            @Query("status") status: String,
            @Query("tar_version") tarVersion: String,
            @Query("j_version") jVersion: String
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
            @Query("city") city: String
    ): Single<WeatherInfo>

    @GET
    fun getTime(
            @Url url: String
    ): Single<TimeInfo>
}