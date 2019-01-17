package com.ufistudio.hotelmediabox.repository.remote

import android.content.Context
import android.os.Build
import android.util.Log
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import java.lang.Exception
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

abstract class RemoteAPI {

    companion object {
        private val TAG = RemoteAPI::class.simpleName
        private const val MAX_IDLE_CONNECTIONS = 8
        private const val KEEP_ALIVE_DURATION = 300L
        private const val CONNECTION_TIMEOUT = 15L
        private const val READ_TIMEOUT = 15L
        private const val WRITE_TIMEOUT = 15L

        private lateinit var sContextRef: WeakReference<Context>
        private var mOkHttpClient: OkHttpClient? = null

        fun init(context: Context) {
            sContextRef = WeakReference(context)
        }

        private fun createClient(): OkHttpClient {
            var context = sContextRef.get()

            val connectionPool = ConnectionPool(MAX_IDLE_CONNECTIONS, KEEP_ALIVE_DURATION, TimeUnit.SECONDS)
            val builder = OkHttpClient.Builder()
            builder.connectionPool(connectionPool)
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(LoggingInterceptor())

            return enableTLS120nPreLollipop(builder).build()
        }

        fun getOkHttpClient(): OkHttpClient? {
            if (mOkHttpClient == null) {
                mOkHttpClient = createClient()
            }
            return mOkHttpClient
        }

        private fun enableTLS120nPreLollipop(builder: OkHttpClient.Builder): OkHttpClient.Builder {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
                try {
                    builder.connectionSpecs(arrayListOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))
                } catch (e: Exception) {
                    Log.e(TAG, "[enableTLS12OnPreLollipop] Error while setting TLS 1.2 ${e.message}")
                }
            }
            return builder
        }
    }
}