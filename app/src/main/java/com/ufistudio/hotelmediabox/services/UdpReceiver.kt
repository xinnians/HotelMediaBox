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
import com.ufistudio.hotelmediabox.receivers.ACTION_UPDATE_APK
import com.ufistudio.hotelmediabox.receivers.TAG_FORCE
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.Broadcast
import com.ufistudio.hotelmediabox.repository.data.Config
import com.ufistudio.hotelmediabox.repository.provider.preferences.SharedPreferencesProvider
import com.ufistudio.hotelmediabox.utils.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.view_date.view.*
import java.io.File
import java.io.IOException
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

    companion object {
        val TAG = UdpReceiver::class.simpleName
        private val TAG_CHECK_STATUS = "checkStatus".hashCode()
        private val TAG_EXPORT_CHANNEL_LIST = "exportChannelList".hashCode()
        private val TAG_IMPORT_CHANNEL_LIST = "importChannelList".hashCode()
        private val TAG_SOFTWARE_UPDATE = "softwareUpdate".hashCode()
        private val TAG_RESOURCE_UPDATE = "resourceUpdate".hashCode()
        private val TAG_SET_STATIC_IP = "setStaticIp".hashCode()
        private val TAG_REBOOT_DEVICE = "rebootDevice".hashCode()
    }

    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
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
                        //將 更新的apk下載到/data/hotel資料夾內
                        Repository(application, SharedPreferencesProvider(application)).getSoftwareUpdate("http://${myBroadcast.ip}:${myBroadcast.port}${myBroadcast.url}")
                                .map {
                                    Log.d(TAG, "TAG_SOFTWARE_UPDATE response = $it")
                                    if (it.needUpdate == 0) {
                                        //TODO判斷apk_version是否為較小
                                        return@map
                                    }
                                    Repository(application, SharedPreferencesProvider(application)).downloadFileWithUrl("http://${it.ip}:${it.port}${it.url}")
                                            .flatMap {
                                                Single.fromCallable { FileUtils.writeResponseBodyToDisk(it, TAG_DEFAULT_APK_NAME) }
                                            }
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
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
                                                }
                                            }, {
                                                Log.d(TAG, "TAG_SOFTWARE_UPDATE download error $it")
                                            })
                                }
                                .subscribeOn(Schedulers.io())
                                .subscribe()
                    }
                    TAG_RESOURCE_UPDATE -> {
                        var retried: Boolean = false

                        fun downloadResource() {
                            //將 hotel.tar下載到/data/correction資料夾內
                            Repository(application, SharedPreferencesProvider(application)).downloadFileWithUrl("http://${myBroadcast.ip}:${myBroadcast.port}${myBroadcast.url}")
                                    .flatMap { Single.fromCallable { FileUtils.writeResponseBodyToDisk(it, TAG_HOTEL_VERIFY_TAR_FILE_NAME, TAG_DEFAULT_CORRECTION_PATH) } }
                                    .retry(1)
                                    .subscribe({
                                        if (FileUtils.fileIsExists(TAG_HOTEL_VERIFY_TAR_FILE_NAME, TAG_DEFAULT_CORRECTION_PATH)) {
                                            FileUtils.getFileFromStorage(TAG_HOTEL_VERIFY_TAR_FILE_NAME, "/data$TAG_DEFAULT_CORRECTION_PATH")?.let {
                                                val checkSum = MD5Utils.getMD5CheckSum(it)
                                                Log.d(TAG, "TAG_RESOURCE_UPDATE resource check sum = $checkSum")
                                                if (TextUtils.equals(checkSum, myBroadcast.md5)) {
                                                    Log.d(TAG, "TAG_RESOURCE_UPDATE download success")
                                                    val fileHotelTar = File("/data$TAG_DEFAULT_CORRECTION_PATH", TAG_DEFAULT_HOTEL_TAR_FILE_NAME)
                                                    it.renameTo(fileHotelTar)

                                                    FileUtils.getFileFromStorage("chkflag")?.delete()
                                                    MiscUtils.reboot(baseContext)
                                                } else {
                                                    Log.d(TAG, "TAG_RESOURCE_UPDATE CheckSum is not correct with server value : ${myBroadcast.md5}")
                                                }
                                            }

                                        } else {
                                            Log.d(TAG, "TAG_RESOURCE_UPDATE download finish, but can not find file")
                                        }
                                    }, {
                                        Log.d(TAG, "TAG_RESOURCE_UPDATE download error $it")
                                    })
                        }
                        downloadResource()
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
                                }, {
                                    Log.e(TAG, "TAG_SET_STATIC_IP error $it")
                                })
                    }
                    TAG_REBOOT_DEVICE -> {
                        MiscUtils.reboot(baseContext)
                    }
                }
            } catch (e: JsonSyntaxException) {
                Log.d(TAG, "error = $e")
            }
        }
    }
}