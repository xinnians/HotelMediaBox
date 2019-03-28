package com.ufistudio.hotelmediabox.repository

import android.app.Application
import com.ufistudio.hotelmediabox.repository.data.BaseChannel
import com.ufistudio.hotelmediabox.repository.provider.preferences.PreferencesKey.CHANNEL_LIST
import com.ufistudio.hotelmediabox.repository.provider.preferences.SharedPreferencesProvider
import com.ufistudio.hotelmediabox.repository.remote.ApiClient
import com.ufistudio.hotelmediabox.repository.remote.RemoteAPI
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Call

class Repository(
    private var application: Application,
    private val sharedPreferencesProvider: SharedPreferencesProvider
) {

    val TAG = Repository::class.simpleName

    init {
        RemoteAPI.init(application)
    }

    fun downloadFileWithUrl(url: String): Call<ResponseBody>? {
        return ApiClient.getInstance()?.downloadFileWithUrl(url)
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