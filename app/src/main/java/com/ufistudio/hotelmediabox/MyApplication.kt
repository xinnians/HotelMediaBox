package com.ufistudio.hotelmediabox

import android.app.Application

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
    }
}