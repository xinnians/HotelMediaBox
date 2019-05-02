package com.ufistudio.hotelmediabox.pages.home

import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import kotlinx.android.synthetic.main.fragment_home.*
import android.view.KeyEvent
import android.view.View.FOCUS_LEFT
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.helper.TVHelper
import com.ufistudio.hotelmediabox.interfaces.ViewModelsCallback
import com.ufistudio.hotelmediabox.pages.factory.FactoryActivity
import com.ufistudio.hotelmediabox.pages.fullScreen.FullScreenActivity
import com.ufistudio.hotelmediabox.repository.data.Home
import com.ufistudio.hotelmediabox.repository.data.HomeIcons
import com.ufistudio.hotelmediabox.repository.data.TVChannel
import com.ufistudio.hotelmediabox.repository.data.HomeWeather
import com.ufistudio.hotelmediabox.utils.FileUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class HomeFragment : InteractionView<OnPageInteractionListener.Primary>(), FunctionsAdapter.OnItemClickListener,
        ViewModelsCallback {
    private val TAG_TYPE_1 = 1//Weather Information
    private val TAG_TYPE_2 = 2//Promo Banner

    private lateinit var mViewModel: HomeViewModel
    private var mAdapter = FunctionsAdapter()

    private lateinit var mExoPlayerHelper: ExoPlayerHelper

    private var mData: Home? = null
    private var mChannelIndex = 0
    private var mFeatureIcons: ArrayList<HomeIcons>? = null
    private var mIsRendered: Boolean = false //判斷塞資料了沒

    private var mChannelList: ArrayList<TVChannel>? = null
    private var mPlayPosition = 0
    private var mDisposable: Disposable? = null

    private var mViewChannelName: TextView? = null
    private var mViewChannelLogo: ImageView? = null
    private var mTVChannel: TVChannel? = null

    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
        private val TAG = HomeFragment::class.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        mTestUdpList.add("udp://239.1.1.1:3990")

        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initHomeProgress.observe(this, Observer {

        })
        mViewModel.initHomeSuccess.observe(this, Observer {
            onSuccess(it)
        })
        mViewModel.initHomeError.observe(this, Observer { onError(it) })

        mExoPlayerHelper = ExoPlayerHelper()
    }

    override fun onStart() {
        super.onStart()
        dvbView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                holder?.setFormat(PixelFormat.TRANSPARENT)
            }
        })

        list_functions.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        list_functions.adapter = mAdapter

        mExoPlayerHelper.initPlayer(context, videoView)
