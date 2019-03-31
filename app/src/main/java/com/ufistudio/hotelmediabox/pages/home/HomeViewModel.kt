package com.ufistudio.hotelmediabox.pages.home

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.Home
import com.ufistudio.hotelmediabox.repository.data.TVChannel
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

    val initChannelsSuccess = MutableLiveData<ArrayList<TVChannel>>()
    val initChannelsProgress = MutableLiveData<Boolean>()
    val initChannelsError = MutableLiveData<Throwable>()

    init {

        compositeDisposable.add(Single.just(getJsonObject())
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
        val gson = Gson()
        val jsonObject = gson.fromJson(MiscUtils.getJsonFromStorage("home_en.json"), Home::class.java)
        if (jsonObject != null) {
            return jsonObject
        }

        return null
    }

    fun initChannels(){

        val jsonObject: Array<TVChannel> = Gson().fromJson(MiscUtils.getJsonFromStorage("channels.json"), Array<TVChannel>::class.java)
        compositeDisposable.add(Single.just(jsonObject)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { initChannelsProgress.value = true }
            .doFinally { initChannelsProgress.value = false }
            .subscribe({ initChannelsSuccess.value = jsonObject.toCollection(ArrayList()) }
                , { initChannelsError.value = it })
        )
    }
}