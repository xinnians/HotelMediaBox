package com.ufistudio.hotelmediabox.pages.base

import android.content.Context
import android.view.KeyEvent

interface AppBaseView {
    fun getActivityContext(): Context?
    fun onBackPressed(): Boolean
    fun isActivite(): Boolean
    fun showFullScreenLoading()
    fun dismissFullScreenLoading()
    fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?):Boolean
}