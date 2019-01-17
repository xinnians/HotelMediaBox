package com.ufistudio.hotelmediabox.repository

import android.app.Application
import com.ufistudio.hotelmediabox.repository.provider.preferences.SharedPreferencesProvider
import com.ufistudio.hotelmediabox.repository.remote.RemoteAPI

class Repository(private var application: Application, private val sharedPreferencesProvider: SharedPreferencesProvider) {

    val TAG = Repository::class.simpleName

    init {
        RemoteAPI.init(application)
    }
}