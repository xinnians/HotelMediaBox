package com.ufistudio.hotelmediabox.pages.vod

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.NoteButton
import com.ufistudio.hotelmediabox.repository.data.Vod
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers

class VodViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository) : BaseViewModel(application, compositeDisposable) {

    val initVodSuccess = MutableLiveData<Pair<Vod?, NoteButton?>>()
    val initVodProgress = MutableLiveData<Boolean>()
    val initVodError = MutableLiveData<Throwable>()

    val mGson = Gson()

    init {
        val json = getJsonObject()
        if (json != null) {
            compositeDisposable.add(Single.just(getJsonObject())
                    .zipWith(Single.just(getNoteButton()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { initVodProgress.value = true }
                    .doFinally { initVodProgress.value = false }
                    .subscribe({ initVodSuccess.value = it }
                            , { initVodError.value = it })
            )
        } else {
            initVodError.value = Throwable("jsonObject is null")
        }
    }

    private fun getJsonObject(): Vod? {
        return mGson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("vod"), Vod::class.java)
    }

    private fun getNoteButton(): NoteButton? {
        return mGson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("bottom_note"), NoteButton::class.java)
                ?: NoteButton()
    }
}