package com.ufistudio.hotelmediabox.pages.facilies

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.HotelFacilities
import com.ufistudio.hotelmediabox.repository.data.NoteButton
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers

class HotelFacilitiesViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository) : BaseViewModel(application, compositeDisposable) {

    val initHotelFacilitiesSuccess = MutableLiveData<Pair<HotelFacilities?, NoteButton?>>()
    val initHotelFacilitiesProgress = MutableLiveData<Boolean>()
    val initHotelFacilitiesError = MutableLiveData<Throwable>()
    val mGson = Gson()

    init {
        val json = getJsonObject()
        if (json != null) {
            compositeDisposable.add(Single.fromCallable { getJsonObject() }
                    .zipWith(Single.fromCallable { getNoteButton() })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { initHotelFacilitiesProgress.value = true }
                    .doFinally { initHotelFacilitiesProgress.value = false }
                    .subscribe({ initHotelFacilitiesSuccess.value = it }
                            , { initHotelFacilitiesError.value = it })
            )
        } else {
            initHotelFacilitiesError.value = Throwable("jsonObject is null")
        }
    }

    private fun getJsonObject(): HotelFacilities? {
        return mGson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("hotel_facilities"), HotelFacilities::class.java)
    }

    private fun getNoteButton(): NoteButton? {
        return mGson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("bottom_note"), NoteButton::class.java)
                ?: NoteButton()
    }
}