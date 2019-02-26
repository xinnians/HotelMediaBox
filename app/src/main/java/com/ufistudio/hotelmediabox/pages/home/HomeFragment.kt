package com.ufistudio.hotelmediabox.pages.home

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.TemplateViewModel
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import kotlinx.android.synthetic.main.fragment_home.*
import android.util.DisplayMetrics
import android.util.Log
import com.ufistudio.hotelmediabox.pages.MainActivity
import android.support.v4.content.ContextCompat
import android.app.Dialog
import android.content.Intent
import com.ufistudio.hotelmediabox.pages.FullScreenActivity


class HomeFragment : InteractionView<OnPageInteractionListener.Primary>() {

    private lateinit var mViewModel: TemplateViewModel
    private var mAdapter = FunctionsAdapter()

    private lateinit var mExoPlayerHelper: ExoPlayerHelper

    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
        private val TAG = HomeFragment::class.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mExoPlayerHelper = ExoPlayerHelper()
    }

    override fun onStart() {
        super.onStart()

        list_functions.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        list_functions.adapter = mAdapter

        mExoPlayerHelper.initPlayer(context, videoView)
        mExoPlayerHelper.setMp4Source(R.raw.videoplayback)

        videoView.setOnClickListener { activity?.startActivity(Intent(activity, FullScreenActivity::class.java)) }


    }
    override fun onStop() {
        super.onStop()
        mExoPlayerHelper.release()
    }
}