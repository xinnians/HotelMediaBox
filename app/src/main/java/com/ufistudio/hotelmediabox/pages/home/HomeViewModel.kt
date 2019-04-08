package com.ufistudio.hotelmediabox.pages.home

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.MyApplication
import com.ufistudio.hotelmediabox.helper.TVHelper
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

    init {
        val json = getJsonObject()
        if (json != null) {
            compositeDisposable.add(Single.just(json)
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
        } else {
            initHomeError.value = Throwable("jsonObject is null")
        }
    }

    private fun getJsonObject(): Home? {
        val gson = Gson()
        return gson.fromJson(MiscUtils.getJsonFromStorage("home_en.json"), Home::class.java)
    }

    fun getTVHelper(): TVHelper {
        return getApplication<MyApplication>().getTVHelper()
    }

}