package com.ufistudio.hotelmediabox.repository.remote

import com.ufistudio.hotelmediabox.repository.data.Broadcast
import com.ufistudio.hotelmediabox.repository.data.InitialData
import com.ufistudio.hotelmediabox.repository.data.WeatherInfo
import com.ufistudio.hotelmediabox.repository.remote.RemoteAPI.Companion.getOkHttpClient
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
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
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .build()

        mService = retrofit.create(ApiClientService::class.java)
    }


    /*--------------------------------------------------------------------------------------------*/
    /* APIs */

    fun downloadFileWithUrl(url: String): Single<ResponseBody> = mService.download(url)

    fun postCheckStatus(url: String, mac: String, ip: String, room: String, status: String, tarVersion: String = "", jVersion: String = ""): Single<ResponseBody> {
        return mService.checkStatus(url, mac, ip, room, status, tarVersion, jVersion)
    }

    fun postChannel(url: String, mac: String, file: MultipartBody.Part): Single<ResponseBody> {
        return mService.postChannelList(url, mac, file)
    }

    fun getSoftwareUpdate(url: String): Single<Broadcast> = mService.softwareUpdate(url)

    fun getWeatherInfo(url: String, city: String): Single<WeatherInfo> = mService.getWeatherInfo(url, city)

    fun getInitialData(url: String): Single<InitialData> = mService.getInitialData(url)
}