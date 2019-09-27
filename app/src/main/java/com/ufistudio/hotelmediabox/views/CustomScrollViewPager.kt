package com.ufistudio.hotelmediabox.views

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent

class CustomScrollViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {

    private val scrollable: Boolean = false

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return scrollable
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return scrollable
    }

    override fun executeKeyEvent(event: KeyEvent): Boolean {
        return false
    }

    override fun isSoundEffectsEnabled(): Boolean {
        return false
    }
}