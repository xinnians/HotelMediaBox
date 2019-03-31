package com.ufistudio.hotelmediabox.repository.remote

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiClientService {
    @GET
    fun download(@Url url: String): Single<ResponseBody>
}