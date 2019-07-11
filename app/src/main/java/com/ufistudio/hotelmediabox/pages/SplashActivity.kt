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
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.view_date.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class SplashActivity : AppCompatActivity() {

    private var mCheckChkFlagDisposable: Disposable? = null
    private var mCheckCounts: Int = 0

    private val TAG = SplashActivity::class.java.simpleName

    private var mTVListener: TVController.OnTVListener = object : TVController.OnTVListener {
        override fun onIPTVLoading() {
        }

        override fun onIPTVPlaying() {
        }

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
        checkChkFlag()
    }

    override fun onPause() {
        super.onPause()
        TVController.releaseListener(mTVListener)

        if (mCheckChkFlagDisposable != null && mCheckChkFlagDisposable?.isDisposed == false) {
            mCheckChkFlagDisposable?.dispose()
        }
    }

    private fun goNextPage() {
        val intent = Intent(this, WelcomeActivity::class.java)
//        val intent = Intent(this, MainActivity::class.java)
//        val intent = Intent(this, DVBTestActivity::class.java)

//        var waitTime = 2500L
//
//        if(!fileIsExists("chkflag")){
//            Log.e(TAG,"chkflag isn't Exist.")
//            waitTime = 10000L
//        }else{
//            Log.e(TAG,"chkflag is Exist.")
//        }
//
//        val timer: Timer = Timer()
//        timer.schedule(waitTime) {
            startActivity(intent)
            finish()
//        }
    }

    private fun checkChkFlag(){
        mCheckCounts = 0
        mCheckChkFlagDisposable = Observable.interval(2500, 5000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                mCheckCounts = mCheckCounts.plus(1)
                if(mCheckCounts == 20){
                    Log.e(TAG,"[checkChkFlag] checkCount : $mCheckCounts, force go to nextPage.")
                    goNextPage()
                }
                if(fileIsExists("chkflag")){
                    Log.e(TAG,"[checkChkFlag] file is Exists, ready go to nextPage. checkCount : $mCheckCounts")
                    goNextPage()
                }else{
                    Log.e(TAG,"[checkChkFlag] file not Exists, ready go to nextPage. checkCount : $mCheckCounts")
                }
            }
    }
}