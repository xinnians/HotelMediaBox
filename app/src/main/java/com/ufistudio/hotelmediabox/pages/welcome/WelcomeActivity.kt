package com.ufistudio.hotelmediabox.pages.welcome

import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.interfaces.ViewModelsCallback
import com.ufistudio.hotelmediabox.pages.MainActivity
import com.ufistudio.hotelmediabox.pages.base.PaneViewActivity
import com.ufistudio.hotelmediabox.repository.data.InitialData
import com.ufistudio.hotelmediabox.repository.data.Welcome
import com.ufistudio.hotelmediabox.repository.data.WelcomeContent
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.utils.MiscUtils
import kotlinx.android.synthetic.main.activity_welcome.*


class WelcomeActivity : PaneViewActivity(), ViewModelsCallback, View.OnClickListener {
    private lateinit var mViewModel: WelcomeViewModel
    private var mWelcomeContent: WelcomeContent? = null
    private var mPlayer: MediaPlayer? = null

    private var mSpecialCount: Int = 0

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

        mViewModel.getInitialDataProgress.observe(this, Observer { onProgress() })
        mViewModel.getInitialDataSuccess.observe(this, Observer { onSuccess(it) })
        mViewModel.getInitialDataError.observe(this, Observer { onError(it) })
    }

    override fun onStart() {
        super.onStart()
        renderUI()
        mViewModel.getInitialDataFromServer()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        mPlayer?.stop()
        mPlayer?.release()
        dateView.stopRefreshTimer()
        super.onStop()
    }

    private fun showGoToSetting() {
        AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("open permission")
                .setPositiveButton(android.R.string.ok) { dialog, which -> MiscUtils.openSetting(baseContext) }
                .show()
    }

    private fun renderUI() {
        button_ok.background = ContextCompat.getDrawable(this, R.drawable.selector_home_icon)
        button_ok.setOnClickListener(this)
        button_ok.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                button_ok.setTextColor(ContextCompat.getColor(this, R.color.colorYellow))
            } else {
                button_ok.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
        }

        button_ok.isFocusable = true
        button_ok.isFocusableInTouchMode = true
        button_ok.requestFocus()
    }

    override fun onSuccess(it: Any?) {

        if (it != null && it is InitialData) {
            text_name.text = it.guestName
            SystemClock.setCurrentTimeMillis(it.timestamp)
            return
        }

        if (it != null && it is Welcome) {
            mWelcomeContent = it.welcome

            mWelcomeContent.let {
                Glide.with(this)
                        .load(FileUtils.getFileFromStorage(it?.titleImage!!))
                        .skipMemoryCache(true)
                        .into(imageView_title)

                view_frame.background =
                        Drawable.createFromPath(FileUtils.getFileFromStorage(it.background)?.absolutePath)

                button_ok.text = it.entryButton
                text_title.text = it.title
                text_room.text = it.room
                text_description.text = it.description

                val file = FileUtils.getFileFromStorage(it.music)
                if (file != null) {
                    mPlayer = MediaPlayer.create(this, Uri.fromFile(file))
                    mPlayer?.start()
                    mPlayer?.isLooping = true
                }
            }
        } else {
            onError(Throwable("OnSuccess response is null"))
        }
    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "onError = ${t?.message}")
        AlertDialog.Builder(this)
                .setTitle(R.string.dialog_error_title)
                .setMessage(R.string.dialog_cannot_find_file)
                .setPositiveButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }
                .create()
                .show()
//        startActivity(Intent(this, FactoryActivity::class.java))
//        finish()
    }

    override fun onProgress(b: Boolean) {
    }

    override fun onClick(v: View?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        return
    }
}