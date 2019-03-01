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
import android.view.KeyEvent


class HomeFragment : InteractionView<OnPageInteractionListener.Primary>(), FunctionsAdapter.OnItemClickListener {

    private lateinit var mViewModel: TemplateViewModel
    private var mAdapter = FunctionsAdapter()

    private lateinit var mExoPlayerHelper: ExoPlayerHelper

    private val mTestUdpList = ArrayList<String>()
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

        mExoPlayerHelper = ExoPlayerHelper()

        mTestUdpList.add("udp://239.1.1.1:3990")
        mTestUdpList.add("udp://239.1.1.2:3990")
        mTestUdpList.add("udp://239.1.1.3:3990")
        mTestUdpList.add("udp://239.1.1.4:3990")
        mTestUdpList.add("udp://239.1.1.5:3990")
        mTestUdpList.add("udp://239.1.1.6:3990")
        mTestUdpList.add("udp://239.1.1.7:3990")
        mTestUdpList.add("udp://239.1.1.8:3990")
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
                if (mChannelIndex != mTestUdpList.size - 1) {
                    mChannelIndex++
                    mExoPlayerHelper.stop()
                    mExoPlayerHelper.setUdpSource(mTestUdpList[mChannelIndex])
                }
                return true
            }
            KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                if (mChannelIndex != 0) {
                    mChannelIndex--
                    mExoPlayerHelper.stop()
                    mExoPlayerHelper.setUdpSource(mTestUdpList[mChannelIndex])
                }
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
}