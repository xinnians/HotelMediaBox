package com.ufistudio.hotelmediabox.pages.factory

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.Config
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.utils.MiscUtils
import com.ufistudio.hotelmediabox.utils.TAG_DEFAULT_APK_NAME
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class FactoryViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {
    private val TAG = FactoryViewModel::class.java.simpleName
    val mApplication = application
    val initConfigSuccess = MutableLiveData<Config>()
    val initConfigProgress = MutableLiveData<Boolean>()
    val initConfigError = MutableLiveData<Throwable>()

    val fileDownloadSuccess = MutableLiveData<String>()
    val fileDownloadProgress = MutableLiveData<Boolean>()
    val fileDownloadError = MutableLiveData<Throwable>()

    init {
        val gson = Gson()
        compositeDisposable.add(Single.fromCallable { gson.fromJson(MiscUtils.getJsonFromStorage("box_config.json"), Config::class.java) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { initConfigProgress.value = true }
                .doFinally { initConfigProgress.value = false }
                .subscribe({ initConfigSuccess.value = it }
                        , { initConfigError.value = it })
        )
    }

    fun downloadFileWithUrl(url: String) {
        fileDownloadProgress.value = true

        compositeDisposable.add(repository.downloadFileWithUrl(url)
                .map {
                    Single.fromCallable { FileUtils.writeResponseBodyToDisk(it, TAG_DEFAULT_APK_NAME) }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                Log.d(TAG, "save file finish $it")
                                fileDownloadProgress.value = false
                                fileDownloadSuccess.value = TAG_DEFAULT_APK_NAME

                            }, {
                                Log.d(TAG, "save file error $it")
                                fileDownloadProgress.value = false
                                fileDownloadError.value = it
                            })
                }
                .subscribe()
        !!)
    }
}