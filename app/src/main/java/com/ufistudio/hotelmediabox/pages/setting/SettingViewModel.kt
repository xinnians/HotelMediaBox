package com.ufistudio.hotelmediabox.pages.setting

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.Config
import com.ufistudio.hotelmediabox.repository.data.Setting
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class SettingViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {

    val initSettingServiceSuccess = MutableLiveData<Setting>()
    val initSettingServiceProgress = MutableLiveData<Boolean>()
    val initSettingServiceError = MutableLiveData<Throwable>()

    init {
        val json = getJsonObject()
        if (json != null) {
            compositeDisposable.add(Single.just(json)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { initSettingServiceProgress.value = true }
                    .doFinally { initSettingServiceProgress.value = false }
                    .subscribe({ initSettingServiceSuccess.value = it }
                            , { initSettingServiceError.value = it })
            )
        } else {
            initSettingServiceError.value = Throwable("")
        }
    }

    private fun getJsonObject(): Setting? {
        val gson = Gson()
        return gson.fromJson(MiscUtils.getJsonFromStorage("setting_en.json"), Setting::class.java)
    }
}