package com.ufistudio.hotelmediabox.repository.remote

import com.ufistudio.hotelmediabox.repository.data.Broadcast
import com.ufistudio.hotelmediabox.repository.data.BroadcastRequest
import com.ufistudio.hotelmediabox.repository.data.Weather
import com.ufistudio.hotelmediabox.repository.data.WeatherInfo
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiClientService {
    @GET
    fun download(@Url url: String): Single<ResponseBody>

    @GET
    fun softwareUpdate(@Url url: String): Single<Broadcast>

    @POST
    fun checkStatus(
            @Url url: String,
            @Body request: BroadcastRequest
    ): Single<ResponseBody>

    @Multipart
    @POST
    fun postChannelList(
            @Url url: String,
            @Body request: BroadcastRequest,
            @Part file: MultipartBody.Part
    ): Single<ResponseBody>

    @GET
    fun getWeatherInfo(
            @Url url: String,
            @Query("city") city: String
    ): Single<WeatherInfo>
}