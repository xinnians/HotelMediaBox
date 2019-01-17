package com.ufistudio.hotelmediabox.repository.provider.resource

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes

interface AppResource {
    fun resources(): Resources
    fun getString(@StringRes stringRes: Int): String
    fun getColor(@ColorRes colorRes: Int): Int
    fun getDrawable(@DrawableRes drawableRes: Int): Drawable?
}