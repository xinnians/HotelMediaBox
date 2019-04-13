package com.ufistudio.hotelmediabox.pages.weather

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.Weather
import com.ufistudio.hotelmediabox.repository.data.HotelFacilities
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class WeatherViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository) : BaseViewModel(application, compositeDisposable) {

    val initWeatherSuccess = MutableLiveData<Weather>()
    val initWeatherProgress = MutableLiveData<Boolean>()
    val initWeatherError = MutableLiveData<Throwable>()


    init {
        val json = getJsonObject()
        if (json != null) {
            compositeDisposable.add(Single.just(getJsonObject())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { initWeatherProgress.value = true }
                    .doFinally { initWeatherProgress.value = false }
                    .subscribe({ initWeatherSuccess.value = it }
                            , { initWeatherError.value = it })
            )
        } else {
            initWeatherError.value = Throwable("jsonObject is null")
        }
    }

    private fun getJsonObject(): Weather? {
        val gson = Gson()
        return gson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("weather"), Weather::class.java)
    }
}