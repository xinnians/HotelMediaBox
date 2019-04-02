package com.ufistudio.hotelmediabox.pages.fullScreen

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.TVChannel
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class FullScreenViewModel(
    application: Application,
    private val compositeDisposable: CompositeDisposable,
    private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {

    val initChannelsSuccess = MutableLiveData<ArrayList<TVChannel>>()
    val initChannelsProgress = MutableLiveData<Boolean>()
    val initChannelsError = MutableLiveData<Throwable>()

    fun initChannels() {

        val jsonObject: Array<TVChannel> =
            Gson().fromJson(MiscUtils.getJsonFromStorage("channels.json"), Array<TVChannel>::class.java) ?: return
        compositeDisposable.add(
            Single.just(jsonObject)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { initChannelsProgress.value = true }
                .doFinally { initChannelsProgress.value = false }
                .subscribe({ initChannelsSuccess.value = jsonObject.toCollection(ArrayList()) }
                    , { initChannelsError.value = it })
        )
    }

}