package com.ufistudio.hotelmediabox.pages.facilies

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import com.ufistudio.hotelmediabox.utils.FileUtils
import kotlinx.android.synthetic.main.activity_full_screen_picture.*

class FullScreenPictureActivity : AppCompatActivity() {

    private var mExoPlayerHelper: ExoPlayerHelper = ExoPlayerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_picture)

    }

    companion object {
        const val TAG_TYPE = "type"
    }

    override fun onStart() {
        super.onStart()

        if (TextUtils.equals(intent.getStringExtra(TAG_TYPE), "image")) {
            Glide.with(applicationContext)
                    .load(FileUtils.getFileFromStorage(intent.getStringExtra(Page.ARG_BUNDLE)))
                    .skipMemoryCache(true)
                    .into(imageView_full_screen)
            imageView_full_screen.visibility = View.VISIBLE
        } else if (TextUtils.equals(intent.getStringExtra(TAG_TYPE), "udp")) {
            mExoPlayerHelper.initPlayer(applicationContext, videoView_full_screen)
            mExoPlayerHelper.setUdpSource(intent.getStringExtra(Page.ARG_BUNDLE))
            mExoPlayerHelper.repeatMode()
            videoView_full_screen?.visibility = View.VISIBLE
        } else {
            mExoPlayerHelper.initPlayer(applicationContext, videoView_full_screen)
            mExoPlayerHelper.setFileSource(Uri.parse(FileUtils.getFileFromStorage(intent.getStringExtra(Page.ARG_BUNDLE))?.absolutePath ?: ""))
            mExoPlayerHelper.repeatMode()
            videoView_full_screen?.visibility = View.VISIBLE
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mExoPlayerHelper.release()
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

}
