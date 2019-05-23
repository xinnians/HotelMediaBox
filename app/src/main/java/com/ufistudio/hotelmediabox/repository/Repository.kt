package com.ufistudio.hotelmediabox.repository

import android.app.Application
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.data.*
import com.ufistudio.hotelmediabox.repository.provider.preferences.PreferencesKey.CHANNEL_LIST
import com.ufistudio.hotelmediabox.repository.provider.preferences.SharedPreferencesProvider
import com.ufistudio.hotelmediabox.repository.remote.ApiClient
import com.ufistudio.hotelmediabox.repository.remote.RemoteAPI
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File
import java.util.concurrent.Callable

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
        val gson = Gson()
        return Single.fromCallable { gson.fromJson(MiscUtils.getJsonFromStorage("box_config.json"), Config::class.java) }
                .subscribeOn(Schedulers.io())
                .flatMap {
                    ApiClient.getInstance()!!.postCheckStatus(url,
                            MiscUtils.getWifiMACAddress(application.applicationContext),
                            MiscUtils.getIpAddress(),
                            MiscUtils.getRoomNumber(),
                            "1",
                            it.config.tar_version,
                            it.config.j_version
                    )
                }
    }

    fun postChannel(url: String): Single<ResponseBody> {

        val channels: File? = FileUtils.getFileFromStorage("box_scanned_channels.json")
        var multipartBody: MultipartBody.Part? = null
        if (channels != null) {
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), channels)
            multipartBody = MultipartBody.Part.createFormData("channels", "box_scanned_channels.json", requestFile)
        } else {
            return Single.error(Throwable("box_scanned_channels.json could not found"))
        }

        return ApiClient.getInstance()!!.postChannel(url, multipartBody)
    }

    fun getSoftwareUpdate(url: String): Single<Broadcast> {
        return ApiClient.getInstance()!!.getSoftwareUpdate(url)
    }

    fun getWeatherInfo(url: String, cityCode: String): Single<WeatherInfo> {
        return ApiClient.getInstance()!!.getWeatherInfo(url, cityCode)
    }

    fun getInitialData(url: String): Single<InitialData> {
        return ApiClient.getInstance()!!.getInitialData(url, MiscUtils.getWifiMACAddress(application.baseContext))
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