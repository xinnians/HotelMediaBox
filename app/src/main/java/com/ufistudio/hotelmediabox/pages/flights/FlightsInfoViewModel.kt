package com.ufistudio.hotelmediabox.pages.flights

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.FlightsInfo
import com.ufistudio.hotelmediabox.repository.data.HotelFacilities
import com.ufistudio.hotelmediabox.repository.data.NoteButton
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers

class FlightsInfoViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository) : BaseViewModel(application, compositeDisposable) {

    val initFlightsInfoSuccess = MutableLiveData<Pair<FlightsInfo?, NoteButton?>>()
    val initFlightsInfoProgress = MutableLiveData<Boolean>()
    val initFlightsInfoError = MutableLiveData<Throwable>()

    val mGson = Gson()

    init {
        val json = getJsonObject()
        if (json != null) {
            compositeDisposable.add(Single.just(getJsonObject())
                    .zipWith(Single.just(getNoteButton()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { initFlightsInfoProgress.value = true }
                    .doFinally { initFlightsInfoProgress.value = false }
                    .subscribe({ initFlightsInfoSuccess.value = it }
                            , { initFlightsInfoError.value = it })
            )
        } else {
            initFlightsInfoError.value = Throwable("jsonObject is null")
        }
    }

    private fun getJsonObject(): FlightsInfo? {
        return mGson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("flights_info"), FlightsInfo::class.java)
    }


    private fun getNoteButton(): NoteButton? {
        return mGson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("bottom_note"), NoteButton::class.java)
                ?: NoteButton()
    }
}