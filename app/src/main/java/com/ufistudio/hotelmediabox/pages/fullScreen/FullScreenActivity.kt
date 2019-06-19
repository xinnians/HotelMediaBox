package com.ufistudio.hotelmediabox.pages.fullScreen

import android.graphics.PixelFormat
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import com.ufistudio.hotelmediabox.pages.factory.FactoryActivity
import com.ufistudio.hotelmediabox.repository.data.TVChannel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import android.content.Intent
import android.view.*
import kotlinx.android.synthetic.main.activity_fullscreen.*
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.helper.TVController
import com.ufistudio.hotelmediabox.pages.MainActivity
import com.ufistudio.hotelmediabox.pages.base.PaneViewActivity
import com.ufistudio.hotelmediabox.repository.data.TVType
import com.ufistudio.hotelmediabox.utils.FileUtils
import kotlinx.android.synthetic.main.view_bottom_fullscreen.*


class FullScreenActivity : PaneViewActivity() {

    private lateinit var mViewModel: FullScreenViewModel

    private var mChannelList: ArrayList<TVChannel>? = null
    private var mPlayPosition = 0
    private var mDisposable: Disposable? = null
    private var mDisposableInfoView: Disposable? = null

    private lateinit var mExoPlayerHelper: ExoPlayerHelper

    private var mTVChannel: TVChannel? = null

    private var mTVListener:TVController.OnTVListener = object :TVController.OnTVListener{
        override fun onScanFinish() {

        }

        override fun onChannelChange(tvChannel: TVChannel?) {
            tvChannel?.let { currentChannel ->
                if(currentChannel.chType == TVType.IPTV.name){
                    videoView.visibility = View.VISIBLE
                    mExoPlayerHelper.setSource(tvChannel.chIp.uri, true)
                    mExoPlayerHelper.play()
                }else{
                    mExoPlayerHelper.stop()
                    videoView.visibility = View.INVISIBLE
                }
            }
        }

        override fun initDeviceFinish() {
        }

        override fun initAVPlayerFinish() {
            TVController.playCurrent()
            mTVChannel = TVController.getCurrentChannel()
            textChannelName?.text = "CH${mTVChannel?.chNum} ${mTVChannel?.chName}"
            viewLogo?.let { viewLogo ->
                Glide.with(applicationContext)
                    .load(FileUtils.getFileFromStorage(mTVChannel?.chLogo?.normalIconName ?: ""))
                    .skipMemoryCache(true)
                    .into(viewLogo)
            }
            viewMainLogo?.let { viewLogo ->
                Glide.with(applicationContext)
                    .load(FileUtils.getFileFromStorage(mTVChannel?.chLogo?.bigIconName ?: ""))
                    .skipMemoryCache(true)
                    .into(viewLogo)
            }
            showInfo()
        }

    }

    companion object {
        private val TAG = FullScreenActivity::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)

        mViewModel = AppInjector.obtainViewModel(this)

