package com.ufistudio.hotelmediabox.repository.viewModel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel(application: Application,
                             private val compositeDisposable: CompositeDisposable) : AndroidViewModel(application) {

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}