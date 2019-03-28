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

class HotelFacilitiesViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository) : BaseViewModel(application, compositeDisposable) {

    val initHotelFacilitiesSuccess = MutableLiveData<HotelFacilities>()
    val initHotelFacilitiesProgress = MutableLiveData<Boolean>()
    val initHotelFacilitiesError = MutableLiveData<Throwable>()


    init {
        val gson = Gson()

        compositeDisposable.add(Single.just(gson.fromJson(MiscUtils.getJsonFromStorage("hotel_facilities_en.json"), HotelFacilities::class.java))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { initHotelFacilitiesProgress.value = true }
                .doFinally { initHotelFacilitiesProgress.value = false }
                .subscribe({ initHotelFacilitiesSuccess.value = it }
                        , { initHotelFacilitiesError.value = it })
        )
    }
}