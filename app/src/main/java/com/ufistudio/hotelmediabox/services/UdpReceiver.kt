package com.ufistudio.hotelmediabox.services

import android.app.IntentService
import android.app.Service

import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.io.IOException
import java.net.*

class UdpReceiver : IntentService("UdpReceiver"), Runnable {
    val TAG_SERVER_IP = "192.168.2.16"
    val TAG_SERVER_PORT = 11000
    var socket: DatagramSocket? = null
    var mPacket: DatagramPacket? = null
    var mServerAddress: InetAddress? = null
    var mThread: Thread? = null

    companion object {
        val TAG = UdpReceiver::class.simpleName
    }

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        mThread = Thread(this)
        mThread?.start()
    }

    override fun run() {
        Log.d("neo", "run")
        registerToServer()
        receiveBroadcast()
    }

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

    private fun receiveBroadcast() {
        while (true) {
            Log.d("Neo", "receiveBroadcast")
            val recBuf: ByteArray = ByteArray(255)
            mPacket = null
            mPacket = DatagramPacket(recBuf, recBuf.size)
            try {
                socket?.receive(mPacket)
            } catch (e: IOException) {
                Log.e(TAG, e.toString())
            }

            Log.i(TAG, "Server: Message received = ${String(mPacket!!.data)}")
            //TODO(收到資料後存成Local json file)
            Log.i(TAG, "Server: IP = ${mPacket!!.address}")
        }
    }
}