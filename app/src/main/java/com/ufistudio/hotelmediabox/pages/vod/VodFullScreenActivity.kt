package com.ufistudio.hotelmediabox.pages.vod

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import com.ufistudio.hotelmediabox.pages.base.PaneViewActivity
import com.ufistudio.hotelmediabox.repository.data.NoteButton
import kotlinx.android.synthetic.main.activity_vod_full_screen.*
import kotlinx.android.synthetic.main.view_bottom_back_home.*
import kotlinx.android.synthetic.main.view_vod_full_screen_bottom_note.*

class VodFullScreenActivity : PaneViewActivity() {

    private var mExoPlayerHelper: ExoPlayerHelper? = null

    private var mMediaURL: String? = ""
    private var mMediaTitle: String? = ""
    private var mIsPause: Boolean = false

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
        mMediaURL?.let { mExoPlayerHelper?.setSource(it, true) }
    }

    override fun onPause() {
        super.onPause()
        mExoPlayerHelper?.stop()
    }

    override fun onStop() {
        super.onStop()
        mExoPlayerHelper?.release()
        dateView?.stopRefreshTimer()
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        Log.e(TAG,"[dispatchKeyEvent] KeyEvent: $event")

        event?.keyCode?.let {keycode ->
            when(keycode){
//                KeyEvent.KEYCODE_MEDIA_STOP ->{
//                    mExoPlayerHelper?.stop()
//                }
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
                else ->{

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
}
