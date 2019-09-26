package com.ufistudio.hotelmediabox.services

import android.app.IntentService

import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.ufistudio.hotelmediabox.constants.Cache
import com.ufistudio.hotelmediabox.constants.Cache.IsMessageUpdate
import com.ufistudio.hotelmediabox.constants.Cache.ServerIP
import com.ufistudio.hotelmediabox.helper.DownloadHelper
import com.ufistudio.hotelmediabox.helper.DownloadHelper.DATA_PATH
import com.ufistudio.hotelmediabox.helper.DownloadHelper.TAR_PATH
import com.ufistudio.hotelmediabox.helper.DownloadHelper.VERIFY_FILE_NAME
import com.ufistudio.hotelmediabox.helper.DownloadHelper.VERIFY_FILE_NAME_APK
import com.ufistudio.hotelmediabox.receivers.ACTION_UPDATE_APK
import com.ufistudio.hotelmediabox.receivers.TAG_FORCE
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.Broadcast
import com.ufistudio.hotelmediabox.repository.data.Config
import com.ufistudio.hotelmediabox.repository.data.KDownloadProgress
import com.ufistudio.hotelmediabox.repository.data.KDownloadVersion
import com.ufistudio.hotelmediabox.repository.provider.preferences.SharedPreferencesProvider
import com.ufistudio.hotelmediabox.repository.remote.ApiClient
import com.ufistudio.hotelmediabox.utils.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.rxkotlin.toSingle
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.net.*
import java.util.concurrent.TimeUnit

class UdpReceiver : IntentService("UdpReceiver"), Runnable {
    //    val TAG_SERVER_IP = "192.168.2.8"
    val TAG_SERVER_IP = "192.168.1.59"
    val TAG_SERVER_PORT = 11000
    var socket: DatagramSocket? = null
    var mPacket: DatagramPacket? = null
    var mServerAddress: InetAddress? = null
    var mThread: Thread? = null
    var mDownloadDisposable: Disposable? = null
    var mDownloadAPKDisposable: Disposable? = null

    var mIsDownloaderUsing: Boolean = false

