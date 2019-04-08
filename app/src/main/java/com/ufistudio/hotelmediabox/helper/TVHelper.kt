package com.ufistudio.hotelmediabox.helper

import android.annotation.SuppressLint
import android.util.Log
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.DVBHelper
import com.ufistudio.hotelmediabox.repository.data.TVChannel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class TVHelper {

    private val TAG: String = TVHelper::class.java.simpleName

    //鎖定頻點
    private var mCurrentLockFrequency: String = ""
    //當前頻點播放頻道
    private var mCurrentPlayProgram: String = ""
    private var mCurrentChannel: TVChannel? = null
    private var mCurrentScreenType: SCREEN_TYPE = SCREEN_TYPE.HOMEPAGE
    //頻道列表
    private var mChannelList: ArrayList<TVChannel>? = null

    private var mIsAVPlayerInit = false
    private var mIsDeviceInit = false

    enum class SCREEN_TYPE(width: Int, height: Int, x: Int, y: Int) {
        HOMEPAGE(832, 464, 108, 72),
        CHANNELPAGE(980, 555, 857, 233),
        FULLSCREEN(0, 0, 0, 0)
    }

    @Synchronized
    fun getChannelList(): ArrayList<TVChannel>? {
        Log.i(TAG, "[TVHelper] getChannelList call")
        if (mChannelList == null || mChannelList?.size ?: 0 == 0) {
            val jsonObject: Array<TVChannel> =
                Gson().fromJson(MiscUtils.getJsonFromStorage("channels.json"), Array<TVChannel>::class.java)
                    ?: return null
            mChannelList = jsonObject.toCollection(ArrayList())
        }
        Log.i(TAG, "[TVHelper] getChannelList size = ${mChannelList?.size ?: "null"}")
        return mChannelList
    }

    @Synchronized
    fun initDevice() {
        Log.i(TAG, "[TVHelper] initDevice call")
        DVBHelper.getDVBPlayer().initDevice()
        mIsDeviceInit = true
    }

    @Synchronized
    fun closeDevice() {
        Log.i(TAG, "[TVHelper] closeDevice call")
        DVBHelper.getDVBPlayer().releaseDev()
        mIsDeviceInit = false
    }

    @SuppressLint("CheckResult")
//    fun initAVPlayer(width: Int, height: Int, x: Int, y: Int) {
    @Synchronized
    fun initAVPlayer(screenType: SCREEN_TYPE) {
        Log.i(TAG, "[TVHelper] initAVPlayer call isDeviceInit? = $mIsDeviceInit")
        if (!mIsDeviceInit) {
            initDevice()
        }
        mCurrentScreenType = screenType
        var width = 0
        var height = 0
        var x = 0
        var y = 0
        when (screenType) {
            SCREEN_TYPE.HOMEPAGE -> {
                width = 832
                height = 464
                x = 108
                y = 72
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

        Single.just(true)
            .map { DVBHelper.getDVBPlayer().releaseAVPlayer() }
            .map { DVBHelper.getDVBPlayer().setVO(width, height, x, y) }
            .map { DVBHelper.getDVBPlayer().setAVPlayer() }
            .subscribeOn(Schedulers.io())
            .subscribe({ result -> mIsAVPlayerInit = result == 0 },
                { throwable -> Log.i(TAG, "[initAVPlayer throwable] = $throwable") })
    }

    @Synchronized
    fun closeAVPlayer() {
        Log.i(TAG, "[TVHelper] closeAVPlayer call")
//        Single.just(true)
//            .map { DVBHelper.getDVBPlayer().releaseAVPlayer() }
//            .subscribeOn(Schedulers.newThread())
//            .subscribe({ result -> mIsAVPlayerInit = false },
//                { throwable -> Log.i(TAG, "[closeAVPlayer throwable] = $throwable") })
        DVBHelper.getDVBPlayer().releaseAVPlayer()
        mIsAVPlayerInit = false
    }

    @Synchronized
    fun reInit(): Single<Int> {
        Log.i(TAG, "[TVHelper] reInit call")
        return Single.just(true)
            .map { DVBHelper.getDVBPlayer().releaseAVPlayer() }
            .map { closeDevice() }
            .map { initDevice() }
            .map {
                var width = 0
                var height = 0
                var x = 0
                var y = 0
                when (mCurrentScreenType) {
                    SCREEN_TYPE.HOMEPAGE -> {
                        width = 832
                        height = 464
                        x = 108
                        y = 72
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
                DVBHelper.getDVBPlayer().setVO(width, height, x, y)
            }
            .map { DVBHelper.getDVBPlayer().setAVPlayer() }
            .map { result ->
                mIsAVPlayerInit = result == 0
                return@map result
            }
            .subscribeOn(Schedulers.io())
    }

    @Synchronized
    fun reInitAVPlayer(): Single<Int> {
        Log.i(TAG, "[TVHelper] reInitAVPlayer call")
        return Single.just(true)
            .map { DVBHelper.getDVBPlayer().releaseAVPlayer() }
            .map {
                var width = 0
                var height = 0
                var x = 0
                var y = 0
                when (mCurrentScreenType) {
                    SCREEN_TYPE.HOMEPAGE -> {
                        width = 832
                        height = 464
                        x = 108
                        y = 72
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
                DVBHelper.getDVBPlayer().setVO(width, height, x, y)

            }
            .map { DVBHelper.getDVBPlayer().setAVPlayer() }
            .map { result ->
                mIsAVPlayerInit = result == 0
                return@map result
            }
            .subscribeOn(Schedulers.io())
    }

    @Synchronized
    fun lockFrequency(parameter: String): Single<Int> {
        Log.i(TAG, "[TVHelper] lockFrequency call")
        return Single.just(true)
            .map {
                if (mCurrentLockFrequency == parameter) return@map 0
                else DVBHelper.getDVBPlayer().scanChannel(parameter)
            }
            .map { result ->
                if (result == 0) mCurrentLockFrequency = parameter
                else {
                    mCurrentLockFrequency = ""
//                    closeAVPlayer()
//                    closeDevice()
//                    initDevice()
//                    initAVPlayer(mCurrentScreenType)
//                    return@map DVBHelper.getDVBPlayer().scanChannel(parameter)
                }
                Log.i(TAG, "[TVHelper] lockFrequency result mCurrentLockFrequency try1 = $mCurrentLockFrequency")
                return@map result
            }
            .flatMap { result ->
                if (result == 0) {
                    return@flatMap Single.just(3)
                } else {
                    return@flatMap reInit()
                }
            }
            .map { result ->
                if (result == 3) {
                    mCurrentLockFrequency = parameter
                    return@map 0
                } else {
                    var scanResult = DVBHelper.getDVBPlayer().scanChannel(parameter)
                    if (scanResult == 0) mCurrentLockFrequency = parameter
                    else {
                        mCurrentLockFrequency = ""
                    }
                    Log.i(TAG, "[TVHelper] lockFrequency result mCurrentLockFrequency try2 = $mCurrentLockFrequency")
                    return@map scanResult
                }
            }
            .subscribeOn(Schedulers.io())
    }

    @Synchronized
    fun play(channel: TVChannel): Single<Int> {
        Log.i(TAG, "[TVHelper] play call")
        if (getChannelList() == null || getChannelList()?.size ?: 0 == 0) {
            Log.i(TAG, "[TVHelper] play channelList null.")
            return Single.just(1)
        }
        Log.i(TAG, "[TVHelper] play return single.")
        return Single.just(true)
            .flatMap { lockFrequency(channel.chIp.frequency + " " + channel.chIp.bandwidth) }
            .flatMap { result ->
                return@flatMap reInitAVPlayer()
            }.map { result ->
                var playresult = DVBHelper.getDVBPlayer().playChannel(channel.chIp.dvbParameter)
                Log.i(TAG, "[TVHelper] play result = play retry playresult = $playresult")
                mCurrentChannel = channel
                return@map playresult
            }.flatMap { result ->
                if (result == 0) {
                    return@flatMap Single.just(result)
                } else {
                    Log.i(TAG, "[TVHelper] play result = 走到initDev的地方了")
                    mCurrentLockFrequency = ""
                    return@flatMap reInit().flatMap { lockFrequency(channel.chIp.frequency + " " + channel.chIp.bandwidth) }
                        .map { result ->
                            DVBHelper.getDVBPlayer().playChannel(channel.chIp.dvbParameter)
                            return@map result
                        }

                }
            }
            .subscribeOn(Schedulers.io())
//        return Single.just(true)
//            .flatMap { lockFrequency(channel.chIp.frequency + " " + channel.chIp.bandwidth) }
//            .map { result ->
//                Log.i(TAG, "[TVHelper] play result = $result")
//                if (result == 0) {
//                    Log.i(TAG, "[TVHelper] play result = lockFrequency success")
//                    var playResult = DVBHelper.getDVBPlayer().playChannel(channel.chIp.dvbParameter)
//                    mCurrentChannel = channel
//                    return@map playResult
//                } else {
//                    Log.i(TAG, "[TVHelper] play result = lockFrequency fail")
//                    mCurrentChannel = channel
//                    return@map 0
//                }
//
//            }
//            .flatMap { result ->
//                if (result == 0) {
//                    Log.i(TAG, "[TVHelper] play result = play success")
//                    return@flatMap Single.just(3)
//                } else {
//                    Log.i(TAG, "[TVHelper] play result = play fail")
//                    return@flatMap reInitAVPlayer()
//                }
//            }.map { result ->
//                if(result == 3){
//
//                }else{
//                    Log.i(TAG, "[TVHelper] play result = play retry")
//                    DVBHelper.getDVBPlayer().playChannel(channel.chIp.dvbParameter)
//                }
//                return@map result
//            }
//            .subscribeOn(Schedulers.io())
    }

    @Synchronized
    fun playCurrent(): Single<Int>? {
        Log.i(TAG, "[TVHelper] playCurrent call")
        if (getChannelList() == null || getChannelList()?.size ?: 0 == 0) {
            return null
        }
        if (mCurrentChannel == null) {
            mCurrentChannel = getChannelList()?.get(0)
        }
        mCurrentChannel?.let { tvChannel ->
            //            play(tvChannel).subscribe({ mCurrentPlayProgram = tvChannel.chIp.dvbParameter }, {})
            mCurrentPlayProgram = tvChannel.chIp.dvbParameter
            return play(tvChannel)
                .subscribeOn(Schedulers.io())
        }

        return null
    }

    @Synchronized
    fun playUp(): Single<Int>? {
        Log.i(TAG, "[TVHelper] playUp call")
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
                    return playCurrent()
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
                    return play(channel)
                        .subscribeOn(Schedulers.io())
                }
            } else {
                return playCurrent()
            }
        }

        return null
    }

    @Synchronized
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

    @Synchronized
    fun playDown(): Single<Int>? {
        Log.i(TAG, "[TVHelper] playDown call")
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
                    return playCurrent()
                } else {
                    var channel: TVChannel
                    if (position != 0) {
                        channel = channelList[position - 1]
//                        play(channel).subscribe({ mCurrentPlayProgram = channel.chIp.dvbParameter }, {})
                    } else {
                        channel = channelList.last()
//                        play(channel).subscribe({ mCurrentPlayProgram = channel.chIp.dvbParameter }, {})
                    }
                    mCurrentPlayProgram = channel.chIp.dvbParameter
                    return play(channel)
                        .subscribeOn(Schedulers.io())
                }
            } else {
                return playCurrent()
            }
        }

        return null
    }

    @Synchronized
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
//                        play(channel).subscribe({ mCurrentPlayProgram = channel.chIp.dvbParameter }, {})
                    } else {
                        channel = channelList.last()
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

    @Synchronized
    fun getCurrentChannel(): TVChannel? {
        return mCurrentChannel
    }


}