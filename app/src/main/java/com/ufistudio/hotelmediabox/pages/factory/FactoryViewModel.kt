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

        val jsonObject = gson.fromJson(MiscUtils.getJsonFromStorage("box_config.json"), Config::class.java)
        if (jsonObject != null) {
            compositeDisposable.add(Single.just(jsonObject)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { initConfigProgress.value = true }
                    .doFinally { initConfigProgress.value = false }
                    .subscribe({ initConfigSuccess.value = it }
                            , { initConfigError.value = it })
            )
        } else {
            initConfigError.value = Throwable("jsonObject is null")
        }
    }

    fun downloadFileWithUrl(url: String) {
        fileDownloadProgress.value = true

        compositeDisposable.add(repository.downloadFileWithUrl(url)
                .map {
                    Single.just(FileUtils.writeResponseBodyToDisk(it, TAG_DEFAULT_APK_NAME))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                Log.d(TAG, "save file finish $it")

                            }, {
                                Log.d(TAG, "save file error $it")
                            })
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d(TAG, " save success = $it")
                    fileDownloadProgress.value = false
                    fileDownloadSuccess.value = TAG_DEFAULT_APK_NAME
                }, {
                    Log.d(TAG, " save error = $it")
                    fileDownloadProgress.value = false
                    fileDownloadError.value = it
                })
        !!)
    }
}