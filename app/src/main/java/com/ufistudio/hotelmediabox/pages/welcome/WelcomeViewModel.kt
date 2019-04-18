package com.ufistudio.hotelmediabox.pages.welcome

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.text.TextUtils
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.Welcome
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class WelcomeViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {

    val initWelcomeSuccess = MutableLiveData<Welcome>()
    val initWelcomeProgress = MutableLiveData<Boolean>()
    val initWelcomeError = MutableLiveData<Throwable>()

    init {
        val gson = Gson()
        val jsonObject = gson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("welcome"), Welcome::class.java)
        if (jsonObject != null) {
            compositeDisposable.add(Single.just(jsonObject)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { initWelcomeProgress.value = true }
                    .doFinally { initWelcomeProgress.value = false }
                    .subscribe({ initWelcomeSuccess.value = it }
                            , { initWelcomeError.value = it })
            )
        }else{
            initWelcomeError.value = Throwable("jsonObject is null")
        }
    }
}