package com.ufistudio.hotelmediabox.pages.welcome

import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.interfaces.ViewModelsCallback
import com.ufistudio.hotelmediabox.pages.MainActivity
import com.ufistudio.hotelmediabox.repository.data.Welcome
import com.ufistudio.hotelmediabox.repository.data.WelcomeContent
import com.ufistudio.hotelmediabox.utils.MiscUtils
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity(), ViewModelsCallback, View.OnClickListener {
    private lateinit var mViewModel: WelcomeViewModel
    private var mWelcomeContent: WelcomeContent? = null
    private lateinit var mPlayer: MediaPlayer

    companion object {
        private val TAG = WelcomeActivity::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initWelcomeProgress.observe(this, Observer { onProgress() })
        mViewModel.initWelcomeSuccess.observe(this, Observer { onSuccess(it) })
        mViewModel.initWelcomeError.observe(this, Observer { onError(it) })
    }

    override fun onStart() {
        super.onStart()

        button_ok.background = ContextCompat.getDrawable(this, R.drawable.selector_home_icon)
        button_ok.setOnClickListener(this)
        button_ok.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                button_ok.setTextColor(ContextCompat.getColor(this, R.color.colorYellow))
            } else {
                button_ok.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, MainActivity::class.java)
//        startActivity(intent)
    }

    override fun onStop() {
        mPlayer.stop()
        mPlayer.release()
        super.onStop()
    }

    override fun onSuccess(it: Any?) {
        mWelcomeContent = (it as Welcome).welcome


        mWelcomeContent.let {
            Glide.with(this)
                    .load(MiscUtils.getFileFromStorage("/image", it?.titleImage!!))
                    .into(imageView_title)

            view_frame.background = Drawable.createFromPath(MiscUtils.getFileFromStorage("/image", it.background)?.absolutePath)

            button_ok.text = it.entryButton
            text_name.text = it.name
            text_title.text = it.title
            text_room.text = it.room
            text_description.text = it.description

            mPlayer = MediaPlayer.create(this, Uri.fromFile(MiscUtils.getFileFromStorage("/other", it.music)))
            mPlayer.start()
        }

    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "onError = ${t?.message}")
    }

    override fun onProgress(b: Boolean) {
    }

    override fun onClick(v: View?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}