        mExoPlayerHelper = ExoPlayerHelper()

    }

    override fun onStart() {
        super.onStart()

//        mViewModel.initChannels()
        dvbView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                holder?.setFormat(PixelFormat.TRANSPARENT)
            }
        })

        mExoPlayerHelper.initPlayer(application, videoView)
    }

    override fun onResume() {
        super.onResume()
//        mViewModel.getTVHelper().initAVPlayer(TVHelper.SCREEN_TYPE.FULLSCREEN)
//        mViewModel.getTVHelper().playCurrent()?.observeOn(AndroidSchedulers.mainThread())?.subscribe({
//
//            mViewModel.getTVHelper().getCurrentChannel()?.let { tvChannel ->
//                textChannelName?.text = tvChannel.chNum + " " + tvChannel.chName
//                viewLogo?.let { viewLogo ->
//                    Glide.with(this)
//                            .load(FileUtils.getFileFromStorage(tvChannel.chLogo.fileName))
//                            .skipMemoryCache(true)
//                            .into(viewLogo)
//                }
//                viewMainLogo?.let { viewLogo ->
//                    Glide.with(this)
//                            .load(FileUtils.getFileFromStorage(tvChannel.chLogo.fileName))
//                            .skipMemoryCache(true)
//                            .into(viewLogo)
//                }
//                showInfo()
//
//            }
//
//        }, {})

        TVController.registerListener(mTVListener)
        TVController.initAVPlayer(TVController.SCREEN_TYPE.FULLSCREEN)
    }

    override fun onPause() {
        super.onPause()
//        mViewModel.getTVHelper().closeAVPlayer()
        TVController.releaseListener(mTVListener)
        TVController.deInitAVPlayer()

    }

    override fun onStop() {
        super.onStop()
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable?.dispose()
        }
        mExoPlayerHelper.release()
        dateView?.stopRefreshTimer()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_CHANNEL_UP -> {
//                playTv(false)

//                mTVChannel = mViewModel.getTVHelper().chooseUp()
                mTVChannel = TVController.chooseUp()
                textChannelName?.text = "CH${mTVChannel?.chNum} ${mTVChannel?.chName}"
                viewLogo?.let { viewLogo ->
                    Glide.with(this)
                        .load(FileUtils.getFileFromStorage(mTVChannel?.chLogo?.normalIconName ?: ""))
                        .skipMemoryCache(true)
                        .into(viewLogo)
                }
                viewMainLogo?.let { viewLogo ->
                    Glide.with(this)
                        .load(FileUtils.getFileFromStorage(mTVChannel?.chLogo?.bigIconName ?: ""))
                        .skipMemoryCache(true)
                        .into(viewLogo)
                }
                setPlayTimer()
                showInfo()

//                mViewModel.getTVHelper().playUp()?.observeOn(AndroidSchedulers.mainThread())?.subscribe({
//
//                    mViewModel.getTVHelper().getCurrentChannel()?.let { tvChannel ->
//                        textChannelName?.text = tvChannel.chNum + " " + tvChannel.chName
//                        viewLogo?.let { viewLogo ->
//                            Glide.with(this)
//                                .load(FileUtils.getFileFromStorage(tvChannel.chLogo.fileName))
//                                .into(viewLogo)
//                        }
//
//                    }
//
//                }, {})
                return true
            }
            KeyEvent.KEYCODE_CHANNEL_DOWN -> {
//                playTv(true)

//                mTVChannel = mViewModel.getTVHelper().chooseDown()
                mTVChannel = TVController.chooseDown()
                textChannelName?.text = "CH${mTVChannel?.chNum} ${mTVChannel?.chName}"
                viewLogo?.let { viewLogo ->
                    Glide.with(this)
                        .load(FileUtils.getFileFromStorage(mTVChannel?.chLogo?.normalIconName ?: ""))
                        .skipMemoryCache(true)
                        .into(viewLogo)
                }
                viewMainLogo?.let { viewLogo ->
                    Glide.with(this)
                        .load(FileUtils.getFileFromStorage(mTVChannel?.chLogo?.bigIconName ?: ""))
                        .skipMemoryCache(true)
                        .into(viewLogo)
                }
                setPlayTimer()
                showInfo()

//                mViewModel.getTVHelper().playDown()?.observeOn(AndroidSchedulers.mainThread())?.subscribe({
//
//                    mViewModel.getTVHelper().getCurrentChannel()?.let { tvChannel ->
//                        textChannelName?.text = tvChannel.chNum + " " + tvChannel.chName
//                        viewLogo?.let { viewLogo ->
//                            Glide.with(this)
//                                .load(FileUtils.getFileFromStorage(tvChannel.chLogo.fileName))
//                                .into(viewLogo)
//                        }
//
//                    }
//
//                }, {})
                return true
            }
            KeyEvent.KEYCODE_INFO ->{
                showInfo()
            }
            KeyEvent.KEYCODE_BACK -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                val intent: Intent = Intent(this, MainActivity::class.java)
                val bundle: Bundle = Bundle()
                bundle.putBoolean(Page.ARG_BUNDLE, true)
                bundle.putInt(Page.ARG_PAGE,Page.CHANNEL)
                intent.putExtras(bundle)
                startActivity(intent)
//                startActivity(Intent(this, MainActivity::class.java))
                finish()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun setPlayTimer() {
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable?.dispose()
        }

        mDisposable = Observable.timer(400, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, { onError -> Log.e(TAG, "error:$onError") }, {
                //                    mViewModel.getTVHelper().playCurrent()?.subscribe()
                TVController.playCurrent()
            })
    }

    private fun showInfo() {
        Log.e(TAG,"[showInfo] call.")
        banner.visibility = View.VISIBLE
        dateView.visibility = View.VISIBLE

        if (mDisposableInfoView != null && !mDisposableInfoView!!.isDisposed) {
            mDisposableInfoView?.dispose()
        }

        mDisposableInfoView = Observable.timer(5, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { Log.e(TAG, " showInfo continus : $it") },
                { onError -> Log.e(TAG, "error:$onError") },
                {
                    Log.e(TAG, " showInfo finish")
                    banner.visibility = View.INVISIBLE
                    dateView.visibility = View.INVISIBLE
                })
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(TAG, "keycode = $keyCode  ,event = $event")
        if (keyCode == 302) {
//            var intent: Intent = Intent(this, FullScreenActivity::class.java)
//            intent.putExtra("page",Page.HOME)
            startActivity(Intent(this, MainActivity::class.java))
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

}