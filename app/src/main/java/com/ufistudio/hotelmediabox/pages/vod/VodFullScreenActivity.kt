package com.ufistudio.hotelmediabox.pages.vod

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Cache
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import com.ufistudio.hotelmediabox.helper.TVController
import com.ufistudio.hotelmediabox.pages.base.PaneViewActivity
import com.ufistudio.hotelmediabox.repository.data.NoteButton
import com.ufistudio.hotelmediabox.repository.data.TVChannel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_vod_full_screen.*
import kotlinx.android.synthetic.main.view_bottom_back_home.*
import kotlinx.android.synthetic.main.view_vod_full_screen_bottom_note.*
import java.util.concurrent.TimeUnit

class VodFullScreenActivity : PaneViewActivity() {

    private var mExoPlayerHelper: ExoPlayerHelper? = null

    private var mDisposableInfoView: Disposable? = null

    private var mMediaURL: String? = ""
    private var mMediaTitle: String? = ""
    private var mIsPause: Boolean = false

    private var mIsResumeViewShow: Boolean = false
    private var mIsResumeButtonFocus: Boolean = true

    private var mTVListener: TVController.OnTVListener = object : TVController.OnTVListener{
        override fun onIPTVFinish() {
            finish()
        }

        override fun onChannelChange(tvChannel: TVChannel?) {}
        override fun initDeviceFinish() {}
        override fun initAVPlayerFinish() {}
        override fun onScanFinish() {}
        override fun onIPTVLoading() {}
        override fun onIPTVPlaying() {}

    }

    companion object {
        private val TAG = this.javaClass.simpleName
        const val KEY_VOD_TITLE = "key_vod_title"
        const val KEY_VOD_URL = "key_vod_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vod_full_screen)

        val b: NoteButton = intent.extras.get("bottom_note") as NoteButton
        mMediaURL = intent.extras.get(KEY_VOD_URL) as String
        mMediaTitle = intent.extras.get(KEY_VOD_TITLE) as String
        Log.d("neo", "${b.note?.home}")
    }

    override fun onStart() {
        super.onStart()
        initPlayer()
        initTitle()
        initBottomText()
    }

    override fun onResume() {
        super.onResume()
//        checkWatchHistory()
        TVController.registerListener(mTVListener)
        if(Cache.VodWatchHistory[mMediaURL?:""] != null){
            layout_resume.visibility = View.VISIBLE
            mIsResumeViewShow = true

            mMediaURL?.let {mExoPlayerHelper?.setSource(it, false,true,Cache.VodWatchHistory[mMediaURL?:""]?:0L)}

            //hide controller
            constraintLayout4.visibility = View.INVISIBLE
            dateView.visibility = View.INVISIBLE
            player_view.controllerAutoShow = false
            player_view.hideController()
        }else{
            mIsResumeViewShow = false

            mMediaURL?.let {
                mExoPlayerHelper?.setSource(it, true) }
            showInfo()
        }

//        mExoPlayerHelper?.seekTo(20000L)
    }

    override fun onPause() {
        super.onPause()
//        Log.e(TAG,"[onPause] ${mExoPlayerHelper?.currentPosition()}/${mExoPlayerHelper?.totalContentDuration()}")
        saveWatchPosition()
        mExoPlayerHelper?.stop()
        TVController.releaseListener(mTVListener)
    }

