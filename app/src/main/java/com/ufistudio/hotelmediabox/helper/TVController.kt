package com.ufistudio.hotelmediabox.helper

import android.util.Log
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.repository.data.Channels
import com.ufistudio.hotelmediabox.repository.data.Config
import com.ufistudio.hotelmediabox.repository.data.JVersion
import com.ufistudio.hotelmediabox.repository.data.TVChannel
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.utils.MiscUtils
import com.ufistudio.hotelmediabox.utils.TAG_DEFAULT_LOCAL_PATH
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.io.*
import java.net.Socket
import java.net.UnknownHostException
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors


object TVController {
    private val TAG = TVController::class.java.simpleName

    val singelThreadScheduler = Schedulers.from(Executors.newSingleThreadExecutor())

    //鎖定頻點
    private var mCurrentLockFrequency: String = ""
    //當前頻點播放頻道參數
    private var mCurrentPlayProgram: String = ""
    //當前頻點播放頻道
    private var mCurrentChannel: TVChannel? = null
    //當前畫面顯示類型
    private var mCurrentScreenType: SCREEN_TYPE = SCREEN_TYPE.HOMEPAGE
    //頻道列表
    private var mChannelList: ArrayList<TVChannel>? = null

    private var mIsAVPlayerInit = false
    private var mIsDeviceInit = false

    const val RESULT_SUCCESS = "0"
    const val RESULT_FAIL = "1"

    private var mPlayDisposable: Disposable? = null
//    960,558
    enum class SCREEN_TYPE(width: Int, height: Int, x: Int, y: Int) {
        HOMEPAGE(873, 506, 87, 52),
        CHANNELPAGE(980, 555, 857, 233),
        FULLSCREEN(0, 0, 0, 0)
    }

    fun getChannelList(): ArrayList<TVChannel>? {
        Log.e(TAG, "[getChannelList] call.")
        if (mChannelList == null || mChannelList?.size ?: 0 == 0) {
            val jsonObject: Channels =
                    Gson().fromJson(MiscUtils.getJsonFromStorage("box_channels.json"), Channels::class.java)
                            ?: return null
            mChannelList = jsonObject.channels.toCollection(ArrayList())
        }
        Log.e(TAG, "[TVHelper] getChannelList size = ${mChannelList?.size ?: "null"}")
        return mChannelList
    }

    /**
     * 在app開啟的時候call一次就好，暫定splashPage
     */
    fun initDevice() {
        Log.e(TAG, "[initDevice] call.")

        if (mIsDeviceInit) {
            Log.e(TAG, "[initDevice] already init.")
            onBroadcastAll(null, TVController.ACTION_TYPE.InitDeviceFinish)
        }
        sendTCPRequestSingle("j_dev_init")
                .map { result ->
                    if (result == RESULT_SUCCESS) mIsDeviceInit = true
                    return@map result
                }
                .subscribeOn(singelThreadScheduler)
                .observeOn(singelThreadScheduler)
                .subscribe({ successResult ->
                    Log.e(TAG, "[initDevice] Result : $successResult")
                    onBroadcastAll(null, TVController.ACTION_TYPE.InitDeviceFinish)
                }, { failResult ->
                    Log.e(TAG, "[initDevice] throwable : $failResult")
                })
    }

