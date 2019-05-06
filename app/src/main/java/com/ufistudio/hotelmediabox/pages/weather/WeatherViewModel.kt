package com.ufistudio.hotelmediabox.pages.weather

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.*
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers

class WeatherViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository) : BaseViewModel(application, compositeDisposable) {

    val initWeatherSuccess = MutableLiveData<Pair<Weather?, NoteButton?>>()
    val initWeatherProgress = MutableLiveData<Boolean>()
    val initWeatherError = MutableLiveData<Throwable>()

    val getWeatherInfoSuccess = MutableLiveData<WeatherInfo?>()
    val getWeatherInfoProgress = MutableLiveData<Boolean>()
    val getWeatherInfoError = MutableLiveData<Throwable>()

    val mGson = Gson()

    init {
        val json = getJsonObject()
        if (json != null) {
            compositeDisposable.add(Single.just(json)
                    .zipWith(Single.just(getNoteButton()))
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
        return mGson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("weather"), Weather::class.java)
    }

    private fun getNoteButton(): NoteButton? {
        return mGson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("bottom_note"), NoteButton::class.java)
                ?: NoteButton()
    }

    private fun getConfig(): Config? {
        return mGson.fromJson(MiscUtils.getJsonFromStorage("box_config.json"), Config::class.java)
    }

    fun getWeather(cityCode: String) {
        val json = getConfig()
        compositeDisposable.add(Single.just(json)
                .map {
                    repository.getWeatherInfo("http:${it.config.defaultServerIp}/api/weather", cityCode)
                            .observeOn(AndroidSchedulers.mainThread())
                            .doFinally { getWeatherInfoProgress.value = false }
                            .subscribe({ getWeatherInfoSuccess.value = it }
                                    , { getWeatherInfoError.value = it })
                }.subscribe()
        )
    }
}