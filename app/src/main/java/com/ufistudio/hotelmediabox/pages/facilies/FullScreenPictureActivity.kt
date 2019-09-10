package com.ufistudio.hotelmediabox.pages.facilies

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Cache
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import com.ufistudio.hotelmediabox.pages.base.PaneViewActivity
import com.ufistudio.hotelmediabox.utils.FileUtils
import kotlinx.android.synthetic.main.activity_full_screen_picture.*

class FullScreenPictureActivity : PaneViewActivity() {

    private var mExoPlayerHelper: ExoPlayerHelper = ExoPlayerHelper()

    private var isHotelFacilities: Boolean = true
    private var mFocusPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_picture)

    }

    companion object {
        const val TAG_TYPE = "type"
    }

    override fun onStart() {
        super.onStart()

        if (TextUtils.equals(intent.getStringExtra(TAG_TYPE), "HotelFacilities")) {
            isHotelFacilities = true
            mFocusPosition = intent.getIntExtra(Page.ARG_BUNDLE, 0)
            renderView(mFocusPosition)
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
        super.onPause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                mExoPlayerHelper.release()
                finish()
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {

                Cache.HotelFacilitiesContents?.let {
                    if (mFocusPosition + 1 <= it.size - 1) {
                        mFocusPosition += 1
                        renderView(mFocusPosition)
                    }
                }

            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                Cache.HotelFacilitiesContents?.let {
                    if (mFocusPosition - 1 >= 0) {
                        mFocusPosition -= 1
                        renderView(mFocusPosition)
                    }
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
                mExoPlayerHelper.repeatMode()
                videoView_full_screen?.visibility = View.VISIBLE
            }
        }
    }

}
