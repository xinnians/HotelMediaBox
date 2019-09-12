package com.ufistudio.hotelmediabox.repository

import android.app.Application
import com.google.android.exoplayer2.util.Log
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.constants.Cache
import com.ufistudio.hotelmediabox.repository.data.*
import com.ufistudio.hotelmediabox.repository.provider.preferences.PreferencesKey.CHANNEL_LIST
import com.ufistudio.hotelmediabox.repository.provider.preferences.SharedPreferencesProvider
import com.ufistudio.hotelmediabox.repository.remote.ApiClient
import com.ufistudio.hotelmediabox.repository.remote.RemoteAPI
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.utils.MiscUtils
import com.ufistudio.hotelmediabox.utils.XTNetWorkManager
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
                .flatMap {

                    var info: XTNetWorkManager.XTHost? = XTNetWorkManager.getInstance().getEthernetInfo(application)
                    Log.e(TAG,"[postCheckStatus] ip : ${info?.ip}, gateway : ${info?.gateway}, mask : ${info?.netmask}, inDHCP : ${Cache.IsDHCP}")

                    ApiClient.getInstance()!!.postCheckStatus(url,
//                            MiscUtils.getWifiMACAddress(application.applicationContext),
                            MiscUtils.getEthernetMacAddress(),
                            MiscUtils.getIpAddress(),
                            MiscUtils.getRoomNumber(),
                            "1",
                            it.config.tar_version,
                            Cache.JVersion ?: "",
                            Cache.AppVersion ?: "",
                            if(Cache.IsDHCP) "0" else "1",
                        info?.netmask ?: "255.255.255.0"
                    )
                }
                .subscribeOn(Schedulers.io())
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
        return ApiClient.getInstance()!!.getInitialData(url, MiscUtils.getEthernetMacAddress())
    }

    fun getStaticIp(url: String): Single<StaticIpData> {
        return ApiClient.getInstance()!!.getStaticIp(url, MiscUtils.getEthernetMacAddress())
    }

    fun getGuestMessage(url: String): Single<PMS> {
        return ApiClient.getInstance()!!.getGuestMessage(url, MiscUtils.getEthernetMacAddress())
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