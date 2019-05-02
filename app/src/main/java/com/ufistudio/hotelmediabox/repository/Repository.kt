package com.ufistudio.hotelmediabox.repository

import android.app.Application
import com.ufistudio.hotelmediabox.repository.data.BaseChannel
import com.ufistudio.hotelmediabox.repository.data.Broadcast
import com.ufistudio.hotelmediabox.repository.data.BroadcastRequest
import com.ufistudio.hotelmediabox.repository.data.WeatherInfo
import com.ufistudio.hotelmediabox.repository.provider.preferences.PreferencesKey.CHANNEL_LIST
import com.ufistudio.hotelmediabox.repository.provider.preferences.SharedPreferencesProvider
import com.ufistudio.hotelmediabox.repository.remote.ApiClient
import com.ufistudio.hotelmediabox.repository.remote.RemoteAPI
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File

class Repository(
        private var application: Application,
        private val sharedPreferencesProvider: SharedPreferencesProvider
) {

    val TAG = Repository::class.simpleName

    init {
        RemoteAPI.init(application)
    }

    fun downloadFileWithUrl(url: String): Single<ResponseBody> {
        return ApiClient.getInstance()!!.downloadFileWithUrl(url)
    }

    fun postCheckStatus(url: String): Single<ResponseBody> {
        val body: BroadcastRequest = BroadcastRequest(
                MiscUtils.getWifiMACAddress(application.applicationContext)!!,
                MiscUtils.getIpAddress(application.applicationContext),
                "ok",
                MiscUtils.getRoomNumber()
        )
        return ApiClient.getInstance()!!.postCheckStatus(url, body)
    }

    fun postChannel(url: String): Single<ResponseBody> {
        val body: BroadcastRequest = BroadcastRequest(
                MiscUtils.getWifiMACAddress(application.applicationContext)!!,
                MiscUtils.getIpAddress(application.applicationContext),
                "ok"
        )

        val channels: File? = FileUtils.getFileFromStorage("box_channels.json")
        var multipartBody: MultipartBody.Part? = null
        if (channels != null) {
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), channels)
            multipartBody = MultipartBody.Part.createFormData("channels.json", "box_channels.json", requestFile)
        }
        return ApiClient.getInstance()!!.postChannel(url, body, multipartBody!!)
    }

    fun getSoftwareUpdate(url: String): Single<Broadcast> {
        return ApiClient.getInstance()!!.getSoftwareUpdate(url)
    }

    fun getWeatherInfo(url: String, cityCode: String): Single<WeatherInfo> {
        return ApiClient.getInstance()!!.getWeatherInfo(url, cityCode)
    }

    // local

    fun getChannelList(): Single<ArrayList<BaseChannel>>? {
        val jsonString = sharedPreferencesProvider.sharedPreferences().getString(CHANNEL_LIST, "")
        return Single.just(MiscUtils.parseJSONList(jsonString))
    }

    fun saveChannelList(list: ArrayList<BaseChannel>?) {
        val jsonString = MiscUtils.toJSONString(list)
        sharedPreferencesProvider.sharedPreferences().edit().putString(CHANNEL_LIST, jsonString).apply()
    }


}