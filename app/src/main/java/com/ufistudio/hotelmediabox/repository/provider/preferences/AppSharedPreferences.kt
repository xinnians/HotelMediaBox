package com.ufistudio.hotelmediabox.repository.provider.preferences

import android.content.SharedPreferences

interface AppSharedPreferences {
    fun sharedPreferences(): SharedPreferences
}