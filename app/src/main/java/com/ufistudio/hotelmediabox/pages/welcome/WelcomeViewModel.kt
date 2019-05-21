package com.ufistudio.hotelmediabox.pages.welcome

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.Config
import com.ufistudio.hotelmediabox.repository.data.InitialData
import com.ufistudio.hotelmediabox.repository.data.Welcome
import com.ufistudio.hotelmediabox.repository.remote.ApiClient
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers

class WelcomeViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {

    val initWelcomeSuccess = MutableLiveData<Pair<Welcome, InitialData>>()
    val initWelcomeProgress = MutableLiveData<Boolean>()
    val initWelcomeError = MutableLiveData<Throwable>()

    val mGson: Gson = Gson()

    init {
        val gson = Gson()
        compositeDisposable.add(Single.fromCallable { gson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("welcome"), Welcome::class.java) }
                .zipWith(getInitialDataFromServer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { initWelcomeProgress.value = true }
                .doFinally { initWelcomeProgress.value = false }
                .subscribe({
                    initWelcomeSuccess.value = it
                }, { initWelcomeError.value = it })
        )
    }

    /**
     * 從Server 讀取時間
     */
    private fun getInitialDataFromServer(): Single<InitialData> {
        return Single.fromCallable { mGson.fromJson(MiscUtils.getJsonFromStorage("box_config.json"), Config::class.java) }
                .flatMap {
                    ApiClient.getInstance()!!.getInitialData("http:${it.config.defaultServerIp}/api/device/initial")
                }
    }
}