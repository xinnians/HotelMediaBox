package com.ufistudio.hotelmediabox.pages.nearby

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.NearbyMe
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class NearbyMeViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository) : BaseViewModel(application, compositeDisposable) {

    val initNearbyMeSuccess = MutableLiveData<NearbyMe>()
    val initNearbyMeProgress = MutableLiveData<Boolean>()
    val initNearbyMeError = MutableLiveData<Throwable>()


    init {
        val json = getJsonObject()
        if (json != null) {
            compositeDisposable.add(Single.just(getJsonObject())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { initNearbyMeProgress.value = true }
                    .doFinally { initNearbyMeProgress.value = false }
                    .subscribe({ initNearbyMeSuccess.value = it }
                            , { initNearbyMeError.value = it })
            )
        } else {
            initNearbyMeError.value = Throwable("jsonObject is null")
        }
    }

    private fun getJsonObject(): NearbyMe? {
        val gson = Gson()
        return gson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("nearby_me"), NearbyMe::class.java)
    }
}