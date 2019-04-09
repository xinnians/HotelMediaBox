package com.ufistudio.hotelmediabox.pages.roomService

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.RoomServices
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class RoomServiceViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {

    val initRoomServiceSuccess = MutableLiveData<RoomServices>()
    val initRoomServiceProgress = MutableLiveData<Boolean>()
    val initRoomServiceError = MutableLiveData<Throwable>()

    init {
        val json = getJsonObject()
        if (json != null) {
            compositeDisposable.add(Single.just(getJsonObject())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { initRoomServiceProgress.value = true }
                    .doFinally { initRoomServiceProgress.value = false }
                    .subscribe({ initRoomServiceSuccess.value = it }
                            , { initRoomServiceError.value = it })
            )
        } else {
            initRoomServiceError.value = Throwable("jsonObject is null")
        }
    }

    private fun getJsonObject(): RoomServices? {
        val gson = Gson()
        return gson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("room_service"), RoomServices::class.java)
    }
}