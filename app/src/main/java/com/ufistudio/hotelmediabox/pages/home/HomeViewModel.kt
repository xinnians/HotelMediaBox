package com.ufistudio.hotelmediabox.pages.home

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.Home
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result.response
import java.io.File
import javax.security.auth.callback.Callback


class HomeViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {

    val initHomeSuccess = MutableLiveData<Home>()
    val initHomeProgress = MutableLiveData<Boolean>()
    val initHomeError = MutableLiveData<Throwable>()

    val fileDownloadSuccess = MutableLiveData<ResponseBody>()
    val fileDownloadProgress = MutableLiveData<Boolean>()
    val fileDownloadError = MutableLiveData<Throwable>()

    init {
        val gson = Gson()
        val jsonModel = gson.fromJson(MiscUtils.parseJsonFile(application, "home.json"), Home::class.java)
        compositeDisposable.add(Single.just(jsonModel)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { initHomeProgress.value = true }
                .doFinally { initHomeProgress.value = false }
                .subscribe({ initHomeSuccess.value = it }
                        , { initHomeError.value = it })
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