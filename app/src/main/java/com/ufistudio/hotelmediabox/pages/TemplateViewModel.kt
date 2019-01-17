package com.ufistudio.hotelmediabox.pages

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class TemplateViewModel(
    application: Application,
    private val compositeDisposable: CompositeDisposable,
    private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {


}