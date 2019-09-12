package com.ufistudio.hotelmediabox.pages.guestService

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Cache.ServerIP
import com.ufistudio.hotelmediabox.constants.Cache.TempAPIGetGuestMessage
import com.ufistudio.hotelmediabox.constants.Cache.TempAPIHeader
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.*
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers

class GuestServiceViewModel(
    application: Application,
    private val compositeDisposable: CompositeDisposable,
    private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {

    val initGuestMessageSuccess = MutableLiveData<Pair<PMS?, NoteButton?>>()
    val initGuestMessageProgress = MutableLiveData<Boolean>()
    val initGuestMessageError = MutableLiveData<Throwable>()

    val mGson = Gson()

    fun initGuestMessage() {
        //call api
        compositeDisposable.add(Single.fromCallable { getJsonObject() }
            .flatMap {
                var url:String = ""
                url =
                if(ServerIP.isNullOrEmpty()){
                    TempAPIHeader+it.config.defaultServerIp+TempAPIGetGuestMessage
                }else{
                    TempAPIHeader+ServerIP+TempAPIGetGuestMessage
                }
                repository.getGuestMessage(url)
//                Single.just(PMS())
            }
            .zipWith(Single.fromCallable { getNoteButton() })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { initGuestMessageProgress.value = true }
            .doFinally { initGuestMessageProgress.value = false }
            .subscribe({
                initGuestMessageSuccess.value = it }
                , { initGuestMessageError.value = it })
        )
    }

    private fun getJsonObject(): Config? {
        return mGson.fromJson(MiscUtils.getJsonFromStorage("box_config.json"), Config::class.java) ?: Config()
    }

    private fun getNoteButton(): NoteButton? {
        return mGson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("bottom_note"), NoteButton::class.java)
            ?: NoteButton()
    }
}