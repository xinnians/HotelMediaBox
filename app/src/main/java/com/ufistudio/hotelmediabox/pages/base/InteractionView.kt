package com.ufistudio.hotelmediabox.pages.base

import android.content.Context

abstract class InteractionView<I : OnPageInteractionListener.Base> : BaseView() {

    private val TAG = InteractionView::class.simpleName

    private lateinit var mInteractionListener: I
    private lateinit var mUnRetriedApis: List<Int>

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            if (parentFragment != null) {
                mInteractionListener = parentFragment as I
            } else {
                mInteractionListener = context as I
            }
        } catch (e: ClassCastException) {
            throw ClassCastException("Either parent fragment or activity should implement OnPageInteractionListener.");
        }

    }

    override fun showFullScreenLoading() {
        getInteractionListener().showFullScreenLoading()
    }

    override fun dismissFullScreenLoading() {
        getInteractionListener().hideFullScreenOverlay()
    }

    protected fun getInteractionListener(): I = mInteractionListener

}