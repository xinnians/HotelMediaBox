package com.ufistudio.hotelmediabox

import android.app.Application
import android.support.multidex.MultiDexApplication
import android.util.Log
import com.ufistudio.hotelmediabox.helper.TVHelper

class MyApplication : MultiDexApplication() {

    private val TAG:String = MyApplication::class.java.simpleName
    private var mTVHelper: TVHelper? = null

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG,"[MyApplication] onCreate call.")
        AppInjector.init(this)
        getTVHelper().initDevice()

    }

    override fun onTerminate() {
        super.onTerminate()
        Log.e(TAG,"[MyApplication] onTerminate call.")
        getTVHelper().closeAVPlayer()
        getTVHelper().closeDevice()
    }

    fun getTVHelper():TVHelper{
        Log.e(TAG,"[MyApplication] getTVHelper call.")
        if(mTVHelper == null){
            mTVHelper = TVHelper()
        }
        return mTVHelper as TVHelper
    }
}