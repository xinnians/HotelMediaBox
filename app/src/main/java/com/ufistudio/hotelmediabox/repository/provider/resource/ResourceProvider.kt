package com.ufistudio.hotelmediabox.repository.provider.resource

import android.app.Application
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat

class ResourceProvider(
        var application: Application
) : AppResource {

    override fun resources(): Resources = application.resources

    override fun getString(stringRes: Int): String = application.getString(stringRes)

    override fun getColor(@ColorRes colorRes: Int): Int = ContextCompat.getColor(application, colorRes)

    override fun getDrawable(@DrawableRes drawableRes: Int): Drawable = ContextCompat.getDrawable(application, drawableRes)!!

}