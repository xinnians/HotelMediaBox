package com.ufistudio.hotelmediabox.pages.home

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
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
import android.widget.Toast
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.interfaces.OnSaveFileStatusListener
import com.ufistudio.hotelmediabox.pages.factory.FactoryActivity
import com.ufistudio.hotelmediabox.repository.data.Home
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.utils.MiscUtils
import okhttp3.ResponseBody
import java.io.IOException


class HomeFragment : InteractionView<OnPageInteractionListener.Primary>(), FunctionsAdapter.OnItemClickListener,
        OnSaveFileStatusListener {

    private val TAG_TYPE_1 = 1//Weather Information
    private val TAG_TYPE_2 = 2//Promo Banner

    private lateinit var mViewModel: HomeViewModel
    private var mAdapter = FunctionsAdapter()

    private lateinit var mExoPlayerHelper: ExoPlayerHelper

    private var mData: Home? = null
    private var mChannelIndex = 0
    private var mDownloadDialog: AlertDialog? = null
    private var mSpecialCount: Int = 0

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

        mViewModel.initHomeProgress.observe(this, Observer {

        })
        mViewModel.initHomeSuccess.observe(this, Observer {
            onLocalJsonSuccess(it)
        })
        mViewModel.initHomeError.observe(this, Observer { onLocalJsonFailed(it) })

        mViewModel.fileDownloadProgress.observe(this, Observer {
            downloadFileProgress(it)
        })
        mViewModel.fileDownloadError.observe(this, Observer {
            downloadFileFailed(it)
        })
        mViewModel.fileDownloadSuccess.observe(this, Observer {
            downloadFileSuccess(it!!)
        })

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
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                mSpecialCount++
            }
        }

        if (mSpecialCount == 10) {
            mSpecialCount = 0
            startActivity(Intent(context, FactoryActivity::class.java))
//            AlertDialog.Builder(context)
//                    .setTitle("是否下載更新欓")
//                    .setPositiveButton(android.R.string.ok, object : DialogInterface.OnClickListener {
//                        override fun onClick(dialog: DialogInterface?, which: Int) {
//                            mViewModel.downloadFileWithUrl("https://drive.google.com/uc?authuser=0&id=11m95GpoQms-lbcdSXGgrWnJdePGZD59M&export=download")
//                        }
//                    })
//                    .setNegativeButton(android.R.string.cancel, null)
//                    .setCancelable(false)
//                    .create()
//                    .show()
        }
        return false
    }

    override fun onClick(view: View) {
        if (view.tag as Int == -100) {
            Toast.makeText(context, "尚未實作", Toast.LENGTH_SHORT).show()
            return
        }
        getInteractionListener().switchPage(R.id.fragment_container, view.tag as Int, Bundle(), true, false)
    }

    private fun onLocalJsonFailed(it: Throwable?) {
        Log.d(TAG, "onLocalJsonFailed => $it")
    }

    private fun onLocalJsonSuccess(data: Home?) {
        mData = data
        mAdapter.setData(mData?.home?.icons)

        switchWedge(mData?.home?.stage_type?.type)
    }

    private fun downloadFileSuccess(it: ResponseBody) {
        FileUtils.writeResponseBodyToDisk(it, "hotelbox.apk", listener = this)
    }

    private fun downloadFileFailed(it: Throwable?) {
        Log.d(TAG, "downloadFileFailed => $it")
    }

    private fun downloadFileProgress(it: Boolean?) {
        if (it == true) {
            if (mDownloadDialog == null)
                mDownloadDialog = AlertDialog.Builder(context)
                        .setTitle("下載中")
                        .setView(R.layout.view_progress)
                        .setCancelable(false)
                        .create()

            mDownloadDialog?.show()
        } else {
            Log.d("neo", "cancel")
        }
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

    override fun savingFileStart() {
        Log.d(TAG, "savingFileStart")
    }

    override fun savingFile(progress: Long) {
        Log.d(TAG, "progress =  $progress")
    }

    override fun savingFileEnd() {
    }

    override fun savingFileError(e: IOException) {
        Log.d(TAG, "savingFileError + $e")
        mDownloadDialog?.cancel()
    }

    override fun savingFileSuccess(path: String, name: String) {
        mDownloadDialog?.cancel()
        MiscUtils.installApk(context!!, name)
    }
}