    companion object {
        val TAG = UdpReceiver::class.simpleName
        private val TAG_CHECK_STATUS = "checkStatus".hashCode()
        private val TAG_EXPORT_CHANNEL_LIST = "exportChannelList".hashCode()
        private val TAG_IMPORT_CHANNEL_LIST = "importChannelList".hashCode()
        private val TAG_SOFTWARE_UPDATE = "softwareUpdate".hashCode()
        private val TAG_RESOURCE_UPDATE = "resourceUpdate".hashCode()
        private val TAG_SET_STATIC_IP = "setStaticIp".hashCode()
        private val TAG_REBOOT_DEVICE = "rebootDevice".hashCode()
        private val TAG_NEW_MESSAGE = "newMessages".hashCode()

        private val DWNLDR_NO_ERR = "0"
        private val DWNLDR_ERR_IN_PROGRESS = "1"
        private val DWNLDR_ERR_PARAM_NULL = "2"
        private val DWNLDR_ERR_FILEOUT_NULL = "3"
        private val DWNLDR_ERR_MD5_PARAM = "4"
        private val DWNLDR_ERR_CONNECT = "5"
        private val DWNLDR_ERR_404_FILENOTFOUND = "6"
        private val DWNLDR_ERR_SIZE_CAL = "7"
        private val DWNLDR_ERR_NO_CAPACITY = "8"
        private val DWNLDR_ERR_CURL_ERRORS = "254"
        private val DWNLDR_ERR_SYSTEM = "255"

        private val DWNLDR_DLINFO_FLAG_IDLE = "0"
        private val DWNLDR_DLINFO_INIT = "1"
        private val DWNLDR_DLINFO_STARTED = "2"
        private val DWNLDR_DLINFO_DOWNLOADING = "3"
        private val DWNLDR_DLINFO_COMPLETED = "4"
        private val DWNLDR_DLINFO_MD5_COMPUTED = "5"
        private val DWNLDR_DLINFO_ERROR = "255"
    }

    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }

    override fun onHandleIntent(intent: Intent?) {
        mThread = Thread(this)
        mThread?.start()
    }

    override fun run() {
        Log.d(TAG, "run UdpReceiver")
//        registerToServer()
        receiveBroadcast()
    }

    override fun stopService(name: Intent?): Boolean {
        Log.d(TAG, "Stop udp receiver service")
        socket = null
        return super.stopService(name)
    }

    /**
     * 向特定IP註冊
     */
    private fun registerToServer() {
        try {
            mServerAddress = InetAddress.getByName(TAG_SERVER_IP)
            Log.i(TAG, "Client : Start connecting")
            socket = DatagramSocket(TAG_SERVER_PORT)
            val buf: ByteArray = "Hell, World".toByteArray()
            mPacket = null
            mPacket = DatagramPacket(buf, buf.size, mServerAddress, TAG_SERVER_PORT)
            Log.i(TAG, "Client: Sending ${String(buf)}")
            socket?.send(mPacket)
            Log.i(TAG, "Client: Send")
            Log.i(TAG, "Client: Succeed")

        } catch (e: UnknownHostException) {
            e.printStackTrace()
        } catch (e: SocketException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private val gson = Gson()
    /**
     * 接收 Broadcast
     */
    private fun receiveBroadcast() {
        Log.d(TAG, "receiveBroadcast")
        socket?.disconnect()
        try {
            socket = DatagramSocket(TAG_SERVER_PORT)
        } catch (e: BindException) {
            Log.e(TAG, "Error = $e")
        }
        while (true) {
            val recBuf: ByteArray = ByteArray(255)
            mPacket = null
            mPacket = DatagramPacket(recBuf, recBuf.size)
            try {
                socket?.receive(mPacket)
            } catch (e: IOException) {
                Log.e(TAG, e.toString())
            }
            val receiverString = String(mPacket!!.data, mPacket!!.offset, mPacket!!.length)
            if (mPacket!!.address == null) {
                continue
            }
            try {
                Log.d(TAG, "Server: IP = ${mPacket!!.address}")
                Log.i(TAG, "Server: Message receiverString = $receiverString")
                val myBroadcast = gson.fromJson(receiverString, Broadcast::class.java)
                when (myBroadcast.command.hashCode()) {
                    TAG_CHECK_STATUS -> {

                        //將Server IP 寫回
                        val config = gson.fromJson(MiscUtils.getJsonFromStorage("box_config.json"), Config::class.java)
                        config.config.defaultServerIp = myBroadcast.ip

                        FileUtils.writeToFile(File("/data/$TAG_DEFAULT_LOCAL_PATH", "box_config.json"), gson.toJson(config))
                        Repository(application, SharedPreferencesProvider(application)).postCheckStatus("http://${myBroadcast.ip}:${myBroadcast.port}${myBroadcast.url}")
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    Log.d(TAG, "TAG_CHECK_STATUS success ${it.string()}")
                                }
                                        , {
                                    Log.d(TAG, "TAG_CHECK_STATUS error $it")
                                })
                    }
                    TAG_EXPORT_CHANNEL_LIST -> {
                        Repository(application, SharedPreferencesProvider(application)).postChannel("http://${myBroadcast.ip}:${myBroadcast.port}${myBroadcast.url}")
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSubscribe {
                                    Log.d(TAG, "TAG_EXPORT_CHANNEL_LIST on progress")
                                }
                                .subscribe({
                                    Log.d(TAG, "TAG_EXPORT_CHANNEL_LIST success ${it.string()}")
                                }
                                        , {
                                    Log.d(TAG, "TAG_EXPORT_CHANNEL_LIST error $it")
                                })
                    }
                    TAG_IMPORT_CHANNEL_LIST -> {
                        Repository(application, SharedPreferencesProvider(application)).downloadFileWithUrl("http://${myBroadcast.ip}:${myBroadcast.port}${myBroadcast.url}?device=${MiscUtils.getEthernetMacAddress()}")
                                .map {
                                    Single.fromCallable { FileUtils.writeResponseBodyToDisk(it, "box_channels.json") }
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                if (FileUtils.fileIsExists("box_channels.json"))
                                                    Log.d(TAG, "TAG_IMPORT_CHANNEL_LIST save file finish $it")
                                                else {
                                                    Log.d(TAG, "TAG_IMPORT_CHANNEL_LIST save file error : can not find file in storage")
                                                }
                                            }, {
                                                Log.d(TAG, "TAG_IMPORT_CHANNEL_LIST save file error : $it")
                                            })
                                }
                                .subscribe()
                    }
                    TAG_SOFTWARE_UPDATE -> {

                        if(mIsDownloaderUsing) return

                        mIsDownloaderUsing = true

                        if (mDownloadAPKDisposable != null && !mDownloadAPKDisposable!!.isDisposed) {
                            mDownloadAPKDisposable?.dispose()
                        }

                        mDownloadAPKDisposable = DownloadHelper.checkVersion()
                            .flatMap { checkVersionResult ->
                                FileUtils.fileIsExist(TAG_DEFAULT_LOCAL_PATH)
                                var versionInfo = Gson().fromJson(checkVersionResult, KDownloadVersion::class.java) ?: KDownloadVersion()
                                Log.e(TAG,"[TAG_SOFTWARE_UPDATE] versionInfo : $versionInfo")
//                                return@flatMap DownloadHelper.downloadHotelAPK("http://${myBroadcast.ip}:${myBroadcast.port}${myBroadcast.url}",fileMD5 = myBroadcast.md5)
                                return@flatMap  Repository(application, SharedPreferencesProvider(application))
                                    .getSoftwareUpdate("http://${myBroadcast.ip}:${myBroadcast.port}${myBroadcast.url}")}
                            .flatMap {
                                Log.d(TAG, "TAG_SOFTWARE_UPDATE response = $it")
                                if (it.needUpdate == 0) {
                                    //TODO判斷apk_version是否為較小
                                    return@flatMap Single.just(Exception("apk don't need update."))
                                }
                                return@flatMap DownloadHelper.downloadHotelAPK("http://${it.ip}:${it.port}${it.url}",fileMD5 = myBroadcast.md5)
                            }
                            .flatMap { downloadResult ->
                                Log.e(TAG,"[TAG_SOFTWARE_UPDATE] call download result : $downloadResult")
                                when(downloadResult){
                                    "0", "1"->{
                                        return@flatMap DownloadHelper.checkDownloadProgress("$DATA_PATH${DownloadHelper.VERIFY_FILE_NAME_APK}")
                                            .delay(10,TimeUnit.SECONDS)
                                            .flatMap { checkProgressResult ->
                                                var progressInfo = Gson().fromJson(checkProgressResult, KDownloadProgress::class.java) ?: KDownloadProgress()
                                                Log.e(TAG,"[TAG_SOFTWARE_UPDATE] progressInfo : $progressInfo")
                                                if(progressInfo.dlinfo.flag != DWNLDR_DLINFO_MD5_COMPUTED){
                                                    Log.e(TAG,"[TAG_SOFTWARE_UPDATE] progressInfo.dlinfo.flag != DWNLDR_DLINFO_MD5_COMPUTED($DWNLDR_DLINFO_MD5_COMPUTED)")
                                                    Single.error<Exception>(Exception("download not finish."))
                                                }else{
                                                    Single.just(progressInfo)
                                                }
                                            }
                                            .retry { count, erroMsg ->
                                                Log.e(TAG,"[TAG_SOFTWARE_UPDATE] retry  count : $count, erroMsg : $erroMsg")
                                                true }
//                                            .retry { t ->
//                                                Log.e(TAG,"[TAG_RESOURCE_UPDATE] retry msg : $t")
//                                                t.message == "download not finish." }
                                    }
                                    else ->{
                                        return@flatMap DownloadHelper.checkDownloadProgress()
                                            .map { checkProgressResult ->
                                                var progressInfo = Gson().fromJson(checkProgressResult, KDownloadProgress::class.java) ?: KDownloadProgress()
                                                Log.e(TAG,"[TAG_SOFTWARE_UPDATE] progressInfo : $progressInfo")
                                                if(progressInfo.dlinfo.flag != DWNLDR_DLINFO_MD5_COMPUTED){
                                                    return@map Single.just(Exception("download not finish."))
                                                }else{
                                                    return@map Single.just(Exception("download fail = $downloadResult"))
                                                }
                                            }
                                    }
                                }
                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({onSuccess ->
                                Log.e(TAG,"[TAG_SOFTWARE_UPDATE] onSuccess")

                                if(onSuccess is KDownloadProgress){
                                    Log.e(TAG,"[TAG_SOFTWARE_UPDATE] onSuccess is KDownloadProgress : $onSuccess")
                                    if(onSuccess.md5result.cmp == "0"){
                                        //TODO 下載成功且md5確認無誤，覆蓋到現有的hotel.tar上，然後刪除chkflag並重開機。

                                        FileUtils.getFileFromStorage(VERIFY_FILE_NAME_APK,DATA_PATH)?.let { verifyFile ->
                                            val fileHotelTar = File("/data$TAG_DEFAULT_LOCAL_PATH", TAG_DEFAULT_APK_NAME)
                                            verifyFile.renameTo(fileHotelTar)
                                        }
                                        if (FileUtils.fileIsExists(TAG_DEFAULT_APK_NAME)) {
                                            Log.d(TAG, "TAG_SOFTWARE_UPDATE download success")
                                            val intent = Intent()
                                            val b = Bundle()
                                            b.putString(TAG_FORCE, myBroadcast.force)
                                            intent.putExtras(b)
                                            intent.action = ACTION_UPDATE_APK
                                            sendBroadcast(intent)
                                        } else {
                                            Log.d(TAG, "TAG_SOFTWARE_UPDATE download finish, but can not find file")
                                            mIsDownloaderUsing = false
                                        }

                                    }else{
                                        //TODO 下載完成但md5確認有異，所以刪除該檔案
                                        FileUtils.getFileFromStorage(VERIFY_FILE_NAME_APK,DATA_PATH)?.delete()
                                        mIsDownloaderUsing = false
                                    }
                                }else{
                                    Log.e(TAG,"[TAG_SOFTWARE_UPDATE] onSuccess not a KDownloadProgress : $onSuccess")
                                    mIsDownloaderUsing = false
                                }
                            },{onError ->
                                Log.e(TAG,"[TAG_SOFTWARE_UPDATE] onError : $onError")
                                mIsDownloaderUsing = false
                            })

                    }
                    TAG_RESOURCE_UPDATE -> {

                        if(mIsDownloaderUsing) return

                        mIsDownloaderUsing = true

                        if (mDownloadDisposable != null && !mDownloadDisposable!!.isDisposed) {
                            mDownloadDisposable?.dispose()
                        }

                        mDownloadDisposable = DownloadHelper.checkVersion()
                            .flatMap { checkVersionResult ->
                                FileUtils.fileIsExist(TAG_DEFAULT_CORRECTION_PATH)
                                var versionInfo = Gson().fromJson(checkVersionResult, KDownloadVersion::class.java) ?: KDownloadVersion()
                                Log.e(TAG,"[TAG_RESOURCE_UPDATE] versionInfo : $versionInfo")
                                return@flatMap DownloadHelper.downloadHotelTar("http://${myBroadcast.ip}:${myBroadcast.port}${myBroadcast.url}",fileMD5 = myBroadcast.md5) }
                            .flatMap { downloadResult ->
                                Log.e(TAG,"[TAG_RESOURCE_UPDATE] call download result : $downloadResult")
                                when(downloadResult){
                                    "0", "1"->{
                                        return@flatMap DownloadHelper.checkDownloadProgress()
                                            .delay(10,TimeUnit.SECONDS)
                                            .flatMap { checkProgressResult ->
                                                var progressInfo = Gson().fromJson(checkProgressResult, KDownloadProgress::class.java) ?: KDownloadProgress()
                                                Log.e(TAG,"[TAG_RESOURCE_UPDATE] progressInfo : $progressInfo")
                                                if(progressInfo.dlinfo.flag != DWNLDR_DLINFO_MD5_COMPUTED){
                                                    Log.e(TAG,"[TAG_RESOURCE_UPDATE] progressInfo.dlinfo.flag != DWNLDR_DLINFO_MD5_COMPUTED($DWNLDR_DLINFO_MD5_COMPUTED)")
                                                    Single.error<Exception>(Exception("download not finish."))
                                                }else{
                                                    Single.just(progressInfo)
                                                }
                                            }
                                            .retry { count, erroMsg ->
                                                Log.e(TAG,"[TAG_RESOURCE_UPDATE] retry  count : $count, erroMsg : $erroMsg")
                                                true }
//                                            .retry { t ->
//                                                Log.e(TAG,"[TAG_RESOURCE_UPDATE] retry msg : $t")
//                                                t.message == "download not finish." }
                                    }
                                    else ->{
                                        return@flatMap DownloadHelper.checkDownloadProgress()
                                            .map { checkProgressResult ->
                                                var progressInfo = Gson().fromJson(checkProgressResult, KDownloadProgress::class.java) ?: KDownloadProgress()
                                                Log.e(TAG,"[TAG_RESOURCE_UPDATE] progressInfo : $progressInfo")
                                                if(progressInfo.dlinfo.flag != DWNLDR_DLINFO_MD5_COMPUTED){
                                                    return@map Single.just(Exception("download not finish."))
                                                }else{
                                                    return@map Single.just(Exception("download fail = $downloadResult"))
                                                }
                                            }
                                    }
                                }
                            }
                            .subscribeOn(Schedulers.io())
                            .subscribe({onSuccess ->
                                Log.e(TAG,"[TAG_RESOURCE_UPDATE] onSuccess")

                                if(onSuccess is KDownloadProgress){
                                    Log.e(TAG,"[TAG_RESOURCE_UPDATE] onSuccess is KDownloadProgress : $onSuccess")
                                    if(onSuccess.md5result.cmp == "0"){
                                        //TODO 下載成功且md5確認無誤，覆蓋到現有的hotel.tar上，然後刪除chkflag並重開機。

                                        FileUtils.getFileFromStorage(VERIFY_FILE_NAME,TAR_PATH)?.let { verifyFile ->
                                            val fileHotelTar = File("/data$TAG_DEFAULT_CORRECTION_PATH", TAG_DEFAULT_HOTEL_TAR_FILE_NAME)
                                            verifyFile.renameTo(fileHotelTar)
                                            FileUtils.getFileFromStorage("chkflag")?.delete()
                                            MiscUtils.reboot(baseContext)
                                        }

                                    }else{
                                        //TODO 下載完成但md5確認有異，所以刪除該檔案
                                        FileUtils.getFileFromStorage(VERIFY_FILE_NAME,TAR_PATH)?.delete()
                                        mIsDownloaderUsing = false
                                    }
                                }else{
                                    Log.e(TAG,"[TAG_RESOURCE_UPDATE] onSuccess not a KDownloadProgress : $onSuccess")
                                    mIsDownloaderUsing = false
                                }
                            },{onError ->
                                Log.e(TAG,"[TAG_RESOURCE_UPDATE] onError : $onError")
                                mIsDownloaderUsing = false
                            })
                    }
                    TAG_SET_STATIC_IP -> {
                        Repository(application, SharedPreferencesProvider(application)).getStaticIp("http://${myBroadcast.ip}:${myBroadcast.port}${myBroadcast.url}")
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    Log.d(TAG, "TAG_SET_STATIC_IP get api success $it")
                                    if (!TextUtils.isEmpty(it.defaultIp)) {
                                        val host: XTNetWorkManager.XTHost = XTNetWorkManager.getInstance().XTHost(it.defaultIp, it.gateway
                                                ?: "0.0.0.0", it.defaultMask ?: "")
                                        XTNetWorkManager.getInstance().enableEthernetStaticIP(applicationContext, host)
                                    } else {
                                        XTNetWorkManager.getInstance().enableEthernetDHCP(applicationContext)
                                    }
                                    Cache.IsDHCP = XTNetWorkManager.getInstance().isEthernetUseDHCP(application)
                                }, {
                                    Log.e(TAG, "TAG_SET_STATIC_IP error $it")
                                })
                    }
                    TAG_REBOOT_DEVICE -> {
                        MiscUtils.reboot(baseContext)
                    }
                    TAG_NEW_MESSAGE -> {
                        ServerIP = myBroadcast.ip
                        IsMessageUpdate = true
                    }
                }
            } catch (e: JsonSyntaxException) {
                Log.d(TAG, "error = $e")
            }
        }
    }
}