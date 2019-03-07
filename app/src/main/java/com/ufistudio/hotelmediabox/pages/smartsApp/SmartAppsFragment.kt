package com.ufistudio.hotelmediabox.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener

class SmartAppsFragment : InteractionView<OnPageInteractionListener.Primary>() {

    private lateinit var mViewModel: SmartAppsViewModel

    companion object {
        fun newInstance(): SmartAppsFragment = SmartAppsFragment()
        private val TAG = SmartAppsFragment::class.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_smart_apps, container, false)
    }

    override fun onStart() {
        super.onStart()
    }
}