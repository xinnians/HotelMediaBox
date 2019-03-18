package com.ufistudio.hotelmediabox.pages.home

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import kotlinx.android.synthetic.main.fragment_home.*
import android.view.KeyEvent
import android.widget.ImageView
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.repository.data.Home


class HomeFragment : InteractionView<OnPageInteractionListener.Primary>(), FunctionsAdapter.OnItemClickListener {

    private val TAG_TYPE_1 = 1//Weather Information
    private val TAG_TYPE_2 = 2//Promo Banner

    private lateinit var mViewModel: HomeViewModel
    private var mAdapter = FunctionsAdapter()

    private lateinit var mExoPlayerHelper: ExoPlayerHelper

    private var mData: Home? = null
    private var mChannelIndex = 0

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
//        mTestUdpList.add("udp://239.1.1.1:3990")

        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initHomeProgress.observe(this, Observer { })
        mViewModel.initHomeSuccess.observe(this, Observer {
            onSuccess(it)
        })
        mViewModel.initHomeError.observe(this, Observer { })

        mExoPlayerHelper = ExoPlayerHelper()
    }


    override fun onStart() {
        super.onStart()

        list_functions.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        list_functions.adapter = mAdapter

        mExoPlayerHelper.initPlayer(context, videoView)
//        mExoPlayerHelper.setUdpSource(mTestUdpList.get(mChannelIndex))
        mExoPlayerHelper.setMp4Source(R.raw.videoplayback)

        videoView.setOnClickListener {
            mExoPlayerHelper.fullScreen()

        }
        mAdapter.setItemClickListener(this)

    }

    override fun onResume() {
        super.onResume()

    }

    override fun onStop() {
        super.onStop()
        mExoPlayerHelper.release()
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_CHANNEL_UP -> {
//                if (mChannelIndex != mTestUdpList.size - 1) {
//                    mChannelIndex++
//                    mExoPlayerHelper.stop()
//                    mExoPlayerHelper.setUdpSource(mTestUdpList[mChannelIndex])
//                }
                return true
            }
            KeyEvent.KEYCODE_CHANNEL_DOWN -> {
//                if (mChannelIndex != 0) {
//                    mChannelIndex--
//                    mExoPlayerHelper.stop()
//                    mExoPlayerHelper.setUdpSource(mTestUdpList[mChannelIndex])
//                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                mExoPlayerHelper.fullScreen()
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                if (mExoPlayerHelper.isFullscreen())
                    mExoPlayerHelper.fullScreen()
                return true
            }
        }
        return false
    }

    override fun onClick(view: View) {
        getInteractionListener().switchPage(R.id.fragment_container, view.tag as Int, Bundle(), true, false)
    }

    private fun onSuccess(data: Home?) {
        mData = data
        mAdapter.setData(mData?.home?.icons)

        switchWedge(mData?.home?.stage_type?.type)
    }

    private fun switchWedge(type: Int?) {
        when (type) {
            TAG_TYPE_1 -> {
                view_wedge.layoutResource = R.layout.view_home_weather
            }
            TAG_TYPE_2 -> {
                view_wedge.layoutResource = R.layout.view_home_banner
                val view = view_wedge.inflate()

//                val banner: ImageView = view.findViewById(R.id.image_banner)
//                banner.setBackgroundColor(ContextCompat.getColor(context!!, android.R.color.holo_blue_bright))
            }
        }
    }

}