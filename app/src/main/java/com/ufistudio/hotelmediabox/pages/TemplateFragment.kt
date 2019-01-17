package com.ufistudio.hotelmediabox.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener

class TemplateFragment : InteractionView<OnPageInteractionListener.Primary>() {

    private lateinit var mViewModel: TemplateViewModel

    companion object {
        fun newInstance(): TemplateFragment = TemplateFragment()
        private val TAG = TemplateFragment::class.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_template, container, false)
    }

}