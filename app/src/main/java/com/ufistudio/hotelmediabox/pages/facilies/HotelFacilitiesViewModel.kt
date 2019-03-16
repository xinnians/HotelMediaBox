package com.ufistudio.hotelmediabox.pages.facilies

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.HotelFacilities
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.io.IOException
import java.nio.charset.Charset

class HotelFacilitiesViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository) : BaseViewModel(application, compositeDisposable) {

    val initHotelFacilitiesSuccess = MutableLiveData<HotelFacilities>()
    val initHotelFacilitiesProgress = MutableLiveData<Boolean>()
    val initHotelFacilitiesError = MutableLiveData<Throwable>()


    init {
        var gson = Gson()
        var jsonString = ""
        try {
            val inputStream = application.assets.open("room_service.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            jsonString = String(buffer, Charset.forName("UTF-8"))

        } catch (e: IOException) {
            e.printStackTrace()
        }
        var jsonModel = gson.fromJson(jsonString, HotelFacilities::class.java)

        compositeDisposable.add(Single.just(jsonModel)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { initHotelFacilitiesProgress.value = true }
                .doFinally { initHotelFacilitiesProgress.value = false }
                .subscribe({ initHotelFacilitiesSuccess.value = it }
                        , { initHotelFacilitiesError.value = it })
        )
    }
}