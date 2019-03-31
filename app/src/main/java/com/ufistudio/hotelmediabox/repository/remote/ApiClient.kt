package com.ufistudio.hotelmediabox.repository.remote

import com.ufistudio.hotelmediabox.repository.remote.RemoteAPI.Companion.getOkHttpClient
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {
    companion object {
        private val TAG = ApiClient::class.simpleName
        private var sInstance: ApiClient? = null
        private lateinit var mService: ApiClientService

        //一頁的listView顯示幾個，之後應由後台做設定
        private const val LISTVIEW_PAGECOUNT = 10

        fun getInstance(): ApiClient? {
            if (sInstance == null) {
                synchronized(ApiClient::class) {
                    if (sInstance == null) {
                        sInstance = ApiClient()
                    }
                }
            }
            return sInstance
        }
    }

    init {
        val url = "https://yahoo.com.tw"
        val client = getOkHttpClient()

        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()

        mService = retrofit.create(ApiClientService::class.java)
    }


    /*--------------------------------------------------------------------------------------------*/
    /* APIs */

    fun downloadFileWithUrl(url: String): Single<ResponseBody> = mService.download(url)

}