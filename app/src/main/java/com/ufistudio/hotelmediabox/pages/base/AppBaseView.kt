package com.ufistudio.hotelmediabox.pages.base

import android.content.Context

interface AppBaseView {
    fun getActivityContext(): Context?
    fun onBackPressed(): Boolean
    fun isActivite(): Boolean
    fun showFullScreenLoading()
    fun dismissFullScreenLoading()
}