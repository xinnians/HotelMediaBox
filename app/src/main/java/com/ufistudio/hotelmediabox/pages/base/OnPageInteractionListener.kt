package com.ufistudio.hotelmediabox.pages.base

import android.os.Bundle
import android.support.annotation.IdRes

interface OnPageInteractionListener {

    interface Base {
        fun pressBack()
        fun showFullScreenLoading()
        fun hideFullScreenOverlay()
    }

    interface Pane : Base {
        fun switchPage(@IdRes container: Int, page: Int, args: Bundle, addToBackStack: Boolean, withAnimation: Boolean)
        fun addPage(@IdRes container: Int, page: Int, args: Bundle, addToBackStack: Boolean, withAnimation: Boolean)
    }

    interface Primary : Pane
}