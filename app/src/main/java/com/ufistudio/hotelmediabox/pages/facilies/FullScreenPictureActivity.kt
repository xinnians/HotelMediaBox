package com.ufistudio.hotelmediabox.pages.facilies

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.Player.STATE_ENDED
import com.google.android.exoplayer2.Player.STATE_IDLE
import com.google.android.exoplayer2.util.Log
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Cache
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import com.ufistudio.hotelmediabox.pages.base.PaneViewActivity
import com.ufistudio.hotelmediabox.utils.FileUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_full_screen_picture.*
import java.util.concurrent.TimeUnit

class FullScreenPictureActivity : PaneViewActivity() {

    private var mExoPlayerHelper: ExoPlayerHelper = ExoPlayerHelper()

    private var isHotelFacilities: Boolean = true
    private var mFocusPosition: Int = 0
    private var mDateDisposable: Disposable? = null
    private var mShowHintDisposable: Disposable? = null
    private var isAUTOPlayOn = false
    private var mCurrentItemWaitTime: Int = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_picture)

    }

    companion object {
        private val TAG = FullScreenPictureActivity::class.java.simpleName
        const val TAG_TYPE = "type"
        const val TAG_AUTOPLAY_ON = "tag_autoplay_on"
        const val DEFAULT_WAIT_TIME = 1L
    }

    override fun onStart() {
        super.onStart()

        if (TextUtils.equals(intent.getStringExtra(TAG_TYPE), "HotelFacilities")) {
            isHotelFacilities = true
            mFocusPosition = intent.getIntExtra(Page.ARG_BUNDLE, 0)
            renderView(mFocusPosition)
            showHint()
            val needSwitchToAutoPlay = intent.getBooleanExtra(TAG_AUTOPLAY_ON,false)
            if(needSwitchToAutoPlay) switchAutoPlay()
        } else {
            isHotelFacilities = false
            mExoPlayerHelper.initPlayer(applicationContext, videoView_full_screen)
            mExoPlayerHelper.setSource(intent.getStringExtra(Page.ARG_BUNDLE))
            mExoPlayerHelper.repeatMode()
            videoView_full_screen?.visibility = View.VISIBLE
        }


//        if (TextUtils.equals(intent.getStringExtra(TAG_TYPE), "image")) {
//            Glide.with(applicationContext)
//                    .load(FileUtils.getFileFromStorage(intent.getStringExtra(Page.ARG_BUNDLE)))
//                    .skipMemoryCache(true)
//                    .into(imageView_full_screen)
//            imageView_full_screen.visibility = View.VISIBLE
//        } else if (TextUtils.equals(intent.getStringExtra(TAG_TYPE), "udp")) {
//            mExoPlayerHelper.initPlayer(applicationContext, videoView_full_screen)
//            mExoPlayerHelper.setSource(intent.getStringExtra(Page.ARG_BUNDLE))
//            mExoPlayerHelper.repeatMode()
//            videoView_full_screen?.visibility = View.VISIBLE
//        } else {
//            mExoPlayerHelper.initPlayer(applicationContext, videoView_full_screen)
//            mExoPlayerHelper.setFileSource(Uri.parse(FileUtils.getFileFromStorage(intent.getStringExtra(Page.ARG_BUNDLE))?.absolutePath ?: ""))
//            mExoPlayerHelper.repeatMode()
//            videoView_full_screen?.visibility = View.VISIBLE
//        }
    }

    override fun onPause() {
        mExoPlayerHelper.stop()
        mExoPlayerHelper.release()
        if (mDateDisposable != null && mDateDisposable?.isDisposed == false) {
            mDateDisposable?.dispose()
        }
        closeShowHintDisposable()
        super.onPause()
    }

    private fun closeShowHintDisposable() {
        if (mShowHintDisposable != null && mShowHintDisposable?.isDisposed == false) {
            mShowHintDisposable?.dispose()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                mExoPlayerHelper.release()
                finish()
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if(isHotelFacilities){
                    showHint()
                    Cache.HotelFacilitiesContents?.let {
                        if (mFocusPosition + 1 <= it.size - 1) {
                            mFocusPosition += 1
                        } else {
                            mFocusPosition = 0
                        }
                        renderView(mFocusPosition)
                    }

                }
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if(isHotelFacilities){
                    showHint()
                    Cache.HotelFacilitiesContents?.let {
                        if (mFocusPosition - 1 >= 0) {
                            mFocusPosition -= 1
                            renderView(mFocusPosition)
                        }
                    }
                }
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                if(isHotelFacilities){
                    showHint()
                    switchAutoPlay()
                }
            }
            else -> {

            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun renderView(position: Int) {
        videoView_full_screen?.visibility = View.INVISIBLE
        imageView_full_screen.visibility = View.INVISIBLE
        mExoPlayerHelper.stop()
        Cache.HotelFacilitiesContents?.get(position)?.let {
            if (it.file_type == "image") {
                mCurrentItemWaitTime = it.wait_time
                Glide.with(applicationContext)
                    .load(FileUtils.getFileFromStorage(it.file_name))
                    .skipMemoryCache(true)
                    .into(imageView_full_screen)
                imageView_full_screen.visibility = View.VISIBLE
            } else {
                mExoPlayerHelper.initPlayer(applicationContext, videoView_full_screen)
                mExoPlayerHelper.setFileSource(
                    Uri.parse(
                        FileUtils.getFileFromStorage(it.file_name)?.absolutePath ?: ""
                    )
                )
                if (isAUTOPlayOn) {
                    mExoPlayerHelper.singleMode()
                }else{
                    mExoPlayerHelper.repeatMode()
                }
                videoView_full_screen?.visibility = View.VISIBLE
            }
        }
    }

    private fun switchAutoPlay() {

        isAUTOPlayOn = !isAUTOPlayOn

        Log.e(TAG, "[switchAutoPlay] isAUTOPlayOn:$isAUTOPlayOn")

        if (mDateDisposable != null && mDateDisposable?.isDisposed == false) {
            mDateDisposable?.dispose()
        }

        if (isAUTOPlayOn) {
            mExoPlayerHelper.singleMode()
            mDateDisposable = Observable.interval(DEFAULT_WAIT_TIME, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.e(TAG, "[switchAutoPlay] mFocusPosition:$mFocusPosition")
                    Cache.HotelFacilitiesContents?.get(mFocusPosition)?.let {
                        if (it.file_type == "image") {
                            if(mCurrentItemWaitTime != 0){
                                mCurrentItemWaitTime--
                            }else{
                                onKeyDown(
                                    KeyEvent.KEYCODE_DPAD_RIGHT,
                                    KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT)
                                )
                            }
                        } else {
                            videoView_full_screen?.player?.playbackState.let { player ->
                                Log.e(TAG, "[switchAutoPlay] playbackState:$player")
                                if (player == STATE_ENDED || player == STATE_IDLE) {
                                    onKeyDown(
                                        KeyEvent.KEYCODE_DPAD_RIGHT,
                                        KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT)
                                    )
                                }
                            }
                        }
                    }
                }
        } else {
            mExoPlayerHelper.repeatMode()
        }
    }

    private fun showHint(){

        closeShowHintDisposable()

        imageView_arrow_left.visibility = View.VISIBLE
        imageView_arrow_right.visibility = View.VISIBLE

        mShowHintDisposable = Observable.timer(5000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, { onError -> Log.e(TAG, "error:$onError") }, {
                imageView_arrow_left.visibility = View.INVISIBLE
                imageView_arrow_right.visibility = View.INVISIBLE
            })
    }

}
