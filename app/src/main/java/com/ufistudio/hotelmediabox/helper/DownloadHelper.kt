package com.ufistudio.hotelmediabox.helper

import android.util.Log
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.schedulers.Schedulers
import java.io.*
import java.net.Socket
import java.net.UnknownHostException

object DownloadHelper {

    private val TAG = DownloadHelper::class.java.simpleName

    const val VERIFY_FILE_NAME_APK = "hotel_verify.apk"
    const val VERIFY_FILE_NAME = "hotel_verify.tar"
    const val TAR_PATH = "/data/correction/"
    const val DATA_PATH = "/data/hotel/"

    fun checkVersion():Single<String>{
        //TODO 確認下載版本，有可能之後會要寫進config內
        return sendTCPRequestSingle("k_ver")
    }

    fun downloadHotelAPK(downloadUrl: String = "", savePath: String = "$DATA_PATH$VERIFY_FILE_NAME_APK", fileMD5: String = ""):Single<String>{
        //TODO call 下載command，需要加上md5讓process做檔案損毀確認
        return sendTCPRequestSingle("k_download $downloadUrl $savePath $fileMD5")
    }

    fun downloadHotelTar(downloadUrl: String = "", savePath: String = "$TAR_PATH$VERIFY_FILE_NAME", fileMD5: String = ""):Single<String>{
        //TODO call 下載command，需要加上md5讓process做檔案損毀確認
        return sendTCPRequestSingle("k_download $downloadUrl $savePath $fileMD5")
    }

    fun checkDownloadProgress(savePath: String = "/data/correction/hotel_verify.tar"):Single<String>{
        //TODO 確認下載進度
        return sendTCPRequestSingle("k_check_download $savePath")
    }

    fun sendTCPRequestSingle(msg: String,port: Int = 5566): Single<String> {
        return Single.create { emitter: SingleEmitter<String> ->
            var serverHostname = "127.0.0.1"
            var serverPort = port

            Log.e(TAG, "Attemping to connect to host $serverHostname on port $serverPort and msg $msg")

            var echoSocket: Socket? = null
            var out: PrintWriter? = null
            var input: BufferedReader? = null
            var result: String? = ""

            try {
                echoSocket = Socket(serverHostname, serverPort)
                out = PrintWriter(echoSocket.getOutputStream(), true)
                input = BufferedReader(InputStreamReader(echoSocket.getInputStream()))
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Don't know about host: $serverHostname")
            } catch (e: IOException) {
                Log.e(TAG, "Couldn't get I/O for the connection to: $serverHostname")
                Log.e(TAG, "exception: $e")

                echoSocket = Socket(serverHostname, serverPort)
                out = PrintWriter(echoSocket.getOutputStream(), true)
                input = BufferedReader(InputStreamReader(echoSocket.getInputStream()))
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "java.lang.exception: $e")
            } catch (e: java.net.SocketException) {
                Log.e(TAG, "SocketException: $e")
                echoSocket = Socket(serverHostname, serverPort)
                out = PrintWriter(echoSocket.getOutputStream(), true)
                input = BufferedReader(InputStreamReader(echoSocket.getInputStream()))
            } catch (e: Exception) {
                Log.e(TAG, "exception: $e")
                echoSocket = Socket(serverHostname, serverPort)
                out = PrintWriter(echoSocket.getOutputStream(), true)
                input = BufferedReader(InputStreamReader(echoSocket.getInputStream()))
            }


            val stdIn = BufferedReader(InputStreamReader(ByteArrayInputStream(msg.toByteArray())))
            var userInput: String = ""

            try {
                while (stdIn.readLine()?.apply { userInput = this } != null) {
                    out?.println(userInput)
                    //TODO 這邊會connect reset 需要包起來
                    result = input?.readLine()
                    Log.e(TAG, "echo: $result")
                }
            }catch (e: Exception){
                Log.e(TAG,"readLine exception: $e")
            }

            out?.close()
            input?.close()
            stdIn.close()
            echoSocket?.close()

            emitter.onSuccess(result ?: "noResponse")
        }
    }
}