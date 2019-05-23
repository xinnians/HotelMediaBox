package com.ufistudio.hotelmediabox.pages

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.helper.TVController
import com.ufistudio.hotelmediabox.pages.welcome.WelcomeActivity
import com.ufistudio.hotelmediabox.repository.data.TVChannel
import com.ufistudio.hotelmediabox.services.UdpReceiver
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.utils.FileUtils.fileIsExists
import com.ufistudio.hotelmediabox.utils.MiscUtils
import com.ufistudio.hotelmediabox.utils.TAG_DEFAULT_APK_NAME
import java.util.*
import kotlin.concurrent.schedule

class SplashActivity : AppCompatActivity() {

    private val TAG = SplashActivity::class.java.simpleName

    private var mTVListener: TVController.OnTVListener = object : TVController.OnTVListener {
        override fun onScanFinish() {

        }

        override fun onChannelChange(tvChannel: TVChannel?) {
        }

        override fun initDeviceFinish() {

        }

        override fun initAVPlayerFinish() {
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onStart() {
        super.onStart()
        if (FileUtils.getFileFromStorage(TAG_DEFAULT_APK_NAME) != null && FileUtils.getFileFromStorage(TAG_DEFAULT_APK_NAME)?.exists() == true) {
            FileUtils.getFileFromStorage(TAG_DEFAULT_APK_NAME)?.delete()
            MiscUtils.reboot(this)
        }

        startService(Intent(this, UdpReceiver::class.java))
    }

    override fun onResume() {
        super.onResume()
        TVController.registerListener(mTVListener)
        // initDVB，init完成後再進下一頁
        TVController.initDevice()
        goNextPage()
    }

    override fun onPause() {
        super.onPause()
        TVController.releaseListener(mTVListener)
    }

    fun goNextPage() {
        val intent = Intent(this, WelcomeActivity::class.java)
//        val intent = Intent(this, MainActivity::class.java)
//        val intent = Intent(this, DVBTestActivity::class.java)

        var waitTime = 2500L

        if(!fileIsExists("chkflag")){
            Log.e(TAG,"chkflag isn't Exist.")
            waitTime = 10000L
        }else{
            Log.e(TAG,"chkflag is Exist.")
        }

        val timer: Timer = Timer()
        timer.schedule(waitTime) {
            startActivity(intent)
            finish()
        }
    }
}