    override fun onStop() {
        super.onStop()
        mExoPlayerHelper?.release()
        dateView?.stopRefreshTimer()
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        Log.e(TAG,"[dispatchKeyEvent] KeyEvent: $event")

        if(mIsResumeViewShow){
            event?.keyCode?.let {keycode ->
                when(keycode){
                    KeyEvent.KEYCODE_DPAD_LEFT ->{

                        if(event.action == KeyEvent.ACTION_UP){
                            return true
                        }

                        mIsResumeButtonFocus = true
                        tv_resumePlay.setTextColor(ContextCompat.getColor(tv_resumePlay.context, R.color.colorBlack))
                        tv_resumePlay.setBackgroundResource(R.drawable.bg_vod_btn_focus)
                        tv_replayMovie.setTextColor(ContextCompat.getColor(tv_replayMovie.context, R.color.colorWhite))
                        tv_replayMovie.setBackgroundResource(R.drawable.bg_vod_btn)

                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT ->{
                        if(event.action == KeyEvent.ACTION_UP){
                            return true
                        }

                        mIsResumeButtonFocus = false
                        tv_resumePlay.setTextColor(ContextCompat.getColor(tv_resumePlay.context, R.color.colorWhite))
                        tv_resumePlay.setBackgroundResource(R.drawable.bg_vod_btn)
                        tv_replayMovie.setTextColor(ContextCompat.getColor(tv_replayMovie.context, R.color.colorBlack))
                        tv_replayMovie.setBackgroundResource(R.drawable.bg_vod_btn_focus)

                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_CENTER ->{
                        if(event.action == KeyEvent.ACTION_UP){
                            return true
                        }

                        layout_resume.visibility = View.INVISIBLE
                        mIsResumeViewShow = false

                        if(mIsResumeButtonFocus){
                            mExoPlayerHelper?.play()
//                            mMediaURL?.let {mExoPlayerHelper?.setSource(it, true,true,Cache.VodWatchHistory[mMediaURL?:""]?:0L) }
                        }else{
                            mMediaURL?.let {mExoPlayerHelper?.setSource(it, true) }
                        }

                        showInfo()

                        return true
                    }
                    else ->{

                    }
                }
            }
        }else{
            showInfo()
            event?.keyCode?.let {keycode ->
                when(keycode){
                    KeyEvent.KEYCODE_MEDIA_STOP ->{
                        if(event.action == KeyEvent.ACTION_UP){
                            return true
                        }
                        onBackPressed()
                    }
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ->{

                        if(event.action == KeyEvent.ACTION_UP){
                            return true
                        }

                        if(mIsPause){
                            iv_pause.visibility = View.VISIBLE
                            mExoPlayerHelper?.pause()
                        }else{
                            iv_pause.visibility = View.INVISIBLE
                            mExoPlayerHelper?.play()
                        }
                        mIsPause = !mIsPause
                        return true
                    }
                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD ->{
                        if(event.action == KeyEvent.ACTION_UP){
                            return true
                        }
                        Toast.makeText(applicationContext,"Speed ${mExoPlayerHelper?.speedUp()}x",Toast.LENGTH_SHORT).show()
                        return true
                    }
                    else ->{

                    }
                }
            }
        }

        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.e(TAG,"[onKeyDown] keycode: $keyCode, KeyEvent: $event")
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_STOP ->{
                mExoPlayerHelper?.stop()
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ->{
                mExoPlayerHelper?.getPositionInfo()
                if(mIsPause){
                    iv_pause.visibility = View.VISIBLE
                    mExoPlayerHelper?.pause()
                }else{
                    iv_pause.visibility = View.INVISIBLE
                    mExoPlayerHelper?.play()
                }
                mIsPause = !mIsPause
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun initTitle(){
        Log.e(TAG,"[initTitle] mMediaTitle : $mMediaTitle")
        textView_subtitle.text = mMediaTitle
    }

    private fun initBottomText(){
        textView_home.text = "Home"
        textView_back.text = "Back"
        textView_stop.text = "Stop"
        textView_fast_forward.text = "Fast Forward"
        textView_watch_movie.text = "Play/Pause"
        textView_rewind.text ="Rewind"

    }

    private fun initPlayer(){
        if(mExoPlayerHelper == null){
            mExoPlayerHelper = ExoPlayerHelper()
        }
        mExoPlayerHelper?.initPlayer(application, player_view)
    }

    private fun showInfo() {
        dateView.visibility = View.VISIBLE
        constraintLayout4.visibility = View.VISIBLE
        player_view.showController()

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
                    constraintLayout4.visibility = View.INVISIBLE
                    dateView.visibility = View.INVISIBLE
                    player_view.hideController()
                })
    }

    private fun saveWatchPosition(){
        Log.e(TAG,"[saveWatchPosition] ${mExoPlayerHelper?.currentPosition()}/${mExoPlayerHelper?.totalContentDuration()}")
        Cache.VodWatchHistory[mMediaURL?:""] = mExoPlayerHelper?.currentPosition()?:0L
    }

    private fun checkWatchHistory(){
        Log.e(TAG,"[checkWatchHistory] Cache.VodWatchHistory[mMediaURL] ${Cache.VodWatchHistory[mMediaURL?:""]}")
        layout_resume.visibility = View.VISIBLE
    }
}
