package com.ufistudio.hotelmediabox.pages.base

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MotionEvent
import android.view.View

abstract class BaseView : Fragment(), AppBaseView, View.OnTouchListener{

    private val TAG = BaseView::class.simpleName

    override fun getActivityContext(): Context? = context

    override fun onBackPressed(): Boolean = false

    override fun isActivite(): Boolean {
        if (!isAdded)
            return false
        if (activity == null)
            return false
        if (activity!!.isFinishing)
            return false
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean = true
}