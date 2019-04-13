package com.ufistudio.hotelmediabox.services

import android.app.IntentService

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.Broadcast
import com.ufistudio.hotelmediabox.repository.provider.preferences.SharedPreferencesProvider
import com.ufistudio.hotelmediabox.utils.FileUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.net.*

class UdpReceiver : IntentService("UdpReceiver"), Runnable {
    val TAG_SERVER_IP = "192.168.2.8"
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

    /**
     * 接收 Broadcast
     */
    private fun receiveBroadcast() {
        while (true) {
            Log.d(TAG, "receiveBroadcast")
            val recBuf: ByteArray = ByteArray(255)
            mPacket = null
            mPacket = DatagramPacket(recBuf, recBuf.size)
            try {
                socket?.receive(mPacket)
            } catch (e: IOException) {
                Log.e(TAG, e.toString())
            }
            val receiverString = String(mPacket!!.data, mPacket!!.offset, mPacket!!.length)
            Log.i(TAG, "Server: Message received = ${mPacket!!.data}")
            Log.i(TAG, "Server: Message receiverString = $receiverString")

            try {
                val gson = Gson()
                val myBroadcast = gson.fromJson(receiverString, Broadcast::class.java)
                when (myBroadcast.command.hashCode()) {
                    TAG_CHECK_STATUS -> {
                        Repository(application, SharedPreferencesProvider(application)).postCheckStatus("http://${myBroadcast.ip}${myBroadcast.url}:${myBroadcast.port}")
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    Log.d(TAG, "TAG_CHECK_STATUS success $it")
                                }
                                        , {
                                    Log.d(TAG, "TAG_CHECK_STATUS error $it")
                                })
                    }
                    TAG_EXPORT_CHANNEL_LIST -> {
                        Repository(application, SharedPreferencesProvider(application)).postChannel("http://${myBroadcast.ip}${myBroadcast.url}:${myBroadcast.port}")
                                .subscribeOn(Schedulers.io())
                                .doOnSubscribe {
                                    Log.d(TAG, "TAG_EXPORT_CHANNEL_LIST on progress $it")
                                }
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    Log.d(TAG, "TAG_EXPORT_CHANNEL_LIST success $it")
                                }
                                        , {
                                    Log.d(TAG, "TAG_EXPORT_CHANNEL_LIST error $it")
                                })
                    }
                    TAG_IMPORT_CHANNEL_LIST -> {
                        Repository(application, SharedPreferencesProvider(application)).downloadFileWithUrl("http://${myBroadcast.ip}${myBroadcast.url}:${myBroadcast.port}")
                                .map {
                                    Single.just(FileUtils.writeResponseBodyToDisk(it, "channels.json"))
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                Log.d(TAG, "TAG_IMPORT_CHANNEL_LIST save file finish $it")
                                            }, {
                                                Log.d(TAG, "TAG_IMPORT_CHANNEL_LIST save file error $it")
                                            })
                                }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    Log.d(TAG, "TAG_IMPORT_CHANNEL_LIST success")
                                }
                                        , {
                                    Log.d(TAG, "TAG_IMPORT_CHANNEL_LIST error $it")
                                })
                    }
                }
                Log.d(TAG, "receive ip = ${myBroadcast.ip}")
            } catch (e: JsonSyntaxException) {
                Log.d(TAG, "error = $e")
            }

            Log.d(TAG, "Server: IP = ${mPacket!!.address}")
        }
    }
}