package com.ufistudio.hotelmediabox.pages.fullScreen

import android.arch.lifecycle.Observer
import android.graphics.PixelFormat
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.DVBHelper
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import com.ufistudio.hotelmediabox.pages.factory.FactoryActivity
import com.ufistudio.hotelmediabox.pages.factory.FactoryViewModel
import com.ufistudio.hotelmediabox.pages.home.HomeFragment
import com.ufistudio.hotelmediabox.repository.data.TVChannel
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import android.content.Context.WINDOW_SERVICE
import android.content.Context
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import kotlinx.android.synthetic.main.activity_fullscreen.*
import android.view.WindowManager


class FullScreenActivity : AppCompatActivity() {

    private lateinit var mViewModel: FullScreenViewModel

    private var mChannelList: ArrayList<TVChannel>? = null
    private var mPlayPosition = 0
    private var mDisposable: Disposable? = null
    private var mDisposableInfoView: Disposable? = null

    private lateinit var mExoPlayerHelper: ExoPlayerHelper

    companion object {
        private val TAG = FactoryActivity::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)

        mViewModel = AppInjector.obtainViewModel(this)

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

        mExoPlayerHelper.initPlayer(this, videoView)

    }

    override fun onStop() {
        super.onStop()
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable?.dispose()
        }
        DVBHelper.getDVBPlayer().closePlayer()
        mExoPlayerHelper.release()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_CHANNEL_UP -> {
                playTv(false)

                return true
            }
            KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                playTv(true)
                return true
            }
//            KeyEvent.KEYCODE_BACK -> {
////                this.onBackPressed()
//                finish()
//            }
        }
        return super.onKeyDown(keyCode, event)
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
                        videoView_frame.visibility = View.GONE
                        DVBHelper.getDVBPlayer().closePlayer()
                        DVBHelper.getDVBPlayer().initPlayer(0, 0, 0, 0)//940,536

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
                        videoView_frame.visibility = View.VISIBLE
                        mExoPlayerHelper.setMp4Source(R.raw.videoplayback, true)
                        mExoPlayerHelper.play()
                    }

                })
    }

    private fun showInfo(channelInfo: TVChannel) {
        if (mDisposableInfoView != null && !mDisposableInfoView!!.isDisposed) {
            mDisposableInfoView?.dispose()
        }

        mDisposableInfoView = Observable.timer(400, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _ ->
                }, { onError -> Log.e(TAG, "error:$onError") }, {
                    if (channelInfo.chType == "DVBT") {

//                        videoView_frame.visibility = View.GONE
//                        dvbView.visibility = View.VISIBLE
                        mExoPlayerHelper.stop()
                        videoView_frame.visibility = View.GONE
                        DVBHelper.getDVBPlayer().closePlayer()
                        DVBHelper.getDVBPlayer().initPlayer(0, 0, 0, 0)//940,536

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
                        videoView_frame.visibility = View.VISIBLE
                        mExoPlayerHelper.setMp4Source(R.raw.videoplayback, true)
                        mExoPlayerHelper.play()
                    }

                })
    }

}