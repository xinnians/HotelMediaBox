package com.ufistudio.hotelmediabox.pages.setting

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.Config
import com.ufistudio.hotelmediabox.repository.data.NoteButton
import com.ufistudio.hotelmediabox.repository.data.Setting
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.zipWith

class SettingViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {

    val initSettingServiceSuccess = MutableLiveData<Pair<Setting?, NoteButton?>>()
    val initSettingServiceProgress = MutableLiveData<Boolean>()
    val initSettingServiceError = MutableLiveData<Throwable>()

    val mGson = Gson()

    init {
        val json = getJsonObject()
        if (json != null) {
            compositeDisposable.add(Single.just(json)
                    .zipWith(Single.just(getNoteButton()))
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
        return mGson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("setting"), Setting::class.java)
    }

    private fun getNoteButton(): NoteButton? {
        return mGson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("bottom_note"), NoteButton::class.java)
                ?: NoteButton()
    }
}