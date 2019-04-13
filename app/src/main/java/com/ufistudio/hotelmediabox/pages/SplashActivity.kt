package com.ufistudio.hotelmediabox.pages

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.welcome.WelcomeActivity
import com.ufistudio.hotelmediabox.services.UdpReceiver
import java.util.*
import kotlin.concurrent.schedule

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
//        DVBHelper.getDVBPlayer().initPlayer(0,0,0,0)
//        DVBHelper.getDVBPlayer().closePlayer()
    }

    override fun onStart() {
        super.onStart()
        startService(Intent(this, UdpReceiver::class.java))
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, WelcomeActivity::class.java)
//        val intent = Intent(this, MainActivity::class.java)
//        val intent = Intent(this, DVBTestActivity::class.java)
        val timer: Timer = Timer()
        timer.schedule(2500L) {
            startActivity(intent)
            finish()
        }
//        startActivity(intent)
    }
}