    fun initAVPlayer(screenType: SCREEN_TYPE) {
        Log.e(TAG, "[initAVPlayer] ${screenType.name} call.")

        mCurrentScreenType = screenType
        var width = 0
        var height = 0
        var x = 0
        var y = 0
        when (screenType) {
            SCREEN_TYPE.HOMEPAGE -> {
                width = 874
                height = 506
                x = 87
                y = 52
            }
            SCREEN_TYPE.CHANNELPAGE -> {
                width = 980
                height = 555
                x = 857
                y = 233
            }
            SCREEN_TYPE.FULLSCREEN -> {
                width = 0
                height = 0
                x = 0
                y = 0
            }
        }

        sendTCPRequestSingle("j_set_win $x $y $width $height")
                .retry(1)
                .subscribeOn(singelThreadScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ successResult ->
                    Log.e(TAG, "[initAVPlayer] Result : $successResult")
                    onBroadcastAll(null, TVController.ACTION_TYPE.InitAVPlayerFinish)
                }, { failResult ->
                    Log.e(TAG, "[initAVPlayer] throwable : $failResult")
                })
    }

    /**
     * 因為切換頻道後不會立即播放的關係，所以才會長得像這樣奇耙
     */
    fun playCurrent() {
        Log.e(TAG, "[playCurrent] call.")
        if (getChannelList() == null || getChannelList()?.size ?: 0 == 0) {
            return
        }
        if (mCurrentChannel == null) {
            mCurrentChannel = getChannelList()?.get(0)
        }
        mCurrentChannel?.let { tvChannel ->
            play(tvChannel)
        }

        return
    }


    //TODO 應該再增加個play method並在裡面做好鎖頻的判斷以及dvb和iptv的切換邏輯
    fun play(channel: TVChannel) {
        Log.i(TAG, "[play] call $channel")
        if (getChannelList() == null || getChannelList()?.size ?: 0 == 0) {
            Log.i(TAG, "[play] channelList null.")
            return
        }

        mPlayDisposable?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }

        mCurrentChannel = channel

        mPlayDisposable = sendTCPRequestSingle("j_stopplay 1")
                .flatMap {
//                    var channelParameter = channel.chIp.frequency + " " + channel.chIp.bandwidth
                    var channelParameter = if(channel.chIp.frequencyParameter.isNullOrEmpty()) channel.chIp.frequency + " " + channel.chIp.bandwidth else channel.chIp.frequencyParameter

                    if (mCurrentLockFrequency == channelParameter) {
                        return@flatMap Single.fromCallable { RESULT_SUCCESS }
                    }
                    return@flatMap sendTCPRequestSingle("j_setchan $channelParameter")
                            .map { result ->
                                mCurrentLockFrequency = if (result == RESULT_SUCCESS) channelParameter
                                else ""
                                return@map result
                            }
                }
                .flatMap { sendTCPRequestSingle("j_play ${channel.chIp.dvbParameter}") }
                .retry(1)
                .subscribeOn(singelThreadScheduler)
                .observeOn(singelThreadScheduler)
                .subscribe({ successResult ->
                    Log.e(TAG, "[play] Result : $successResult")
                    onBroadcastAll(channel, TVController.ACTION_TYPE.OnChannelChange)
                }, { failResult ->
                    Log.e(TAG, "[play] throwable : $failResult")
                })
    }

