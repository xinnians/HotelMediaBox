package com.ufistudio.hotelmediabox.pages

import android.graphics.PixelFormat
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import com.ufistudio.hotelmediabox.DVBHelper
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.helper.TVController
import com.ufistudio.jnitest5
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_dvb_test.*

class DVBTestActivity : AppCompatActivity(), View.OnClickListener, AdapterView.OnItemSelectedListener {

//    lateinit var mVideoView : SurfaceView
//    lateinit var mViewChannelList : RecyclerView
//    lateinit var mBtnInitDev : Button
//    lateinit var mBtnInitAVPlayer : Button
//    lateinit var mBtnPlay : Button
//    lateinit var mBtnDeInitAVPlayer : Button
//    lateinit var mBtnDeInitDev : Button

    private val TAG:String = DVBTestActivity::class.java.simpleName

    var list_of_items = arrayOf(
        "fa1 fa2 4 1",
        "fab fac 4 1",
        "fb5 fb6 0 3",
        "fc0 fbf 0 3",
        "3e9 3ea 4 1",
        "3f3 3f4 4 1",
        "3fd 3fe 0 3",
        "407 408 0 3"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dvb_test)

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                holder?.setFormat(PixelFormat.TRANSPARENT)
            }
        })


        // Create an ArrayAdapter using a simple spinner layout and languages array
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, list_of_items)
        // Set layout to use when the list of choices appear
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        viewChannelList.adapter = aa
        viewChannelList.onItemSelectedListener = this

        btnInitDevice.setOnClickListener(this)
        btnInitVO.setOnClickListener(this)
        btnInitAVPlayer.setOnClickListener(this)
        btnSetChnl.setOnClickListener(this)
        btnPlay.setOnClickListener(this)
        btnDeInitAVPlayer.setOnClickListener(this)
        btnDeInitDevice.setOnClickListener(this)

    }

//    vcodec: 0-mpeg2, 1-mpeg4, 4-h264
//    acodec: 0-pcm, 1-aac, 2-mp2, 3-mp3

    override fun onClick(v: View?) {
        when (v?.id) {
            btnInitDevice.id ->
//                DVBHelper.getDVBPlayer().initPlayer(600, 600, 0, 0)
//                Single.just(true)
//                    .map { DVBHelper.getDVBPlayer().initDevice() }
//                    .subscribeOn(Schedulers.io())
//                    .subscribe()
                TVController.initDevice()
            btnInitVO.id ->
//                Single.just(true)
//                    .map { DVBHelper.getDVBPlayer().setVO(600, 600, 0, 0) }
//                    .subscribeOn(Schedulers.io())
//                    .subscribe()
                TVController.initAVPlayer(TVController.SCREEN_TYPE.CHANNELPAGE)
            btnInitAVPlayer.id -> {
//                Single.just(true)
//                    .map { DVBHelper.getDVBPlayer().setAVPlayer() }
//                    .subscribeOn(Schedulers.io())
//                    .subscribe()
            }
            btnSetChnl.id -> {
//                Single.just(true)
//                    .map { DVBHelper.getDVBPlayer().scanChannel("581000 6000") }
//                    .subscribeOn(Schedulers.io())
//                    .subscribe({result -> Log.e(TAG,"[scanChannel result] = $result")},{})
            }
            btnPlay.id ->
                DVBHelper.getDVBPlayer().playChannel("fa1 fa2 4 1")
//                jnitest5.SetNPlay("581000 6000","fa1 fa2 4 1")
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.io())
//                .subscribe()
            btnDeInitAVPlayer.id ->
                Single.just(true)
                    .map { DVBHelper.getDVBPlayer().releaseAVPlayer() }
                    .subscribeOn(Schedulers.io())
                    .subscribe()
            btnDeInitDevice.id -> {
                Single.just(true)
                    .map { DVBHelper.getDVBPlayer().releaseDev() }
                    .subscribeOn(Schedulers.io())
                    .subscribe()
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
//            0 -> jnitest5.SetNPlay("581000 6000" , list_of_items[0])
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.io())
//                .subscribe()
//            1 -> Single.just(true)
//                .map { DVBHelper.getDVBPlayer().scanChannel("581000 6000") }
//                .map { DVBHelper.getDVBPlayer().playChannel(list_of_items[1]) }
//                .subscribeOn(Schedulers.io())
//                .subscribe()
//            2 -> Single.just(true)
//                .map { DVBHelper.getDVBPlayer().scanChannel("581000 6000") }
//                .map { DVBHelper.getDVBPlayer().playChannel(list_of_items[2]) }
//                .subscribeOn(Schedulers.io())
//                .subscribe()
//            3 -> Single.just(true)
//                .map { DVBHelper.getDVBPlayer().scanChannel("581000 6000") }
//                .map { DVBHelper.getDVBPlayer().playChannel(list_of_items[3]) }
//                .subscribeOn(Schedulers.io())
//                .subscribe()
//            4 -> Single.just(true)
//                .map { DVBHelper.getDVBPlayer().scanChannel("533000 6000") }
//                .map { DVBHelper.getDVBPlayer().playChannel(list_of_items[4]) }
//                .subscribeOn(Schedulers.io())
//                .subscribe()
//            5 -> Single.just(true)
//                .map { DVBHelper.getDVBPlayer().scanChannel("533000 6000") }
//                .map { DVBHelper.getDVBPlayer().playChannel(list_of_items[5]) }
//                .subscribeOn(Schedulers.io())
//                .subscribe()
//            6 -> Single.just(true)
//                .map { DVBHelper.getDVBPlayer().scanChannel("533000 6000") }
//                .map { DVBHelper.getDVBPlayer().playChannel(list_of_items[6]) }
//                .subscribeOn(Schedulers.io())
//                .subscribe()
//            7 -> Single.just(true)
//                .map { DVBHelper.getDVBPlayer().scanChannel("533000 6000") }
//                .map { DVBHelper.getDVBPlayer().playChannel(list_of_items[7]) }
//                .subscribeOn(Schedulers.io())
//                .subscribe()
        }
    }

}