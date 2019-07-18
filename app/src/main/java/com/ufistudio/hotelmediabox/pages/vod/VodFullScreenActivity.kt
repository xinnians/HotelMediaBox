package com.ufistudio.hotelmediabox.pages.vod

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import com.ufistudio.hotelmediabox.pages.base.PaneViewActivity
import com.ufistudio.hotelmediabox.repository.data.NoteButton
import kotlinx.android.synthetic.main.activity_vod_full_screen.*

class VodFullScreenActivity : PaneViewActivity() {

    private var mExoPlayerHelper: ExoPlayerHelper? = null

    private var mMediaURL: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vod_full_screen)

        val b: NoteButton = intent.extras.get("bottom_note") as NoteButton
        mMediaURL = intent.extras.get("media_url") as String
        Log.d("neo", "${b.note?.home}")

    }

    override fun onStart() {
        super.onStart()
        initPlayer()
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
    }

    private fun initPlayer(){
        if(mExoPlayerHelper == null){
            mExoPlayerHelper = ExoPlayerHelper()
        }
        mExoPlayerHelper?.initPlayer(application, player_view)
    }
}
