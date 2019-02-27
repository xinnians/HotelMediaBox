package com.ufistudio.hotelmediabox.pages

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import kotlinx.android.synthetic.main.activity_fullscreen_video.*

class FullScreenActivity : AppCompatActivity() {
    private lateinit var mExoPlayerHelper: ExoPlayerHelper

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_video)
        mExoPlayerHelper = ExoPlayerHelper()
    }

    override fun onStart() {
        super.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mExoPlayerHelper.initPlayer(applicationContext, player_view)
        mExoPlayerHelper.setMp4Source(R.raw.videoplayback)
        player_view.setOnClickListener { mExoPlayerHelper.changeFullScreenInfo() }
    }

    public override fun onPause() {
        super.onPause()
        mExoPlayerHelper.release()
    }
}