package com.ufistudio.hotelmediabox.pages.facilies

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.HotelFacilities
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class HotelFacilitiesViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository) : BaseViewModel(application, compositeDisposable) {

    val initHotelFacilitiesSuccess = MutableLiveData<HotelFacilities>()
    val initHotelFacilitiesProgress = MutableLiveData<Boolean>()
    val initHotelFacilitiesError = MutableLiveData<Throwable>()


    init {
        val json = getJsonObject()
        if (json != null) {
            compositeDisposable.add(Single.just(getJsonObject())
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
        val gson = Gson()
        return gson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("hotel_facilities"), HotelFacilities::class.java)
    }
}