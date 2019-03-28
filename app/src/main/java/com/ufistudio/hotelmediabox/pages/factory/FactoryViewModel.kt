package com.ufistudio.hotelmediabox.pages.factory

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.Config
import com.ufistudio.hotelmediabox.repository.data.Welcome
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

class FactoryViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {

    val initConfigSuccess = MutableLiveData<Config>()
    val initConfigProgress = MutableLiveData<Boolean>()
    val initConfigError = MutableLiveData<Throwable>()

    val fileDownloadSuccess = MutableLiveData<ResponseBody>()
    val fileDownloadProgress = MutableLiveData<Boolean>()
    val fileDownloadError = MutableLiveData<Throwable>()

    init {
        val gson = Gson()

        compositeDisposable.add(Single.just(gson.fromJson(MiscUtils.getJsonFromStorage("config.json"), Config::class.java))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { initConfigProgress.value = true }
                .doFinally { initConfigProgress.value = false }
                .subscribe({ initConfigSuccess.value = it }
                        , { initConfigError.value = it })
        )
    }

    fun downloadFileWithUrl(url: String) {
        fileDownloadProgress.value = true
        repository.downloadFileWithUrl(url)?.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                fileDownloadProgress.value = false
                fileDownloadError.value = t
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                fileDownloadProgress.value = false
                fileDownloadSuccess.value = response.body()
            }
        })
    }
}