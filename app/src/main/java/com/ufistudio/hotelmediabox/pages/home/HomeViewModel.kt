package com.ufistudio.hotelmediabox.pages.home

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.MyApplication
import com.ufistudio.hotelmediabox.constants.Cache
import com.ufistudio.hotelmediabox.helper.TVHelper
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.Config
import com.ufistudio.hotelmediabox.repository.data.Home
import com.ufistudio.hotelmediabox.repository.data.NoteButton
import com.ufistudio.hotelmediabox.repository.data.WeatherInfo
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class HomeViewModel(
    application: Application,
    private val compositeDisposable: CompositeDisposable,
    private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {

    val initHomeSuccess = MutableLiveData<Home>()
    val initHomeProgress = MutableLiveData<Boolean>()
    val initHomeError = MutableLiveData<Throwable>()

    val getWeatherInfoSuccess = MutableLiveData<WeatherInfo?>()
    val getWeatherInfoProgress = MutableLiveData<Boolean>()
    val getWeatherInfoError = MutableLiveData<Throwable>()

    val mGson = Gson()

    init {
        compositeDisposable.add(Single.fromCallable { getJsonObject() }
            .map { result ->
                Cache.IsVODEnable = result.home.icons[1].enable == 1
                return@map result
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                initHomeProgress.value = true
            }
            .doFinally {
                initHomeProgress.value = false
            }
            .subscribe({ initHomeSuccess.value = it }
                , { initHomeError.value = it })
        )
    }

    private fun getJsonObject(): Home? {
        return mGson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("home"), Home::class.java) ?: Home()
    }

    fun getTVHelper(): TVHelper {
        return getApplication<MyApplication>().getTVHelper()
    }

    private fun getConfig(): Config? {
        return mGson.fromJson(MiscUtils.getJsonFromStorage("box_config.json"), Config::class.java) ?: Config()
    }

    fun getWeather(cityCode: String) {
        compositeDisposable.add(
            Single.fromCallable { getConfig() }
                .map {
                    repository.getWeatherInfo("http:${it.config.defaultServerIp}/api/weather", cityCode)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally { getWeatherInfoProgress.value = false }
                        .subscribe({ getWeatherInfoSuccess.value = it }
                            , { getWeatherInfoError.value = it })
                }.subscribe()
        )
    }

    val initNoteButtonSuccess = MutableLiveData<NoteButton?>()
    val initNoteButtonProgress = MutableLiveData<Boolean>()
    val initNoteButtonError = MutableLiveData<Throwable>()

    fun initNoteButton() {
        compositeDisposable.add(Single.fromCallable {
            mGson.fromJson(
                MiscUtils.getJsonLanguageAutoSwitch("bottom_note"),
                NoteButton::class.java
            ) ?: NoteButton()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { initNoteButtonProgress.value = true }
            .doFinally { initNoteButtonProgress.value = false }
            .subscribe({ initNoteButtonSuccess.value = it }
                , { initNoteButtonError.value = it })
        )
    }
}