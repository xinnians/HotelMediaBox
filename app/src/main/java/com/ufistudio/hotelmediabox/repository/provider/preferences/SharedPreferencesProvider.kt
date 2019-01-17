package com.ufistudio.hotelmediabox.repository.provider.preferences

import android.app.Application
import android.content.Context

class SharedPreferencesProvider(
        private var application: Application
) : AppSharedPreferences {

    override fun sharedPreferences() = application.getSharedPreferences(NAME, Context.MODE_PRIVATE)!!

    companion object {
        private const val NAME = "FoodSafeData"
    }
}