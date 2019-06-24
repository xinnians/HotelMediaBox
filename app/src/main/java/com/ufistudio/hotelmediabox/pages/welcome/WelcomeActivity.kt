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
import com.ufistudio.hotelmediabox.helper.TVController.updateJVersionAndAppVersionToFile
import com.ufistudio.hotelmediabox.interfaces.ViewModelsCallback
import com.ufistudio.hotelmediabox.pages.MainActivity
import com.ufistudio.hotelmediabox.pages.base.PaneViewActivity
import com.ufistudio.hotelmediabox.repository.data.InitialData
import com.ufistudio.hotelmediabox.repository.data.Welcome
import com.ufistudio.hotelmediabox.repository.data.WelcomeContent
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.utils.MiscUtils
import kotlinx.android.synthetic.main.activity_welcome.*
import android.app.AlarmManager
import android.content.Context
import com.ufistudio.hotelmediabox.repository.data.NoteButton
import kotlinx.android.synthetic.main.view_bottom_ok.*


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
        mViewModel.getInitialDataError.observe(this, Observer { Log.e(TAG, "Get Initial data Error : $it") })

        mViewModel.initNoteButtonProgress.observe(this, Observer { onProgress() })
        mViewModel.initNoteButtonSuccess.observe(this, Observer { onSuccess(it) })
        mViewModel.initNoteButtonError.observe(this, Observer { onError(it) })
    }

    override fun onStart() {
        super.onStart()
        renderUI()
        mViewModel.getInitialDataFromServer()
        updateJVersionAndAppVersionToFile(MiscUtils.getLocalVersionName(this) + "." + MiscUtils.getLocalVersion(this))
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
        mViewModel.initNoteButton()

        button_ok.background = ContextCompat.getDrawable(this, R.drawable.home_icon_frame_frame_focused)
        button_ok.setOnClickListener(this)
        button_ok.setTextColor(ContextCompat.getColor(this, R.color.colorYellow))

        button_ok.isFocusable = true
        button_ok.isFocusableInTouchMode = true
        button_ok.requestFocus()
        text_name.setOnClickListener(this)
    }

    override fun onSuccess(result: Any?) {

        result?.let {
            when (it) {
                is InitialData -> {
                    Log.e(TAG, "[InitialData] : $it")
                    text_name.text = if (it.guestName.isNullOrEmpty()) "Guest" else it.guestName
                    text_name?.requestFocus()
                    val mAlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    mAlarmManager.setTimeZone(it.timezone)
                    SystemClock.setCurrentTimeMillis(it.timestamp)
                }
                is Welcome -> {
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
                        text_description.text = it.description

                        val file = FileUtils.getFileFromStorage(it.music)
                        if (file != null) {
                            mPlayer = MediaPlayer.create(this, Uri.fromFile(file))
                            mPlayer?.start()
                            mPlayer?.isLooping = true
                        }
                    }
                }
                is NoteButton -> {
                    textView_ok.text = it.note?.next
                }
                else -> {
                    onError(Throwable("OnSuccess response is null"))
                }
            }
        }
    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "onError = ${t?.message}")
        FileUtils.getFileFromStorage("chkflag")?.delete()
        MiscUtils.reboot(baseContext)
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