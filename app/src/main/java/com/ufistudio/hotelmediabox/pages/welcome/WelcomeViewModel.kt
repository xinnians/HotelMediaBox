package com.ufistudio.hotelmediabox.pages.welcome

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.*
import com.ufistudio.hotelmediabox.repository.remote.ApiClient
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class WelcomeViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {

    val initWelcomeSuccess = MutableLiveData<Welcome>()
    val initWelcomeProgress = MutableLiveData<Boolean>()
    val initWelcomeError = MutableLiveData<Throwable>()

    val getInitialDataSuccess = MutableLiveData<InitialData>()
    val getInitialDataProgress = MutableLiveData<Boolean>()
    val getInitialDataError = MutableLiveData<Throwable>()

    val initNoteButtonSuccess = MutableLiveData<NoteButton>()
    val initNoteButtonProgress = MutableLiveData<Boolean>()
    val initNoteButtonError = MutableLiveData<Throwable>()

    val getSlideShowDataSucess = MutableLiveData<HotelFacilitiesContentList>()
    val getSlideShowDataError = MutableLiveData<Throwable>()


    val mGson: Gson = Gson()

    init {
        val gson = Gson()
        compositeDisposable.add(Single.fromCallable { gson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("welcome"), Welcome::class.java) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { initWelcomeProgress.value = true }
                .doFinally { initWelcomeProgress.value = false }
                .subscribe({
                    Log.d("neo", "welcome = $it")
                    initWelcomeSuccess.value = it
                }, {
                    Log.d("neo", "welcome error= $it")
                    initWelcomeError.value = it
                })
        )
    }

    /**
     * 從Server 讀取Initial data, etc. time 、 guest name ...
     */
    fun getInitialDataFromServer() {
        compositeDisposable.add(
            Single.fromCallable { mGson.fromJson(MiscUtils.getJsonFromStorage("box_config.json"), Config::class.java) }
                .flatMap {
                    repository.getInitialData("http:${it.config.defaultServerIp}/api/device/initial")
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { getInitialDataProgress.value = true }
                .doFinally { getInitialDataProgress.value = false }
                .subscribe({
                    Log.d("WelcomeViewModel", "initial = $it")
                    getInitialDataSuccess.value = it
                }, {
                    Log.d("WelcomeViewModel", "initial error = $it")
                    getInitialDataError.value = it
                })
        )
    }

    fun getSlideShowData(){
        compositeDisposable.add(
            Single.fromCallable { mGson.fromJson(MiscUtils.getJsonFromStorage("box_slideshow.json"), HotelFacilitiesContentList::class.java) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("WelcomeViewModel", "initial = $it")
                    getSlideShowDataSucess.value = it
                }, {
                    Log.d("WelcomeViewModel", "initial error = $it")
                    getSlideShowDataError.value = it
                })
        )
    }

    fun initNoteButton(){
        compositeDisposable.add(Single.fromCallable { mGson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("bottom_note"), NoteButton::class.java) ?: NoteButton() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { initNoteButtonProgress.value = true }
            .doFinally { initNoteButtonProgress.value = false }
            .subscribe({
                initNoteButtonSuccess.value = it
            }, {
                initNoteButtonError.value = it
            })
        )
    }
}