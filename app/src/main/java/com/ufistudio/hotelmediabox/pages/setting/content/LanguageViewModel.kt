package com.ufistudio.hotelmediabox.pages.setting.content

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.Config
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class LanguageViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {
    val initConfigServiceSuccess = MutableLiveData<Config>()
    val initConfigServiceProgress = MutableLiveData<Boolean>()
    val initConfigServiceError = MutableLiveData<Throwable>()

    init {
        val json = getConfigJsonObject()
        if (json != null) {
            compositeDisposable.add(Single.just(json)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { initConfigServiceProgress.value = true }
                    .doFinally { initConfigServiceProgress.value = false }
                    .subscribe({ initConfigServiceSuccess.value = it }
                            , { initConfigServiceError.value = it })
            )
        } else {
            initConfigServiceError.value = Throwable("jsonObject is null")
        }
    }

    private fun getConfigJsonObject(): Config? {
        val gson = Gson()
        return gson.fromJson(MiscUtils.getJsonFromStorage("config.json"), Config::class.java)
    }
}