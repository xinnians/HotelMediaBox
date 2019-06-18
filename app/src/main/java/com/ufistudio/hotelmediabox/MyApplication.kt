package com.ufistudio.hotelmediabox

import android.support.multidex.MultiDexApplication
import android.util.Log
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.*
import com.google.android.exoplayer2.util.Util
import com.ufistudio.hotelmediabox.helper.TVHelper
import java.io.File

class MyApplication : MultiDexApplication() {

    private val TAG:String = MyApplication::class.java.simpleName
    private var mTVHelper: TVHelper? = null

    private val DOWNLOAD_CONTENT_DIRECTORY = "downloads"
    private var mDownloadDirectory: File? = null
    private var mDownloadCache: Cache? = null
    private var mUserAgent: String? = ""

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG,"[MyApplication] onCreate call.")
        AppInjector.init(this)
        getTVHelper().initDevice()
        mUserAgent = Util.getUserAgent(this,"ExoPlayer")

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

    fun buildDataSourceFactory(): DataSource.Factory?{
        var upstreamFactory: DefaultDataSourceFactory = DefaultDataSourceFactory(this, buildHttpDataSourceFactory())
        return getDownloadCache()?.let { cache -> buildReadOnlyCacheDataSource(upstreamFactory, cache) }
    }

    fun buildHttpDataSourceFactory(): HttpDataSource.Factory{
        return DefaultHttpDataSourceFactory(mUserAgent)
    }

    private fun buildReadOnlyCacheDataSource(upstreamFactory: DefaultDataSourceFactory, cache: Cache): CacheDataSourceFactory{
        return CacheDataSourceFactory(
            cache,
            upstreamFactory,
            FileDataSourceFactory(),
            null,
            CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
            null)
    }

    private fun getDownloadCache(): Cache?{
        if(mDownloadCache == null){
            var downloadContentDirectory = File(getDownloadDirectory(),DOWNLOAD_CONTENT_DIRECTORY)
            mDownloadCache = SimpleCache(downloadContentDirectory, NoOpCacheEvictor())
        }
        return mDownloadCache
    }

    private fun getDownloadDirectory(): File?{
        if(mDownloadDirectory == null){
            mDownloadDirectory = getExternalFilesDir(null)
            if(mDownloadDirectory == null){
                mDownloadDirectory = filesDir
            }
        }
        return mDownloadDirectory
    }
}