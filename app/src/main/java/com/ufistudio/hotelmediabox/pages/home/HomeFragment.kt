package com.ufistudio.hotelmediabox.pages.home

import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import kotlinx.android.synthetic.main.fragment_home.*
import android.widget.Toast
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.DVBHelper
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.interfaces.ViewModelsCallback
import com.ufistudio.hotelmediabox.pages.channel.ChannelFragment
import com.ufistudio.hotelmediabox.pages.factory.FactoryActivity
import com.ufistudio.hotelmediabox.pages.fullScreen.FullScreenActivity
import com.ufistudio.hotelmediabox.repository.data.Home
import com.ufistudio.hotelmediabox.repository.data.HomeIcons
import com.ufistudio.hotelmediabox.repository.data.TVChannel
import com.ufistudio.hotelmediabox.utils.FileUtils
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class HomeFragment : InteractionView<OnPageInteractionListener.Primary>(), FunctionsAdapter.OnItemClickListener,
    ViewModelsCallback {
    private val TAG_TYPE_1 = 1//Weather Information
    private val TAG_TYPE_2 = 2//Promo Banner

    private lateinit var mViewModel: HomeViewModel
    private var mAdapter = FunctionsAdapter()

    private lateinit var mExoPlayerHelper: ExoPlayerHelper

    private var mData: Home? = null
    private var mChannelIndex = 0
    private var mSpecialCount: Int = 0
    private var mFeatureIcons: ArrayList<HomeIcons>? = null
    private var mIsRendered: Boolean = false //判斷塞資料了沒

    private var mChannelList: ArrayList<TVChannel>? = null
    private var mPlayPosition = 0
    private var mDisposable: Disposable? = null

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
            onSuccess(it)
        })
        mViewModel.initHomeError.observe(this, Observer { onError(it) })

        //initChannel
        mViewModel.initChannelsSuccess.observe(this, Observer { list -> list?.let { initChannelsSuccess(it) } })
        mViewModel.initChannelsProgress.observe(this, Observer { isProgress ->
            initChannelsProgress(isProgress ?: false)
        })
        mViewModel.initChannelsError.observe(this, Observer { throwable -> throwable?.let { initChannelsError(it) } })

        mExoPlayerHelper = ExoPlayerHelper()
    }

    override fun onStart() {
        super.onStart()

        mViewModel.initChannels()
        dvbView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                holder?.setFormat(PixelFormat.TRANSPARENT)
            }
        })

        list_functions.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        list_functions.adapter = mAdapter

        mExoPlayerHelper.initPlayer(context, videoView)
//        mExoPlayerHelper.setUdpSource(mTestUdpList.get(mChannelIndex))
//        mExoPlayerHelper.setMp4Source(R.raw.videoplayback)

        videoView.setOnClickListener {
            //            mExoPlayerHelper.fullScreen()
            startActivity(Intent(context, FullScreenActivity::class.java))

        }
        mAdapter.setItemClickListener(this)
        renderView()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onStop() {
        super.onStop()
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable?.dispose()
        }
        DVBHelper.getDVBPlayer().closePlayer()
        mExoPlayerHelper.release()
        mIsRendered = false
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_CHANNEL_UP -> {
//                if (mChannelIndex != mTestUdpList.size - 1) {
//                    mChannelIndex++
//                    mExoPlayerHelper.stop()
//                    mExoPlayerHelper.setUdpSource(mTestUdpList[mChannelIndex])
//                }
                playTv(true)

                return true
            }
            KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                playTv(false)
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
        }
        return false
    }

    override fun onClick(view: View) {
        if (view.tag as Int == -100) {
            Toast.makeText(context, "尚未實作", Toast.LENGTH_SHORT).show()
            return
        }
        val b = Bundle()
        b.putParcelableArrayList(Page.ARG_BUNDLE, mFeatureIcons)
        getInteractionListener().switchPage(R.id.fragment_container, view.tag as Int, b, true, false, true)
    }

    override fun onSuccess(it: Any?) {
        mData = it as Home?
        mFeatureIcons = mData?.home?.icons

        renderView()
    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "onError => $t")
    }

    override fun onProgress(b: Boolean) {
    }

    /**
     * 塞資料
     */
    private fun renderView() {
        if (!mIsRendered) {
            if (mData?.home?.stage_type?.type != null && mFeatureIcons != null) {
                mIsRendered = true
                switchWedge(mData?.home?.stage_type?.type)
                mAdapter.setData(mFeatureIcons)
            }
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

                Glide.with(this)
                    .load(FileUtils.getFileFromStorage(mData?.home?.promo_banner!![0].image))
                    .into(view.findViewById(R.id.image_banner))
            }
        }
    }

    private fun initChannelsSuccess(list: ArrayList<TVChannel>) {
        mChannelList = list
        playTv("")
    }

    private fun initChannelsProgress(isProgress: Boolean) {
        Log.e(TAG, "initChannelsProgress call. isProgress:$isProgress")
    }

    private fun initChannelsError(throwable: Throwable) {
        Log.e(TAG, "initChannelsError call. ${throwable.message}")
    }

    private fun playTv(action: Any) {
        Log.e(TAG, "[playTV] call action:$action")
        when (action) {
            is String -> {
                mPlayPosition = 0
                mChannelList?.let { list ->
                    if (list.size != 0 && list.size >= mPlayPosition)
                        setPlayTimer(list[mPlayPosition])
                }
            }
            is Boolean -> {
                if (mChannelList == null || mChannelList?.size ?: 0 <= 0) {
                    return
                }
                if (action) {
                    if (mPlayPosition > 0) {
                        mPlayPosition--
                    } else {
                        return
                    }
                    mChannelList?.let { list ->
                        if (list.size != 0 && list.size >= mPlayPosition)
                            setPlayTimer(list[mPlayPosition])
                    }
                } else {
                    if (mPlayPosition < mChannelList?.size?.minus(1) ?: 0) {
                        mPlayPosition++
                    } else {
                        return
                    }
                    mChannelList?.let { list ->
                        if (list.size != 0 && list.size >= mPlayPosition)
                            setPlayTimer(list[mPlayPosition])
                    }
                }
            }
        }
    }

    private fun setPlayTimer(channelInfo: TVChannel) {
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable?.dispose()
        }

        mDisposable = Observable.timer(400, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {}, { onError -> Log.e(TAG, "error:$onError") }, {
                    if (channelInfo.chType == "DVBT") {

//                        videoView_frame.visibility = View.GONE
//                        dvbView.visibility = View.VISIBLE
                        mExoPlayerHelper.stop()
//                        videoView_frame?.visibility = View.GONE
                        DVBHelper.getDVBPlayer().closePlayer()
                        DVBHelper.getDVBPlayer().initPlayer(832, 464, 108, 72)//940,536

                        Single.just(true)
                            .map {
                                DVBHelper.getDVBPlayer()
                                    .scanChannel("${channelInfo.chIp.frequency} ${channelInfo.chIp.bandwidth}")
                            }
                            .map { DVBHelper.getDVBPlayer().playChannel(channelInfo.chIp.dvbParameter) }
                            .subscribeOn(Schedulers.io())
                            .subscribe()

                    } else {

//                        dvbView.visibility = View.GONE
//                        videoView_frame.visibility = View.VISIBLE
                        DVBHelper.getDVBPlayer().closePlayer()
//                        videoView_frame?.visibility = View.VISIBLE
                        mExoPlayerHelper.setMp4Source(R.raw.videoplayback, true)
                        mExoPlayerHelper.play()
                    }

                })
    }
}