//    /**
//     * stopType 暫停類型：0 = 畫面暫存, 1 = 畫面全黑
//     */
//    fun stop(stopType: String): Single<String> {
//        Log.e(TAG, "[stopType] call.")
//        return DVBUtils().sendTCPRequestSingle("j_stopplay $stopType")
//    }

    fun deInitAVPlayer() {
        Log.e(TAG, "[deInitAVPlayer] call.")
        sendTCPRequestSingle("j_stopplay 1")
//            .flatMap { result ->
//                Log.e(TAG, "[deInitAVPlayer] avplay_deinit result = $result")
//                return@flatMap sendTCPRequestSingle("jv_win_deinit")
//            }
                .retry(1)
                .subscribeOn(singelThreadScheduler)
                .observeOn(singelThreadScheduler)
                .subscribe({ successResult ->
                    Log.e(TAG, "[deInitAVPlayer] Result : $successResult")
//                onBroadcastAll(null,TVController.ACTION_TYPE.)
                }, { failResult ->
                    Log.e(TAG, "[deInitAVPlayer] throwable : $failResult")
                })
    }

    fun scanChannel() {
        Log.e(TAG, "[scanChannel] call.")
        sendTCPRequestSingle("j_presetscan /data/hotel/box_DvbScanConfig.json /data/hotel/box_scanned_channels.json")
                .subscribeOn(singelThreadScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ successResult ->
                    Log.e(TAG, "[j_presetscan] Result : $successResult")
                    onBroadcastAll(null,TVController.ACTION_TYPE.OnScanFinish)
                }, { failResult ->
                    Log.e(TAG, "[j_presetscan] throwable : $failResult")
                })

    }

    fun chooseUp(): TVChannel? {
        if (getChannelList() == null || getChannelList()?.size ?: 0 == 0) {
            return null
        }
        if (mCurrentChannel == null) {
            mCurrentChannel = getChannelList()?.get(0)
        }
        getChannelList()?.let { channelList ->
            if (mCurrentChannel != null) {
                var position = channelList.indexOf(mCurrentChannel!!)
                if (position == -1) {
                    mCurrentChannel = channelList[0]
                    return mCurrentChannel
                } else {
                    var channel: TVChannel
                    if (channelList.size - 1 > position) {
                        channel = channelList[position + 1]
//                        play(channel).subscribe({ mCurrentPlayProgram = channel.chIp.dvbParameter }, {})
                    } else {
                        channel = channelList[0]
//                        play(channel).subscribe({ mCurrentPlayProgram = channel.chIp.dvbParameter }, {})
                    }
                    mCurrentPlayProgram = channel.chIp.dvbParameter
                    mCurrentChannel = channel
                    return mCurrentChannel
                }
            } else {
                return mCurrentChannel
            }
        }

        return null
    }

    fun chooseDown(): TVChannel? {
        if (getChannelList() == null || getChannelList()?.size ?: 0 == 0) {
            return null
        }
        if (mCurrentChannel == null) {
            mCurrentChannel = getChannelList()?.get(0)
        }
        getChannelList()?.let { channelList ->
            if (mCurrentChannel != null) {
                var position = channelList.indexOf(mCurrentChannel!!)
                if (position == -1) {
                    mCurrentChannel = channelList[0]
                    return mCurrentChannel
                } else {
                    var channel: TVChannel
                    if (position != 0) {
                        channel = channelList[position - 1]
                    } else {
                        channel = channelList.last()
                    }
                    mCurrentPlayProgram = channel.chIp.dvbParameter
                    mCurrentChannel = channel
                    return mCurrentChannel
                }
            } else {
                return mCurrentChannel
            }
        }
        return null
    }

    fun getCurrentChannel(): TVChannel? {
        if (mCurrentChannel == null) {
            Log.e(TAG,"[getCurrentChannel] mCurrentChannel == null")
            mCurrentChannel = getChannelList()?.get(0)
        }
        Log.e(TAG,"[getCurrentChannel] $mCurrentChannel")
        return mCurrentChannel
    }

    fun updateJVersionAndAppVersionToFile(appVersion: String = ""){
        var gson: Gson = Gson()

        Log.e(TAG, "[updateJVersionToFile] call.")
        sendTCPRequestSingle("j_ver")
            .subscribeOn(singelThreadScheduler)
            .observeOn(singelThreadScheduler)
            .subscribe({ successResult ->
                Log.e(TAG, "[updateJVersionToFile] Result : $successResult")
                var JVersion = gson.fromJson(successResult,JVersion::class.java)
                Log.e(TAG, "[updateJVersionToFile] JVersion Result : $JVersion")
                val config = gson.fromJson(MiscUtils.getJsonFromStorage("box_config.json"), Config::class.java)
                if(config!= null || config?.config != null){
                    config.config.j_version = "${JVersion.process}-${JVersion.ver}-${JVersion.build}-${JVersion.chanlistver}"
                    config.config.apk_version = appVersion

                    FileUtils.writeToFile(File("/data/$TAG_DEFAULT_LOCAL_PATH", "box_config.json"), gson.toJson(config))
                }
            }, { failResult ->
                Log.e(TAG, "[updateJVersionToFile] throwable : $failResult")
            })


    }

    fun sendTCPRequestSingle(msg: String): Single<String> {
        return Single.create { emitter: SingleEmitter<String> ->
            var serverHostname = "127.0.0.1"
            var serverPort = 1234

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
//            System.exit(1)
            } catch (e: IOException) {
                Log.e(TAG, "Couldn't get I/O for the connection to: $serverHostname")
                Log.e(TAG, "exception: $e")

                echoSocket = Socket(serverHostname, serverPort)
                out = PrintWriter(echoSocket.getOutputStream(), true)
                input = BufferedReader(InputStreamReader(echoSocket.getInputStream()))
//            System.exit(1)
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



//            Log.e(TAG, "command finish.")

            out?.close()
            input?.close()
            stdIn.close()
            echoSocket?.close()

//            Log.e(TAG, "resource close.")
            emitter.onSuccess(result ?: "noResponse")
        }
    }

    //------------------------------------------------------------------------------------------------------------------------

    private var mListeners: ArrayList<OnTVListener>? = ArrayList()

    fun registerListener(listener: OnTVListener) {
        Log.e(TAG, "[registerListener] call.")
        mListeners?.add(listener)
    }

    fun releaseListener(listener: OnTVListener) {
        Log.e(TAG, "[releaseListener] call.")
        if (mListeners?.contains(listener) == true) {
            mListeners?.remove(listener)
        }
    }

    fun onBroadcastAll(tvChannel: TVChannel?, action: ACTION_TYPE) {
        Log.e(TAG, "[onBroadcastAll] call.")
        mListeners?.let { listeners ->
            for (listener in listeners) {
                when (action) {
                    ACTION_TYPE.OnChannelChange -> {
                        listener.onChannelChange(tvChannel)
                    }
                    ACTION_TYPE.InitDeviceFinish -> {
                        listener.initDeviceFinish()
                    }
                    ACTION_TYPE.InitAVPlayerFinish -> {
                        listener.initAVPlayerFinish()
                    }
                    TVController.ACTION_TYPE.OnScanFinish -> {
                        listener.onScanFinish()
                    }
                }
            }
        }
    }

    enum class ACTION_TYPE {
        OnChannelChange,
        InitDeviceFinish,
        InitAVPlayerFinish,
        OnScanFinish
    }

    interface OnTVListener {
        fun onChannelChange(tvChannel: TVChannel?)
        fun initDeviceFinish()
        fun initAVPlayerFinish()
        fun onScanFinish()
    }
}