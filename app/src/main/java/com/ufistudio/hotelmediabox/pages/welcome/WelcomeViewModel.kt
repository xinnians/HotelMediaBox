package com.ufistudio.hotelmediabox.pages.welcome

import android.app.Application
import android.arch.lifecycle.MutableLiveData
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
        val jsonModel = gson.fromJson(MiscUtils.getJsonFromStorage("/json/welcome", "welcome_en.json"), Welcome::class.java)

        if (jsonModel == null) {
            initWelcomeError.value = Throwable("Data in not exists")
        } else {
            compositeDisposable.add(Single.just(jsonModel)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { initWelcomeProgress.value = true }
                    .doFinally { initWelcomeProgress.value = false }
                    .subscribe({ initWelcomeSuccess.value = it }
                            , { initWelcomeError.value = it })
            )
        }
    }
}