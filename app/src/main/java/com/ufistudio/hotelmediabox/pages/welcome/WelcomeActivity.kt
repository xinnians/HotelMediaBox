package com.ufistudio.hotelmediabox.pages.welcome

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
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
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.utils.MiscUtils
import kotlinx.android.synthetic.main.activity_welcome.*

const val TAG_WRITE_PERMISSION_CODE = 10000

class WelcomeActivity : AppCompatActivity(), ViewModelsCallback, View.OnClickListener {
    private lateinit var mViewModel: WelcomeViewModel
    private var mWelcomeContent: WelcomeContent? = null
    private var mPlayer: MediaPlayer? = null

    companion object {
        private val TAG = WelcomeActivity::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showGoToSetting()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    TAG_WRITE_PERMISSION_CODE
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            renderUI()
        }
    }

    override fun onStop() {
        mPlayer?.stop()
        mPlayer?.release()
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
        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initWelcomeProgress.observe(this, Observer { onProgress() })
        mViewModel.initWelcomeSuccess.observe(this, Observer { onSuccess(it) })
        mViewModel.initWelcomeError.observe(this, Observer { onError(it) })

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            TAG_WRITE_PERMISSION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    renderUI()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showGoToSetting()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onSuccess(it: Any?) {
        mWelcomeContent = (it as Welcome).welcome


        mWelcomeContent.let {
            Glide.with(this)
                .load(FileUtils.getFileFromStorage(it?.titleImage!!))
                .into(imageView_title)

            view_frame.background =
                Drawable.createFromPath(FileUtils.getFileFromStorage(it.background)?.absolutePath)

            button_ok.text = it.entryButton
            text_name.text = it.name
            text_title.text = it.title
            text_room.text = it.room
            text_description.text = it.description

            mPlayer = MediaPlayer.create(this, Uri.fromFile(FileUtils.getFileFromStorage(it.music)))
            mPlayer?.start()
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

    override fun onBackPressed() {
        return
    }
}