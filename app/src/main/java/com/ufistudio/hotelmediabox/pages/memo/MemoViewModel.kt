package com.ufistudio.hotelmediabox.pages.memo

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.NoteButton
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MemoViewModel(
    application: Application,
    private val compositeDisposable: CompositeDisposable,
    private val repository: Repository
) : BaseViewModel(application, compositeDisposable)  {

    val initNoteButtonSuccess = MutableLiveData<NoteButton>()
    val initNoteButtonProgress = MutableLiveData<Boolean>()
    val initNoteButtonError = MutableLiveData<Throwable>()

    val mGson: Gson = Gson()

    fun initNoteButton(){
        compositeDisposable.add(Single.fromCallable { mGson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("bottom_note"), NoteButton::class.java) ?: NoteButton() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { initNoteButtonProgress.value = true }
            .doFinally { initNoteButtonProgress.value = false }
            .subscribe({
                initNoteButtonSuccess.value = it
            }, {
                initNoteButtonError.value = it
            })
        )
    }
}