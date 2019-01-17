package com.ufistudio.hotelmediabox

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.ufistudio.hotelmediabox.pages.TemplateFragment
import com.ufistudio.hotelmediabox.pages.TemplateViewModel
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.provider.preferences.SharedPreferencesProvider
import com.ufistudio.hotelmediabox.repository.provider.resource.ResourceProvider
import io.reactivex.disposables.CompositeDisposable


class ViewModelFactory(private val application: Application,
                       private val repository: Repository,
                       private val preferences: SharedPreferencesProvider,
                       private val resource: ResourceProvider
) : ViewModelProvider.NewInstanceFactory() {

    //ViewModel需要的model再打進去

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return with(modelClass) {
            when {
                isAssignableFrom(TemplateFragment::class.java) -> TemplateViewModel(application, CompositeDisposable(), repository)
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            } as T
        }
    }
}