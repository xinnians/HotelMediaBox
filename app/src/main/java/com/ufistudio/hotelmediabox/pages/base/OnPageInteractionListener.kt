package com.ufistudio.hotelmediabox.pages.base

import android.os.Bundle
import android.support.annotation.IdRes
import com.ufistudio.hotelmediabox.interfaces.OnFragmentKeyListener

interface OnPageInteractionListener {

    interface Base {
        fun pressBack()
        fun showFullScreenLoading()
        fun hideFullScreenOverlay()
    }

    interface Pane : Base {
        fun switchPage(@IdRes container: Int, page: Int, args: Bundle, addToBackStack: Boolean, withAnimation: Boolean, createNewFragment: Boolean = false)
        fun addPage(@IdRes container: Int, page: Int, args: Bundle, addToBackStack: Boolean, withAnimation: Boolean)
    }

    interface Primary : Pane {
        fun setFragmentCacheData(data: Any?)
        fun getFragmentCacheData(): Any?
        fun clearFragmentCacheData()
        fun setOnKeyListener(listener: OnFragmentKeyListener?)
        fun getOnKeyListener(): OnFragmentKeyListener?
    }
}