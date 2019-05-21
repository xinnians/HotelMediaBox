package com.ufistudio.hotelmediabox.repository.remote

import android.util.Log
import io.reactivex.exceptions.OnErrorNotImplementedException
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.ProtocolException

class LoggingInterceptor : Interceptor {

    companion object {
        private val TAG = LoggingInterceptor::class.java.simpleName
    }

    override fun intercept(chain: Interceptor.Chain?): Response {
        var request = chain?.request()

        val t1 = System.nanoTime()
        Log.d(
            TAG, String.format(
                "Sending request %s on %s%n%s",
                request?.url(), chain?.connection(), request?.headers()
            )
        )
        var response: Response? = null



        try {
            if (request == null) {
                Request.Builder().build()
            }
            response = chain?.proceed(request)
        } catch (e: io.reactivex.rxkotlin.OnErrorNotImplementedException) {
            Log.e(TAG, "error = $e")
        } catch (e: ProtocolException) {
            Log.e(TAG, "error = $e")
        }

        val t2 = System.nanoTime()
        Log.d(
            TAG, String.format(
                "Received response for %s in %.1fms%n%s",
                response?.request()?.url(), (t2 - t1) / 1e6, request?.headers()
            )
        )

        return response!!
    }
}