//        mExoPlayerHelper.setUdpSource(mTestUdpList.get(mChannelIndex))
//        mExoPlayerHelper.setMp4Source(R.raw.videoplayback)

        videoView.setOnClickListener {
            //            mExoPlayerHelper.fullScreen()
            startActivity(Intent(context, FullScreenActivity::class.java))

        }
        mAdapter.setItemClickListener(this)
        renderView()
    }

    override fun onResume() {
        super.onResume()
        mViewModel.getTVHelper().initAVPlayer(TVHelper.SCREEN_TYPE.HOMEPAGE)
        mViewModel.getTVHelper().playCurrent()?.observeOn(AndroidSchedulers.mainThread())?.subscribe({

            mViewModel.getTVHelper().getCurrentChannel()?.let { tvChannel ->
                mViewChannelName?.text = tvChannel.chNum + " " + tvChannel.chName
                mViewChannelLogo?.let { viewLogo ->
                    Glide.with(this)
                            .load(FileUtils.getFileFromStorage(tvChannel.chLogo.fileName))
                            .skipMemoryCache(true)
                            .into(viewLogo)
                }

            }

        }, {})
    }

    override fun onPause() {
        super.onPause()
        mViewModel.getTVHelper().closeAVPlayer()
    }

    override fun onStop() {
        super.onStop()
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable?.dispose()
        }
        mExoPlayerHelper.release()
        mIsRendered = false
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_CHANNEL_UP -> {

                mTVChannel = mViewModel.getTVHelper().chooseUp()
                mViewChannelName?.text = mTVChannel?.chNum + " " + mTVChannel?.chName
                mViewChannelLogo?.let { viewLogo ->
                    Glide.with(this)
                            .load(FileUtils.getFileFromStorage(mTVChannel?.chLogo?.fileName ?: ""))
                            .skipMemoryCache(true)
                            .into(viewLogo)
                }
                setPlayTimer()


//                mViewModel.getTVHelper().playUp()?.observeOn(AndroidSchedulers.mainThread())?.subscribe({
//                    mViewModel.getTVHelper().getCurrentChannel()?.let { tvChannel ->
//                        mViewChannelName?.text = tvChannel.chNum + " " + tvChannel.chName
//                        mViewChannelLogo?.let { viewLogo ->
//                            Glide.with(this)
//                                .load(FileUtils.getFileFromStorage(tvChannel.chLogo.fileName))
//                                .into(viewLogo)
//                        }
//
//                    }
//
//                }, {})
                return true
            }
            KeyEvent.KEYCODE_CHANNEL_DOWN -> {

                mTVChannel = mViewModel.getTVHelper().chooseDown()
                mViewChannelName?.text = mTVChannel?.chNum + " " + mTVChannel?.chName
                mViewChannelLogo?.let { viewLogo ->
                    Glide.with(this)
                            .load(FileUtils.getFileFromStorage(mTVChannel?.chLogo?.fileName ?: ""))
                            .skipMemoryCache(true)
                            .into(viewLogo)
                }
                setPlayTimer()

//                mViewModel.getTVHelper().playDown()?.observeOn(AndroidSchedulers.mainThread())?.subscribe({
//                    mViewModel.getTVHelper().getCurrentChannel()?.let { tvChannel ->
//                        mViewChannelName?.text = tvChannel.chNum + " " + tvChannel.chName
//                        mViewChannelLogo?.let { viewLogo ->
//                            Glide.with(this)
//                                .load(FileUtils.getFileFromStorage(tvChannel.chLogo.fileName))
//                                .into(viewLogo)
//                        }
//
//                    }
//
//                }, {})

                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER -> {
//                mExoPlayerHelper.fullScreen()
                startActivity(Intent(context, FullScreenActivity::class.java))
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                if (mExoPlayerHelper.isFullscreen())
                    mExoPlayerHelper.fullScreen()
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
            }
        }
        return false
    }

    override fun onClick(view: View) {
        if (view.tag as Int == -100) {
            Toast.makeText(context, "尚未實作", Toast.LENGTH_SHORT).show()
            return
        }
        val b = Bundle()
        b.putParcelableArrayList(Page.ARG_BUNDLE, mFeatureIcons)
        getInteractionListener().switchPage(R.id.fragment_container, view.tag as Int, b, true, false, true)
    }

    override fun onSuccess(it: Any?) {
        mData = it as Home?
        mFeatureIcons = mData?.home?.icons

        //for setting page reset language use
        if (activity?.intent?.extras?.getBoolean(Page.ARG_BUNDLE) != null && activity?.intent?.extras?.getBoolean(Page.ARG_BUNDLE)!!) {
            val b: Bundle = Bundle()
            b.putParcelableArrayList(Page.ARG_BUNDLE, mFeatureIcons)
            getInteractionListener().switchPage(R.id.fragment_container, Page.SETTING, b, true, false, true)
            return
        }
        renderView()
    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "onError => $t")
    }

    override fun onProgress(b: Boolean) {
    }

    /**
     * 塞資料
     */
    private fun renderView() {
        if (!mIsRendered) {
            if (mData?.home?.stage_type?.type != null && mFeatureIcons != null) {
                mIsRendered = true
                switchWedge(mData?.home?.stage_type?.type)
                mAdapter.setData(mFeatureIcons)
            }
        }

        list_functions?.requestFocus(FOCUS_LEFT)
    }

    /**
     * 切換天氣or廣告欄
     * @type: TAG_TYPE_1:天氣
     *        TAG_TYPE_2:廣告欄
     */
    private fun switchWedge(type: Int?) {
        if (view_wedge == null) return
        when (type) {
            TAG_TYPE_1 -> {
                view_wedge.layoutResource = R.layout.view_home_weather
                val view = view_wedge.inflate()
                val weather: HomeWeather? = mData?.home?.weather

                val textViewWifiId: TextView = view.findViewById<TextView>(R.id.textView_wifi_id)
                textViewWifiId.text = weather?.wifi_id
                val textViewWifiIdTitle: TextView = view.findViewById<TextView>(R.id.textView_wifiIdTitle)
                textViewWifiIdTitle.text = weather?.wifi_id_title
                val textViewWifiIdPassword: TextView = view.findViewById<TextView>(R.id.textView_wifi_password)
                textViewWifiIdPassword.text = weather?.wifi_password
                val textViewWifiPasswordTitle: TextView = view.findViewById<TextView>(R.id.textView_wifiPasswordTitle)
                textViewWifiPasswordTitle.text = weather?.wifi_password_title
                val textViewValue: TextView = view.findViewById<TextView>(R.id.textView_value)
                textViewValue.text = weather?.weather_value
                val textViewWeatherTitle: TextView = view.findViewById<TextView>(R.id.weather_title)
                textViewWeatherTitle.text = weather?.weather_title
                when (weather?.weather_type) {
                    "1" -> {
                        Glide.with(this)
                                .load(R.drawable.ic_weather_1)
                                .skipMemoryCache(true)
                                .into(view.findViewById(R.id.imageView))
                    }
                    "2" -> {
                        Glide.with(this)
                                .load(R.drawable.ic_weather_cloudy)
                                .skipMemoryCache(true)
                                .into(view.findViewById(R.id.imageView))
                    }
                    "3" -> {
                        Glide.with(this)
                                .load(R.drawable.ic_weather_partlycloudy)
                                .skipMemoryCache(true)
                                .into(view.findViewById(R.id.imageView))
                    }
                    "4" -> {
                        Glide.with(this)
                                .load(R.drawable.ic_weather_raining)
                                .skipMemoryCache(true)
                                .into(view.findViewById(R.id.imageView))
                    }
                    "5" -> {
                        Glide.with(this)
                                .load(R.drawable.ic_weather_shower)
                                .skipMemoryCache(true)
                                .into(view.findViewById(R.id.imageView))
                    }
                    "6" -> {
                        Glide.with(this)
                                .load(R.drawable.ic_weather_sunny)
                                .skipMemoryCache(true)
                                .into(view.findViewById(R.id.imageView))
                    }
                }
            }
            TAG_TYPE_2 -> {
                view_wedge.layoutResource = R.layout.view_home_banner
                val view = view_wedge.inflate()

                mViewChannelName = view.findViewById(R.id.text_channel)
                mViewChannelLogo = view.findViewById(R.id.image_channel)

                Glide.with(this)
                        .load(FileUtils.getFileFromStorage(mData?.home?.promo_banner!![0].image))
                        .skipMemoryCache(true)
                        .into(view.findViewById(R.id.image_banner))
            }
        }
    }

    private fun initChannelsSuccess(list: ArrayList<TVChannel>) {
        mChannelList = list
//        playTv("")
    }

    private fun initChannelsProgress(isProgress: Boolean) {
        Log.e(TAG, "initChannelsProgress call. isProgress:$isProgress")
    }

    private fun initChannelsError(throwable: Throwable) {
        Log.e(TAG, "initChannelsError call. ${throwable.message}")
    }

    private fun setPlayTimer() {
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable?.dispose()
        }

        mDisposable = Observable.timer(400, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {}, { onError -> Log.e(TAG, "error:$onError") }, {
                    mViewModel.getTVHelper().playCurrent()
                            ?.subscribe()
                })
    }
}