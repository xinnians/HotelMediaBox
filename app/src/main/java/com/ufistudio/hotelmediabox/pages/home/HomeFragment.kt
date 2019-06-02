package com.ufistudio.hotelmediabox.pages.home

import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.PixelFormat
import android.opengl.Visibility
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v4.view.ViewPager
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
import com.ufistudio.hotelmediabox.helper.TVController
import com.ufistudio.hotelmediabox.interfaces.ViewModelsCallback
import com.ufistudio.hotelmediabox.pages.fullScreen.FullScreenActivity
import com.ufistudio.hotelmediabox.pages.weather.WeatherIconEnum
import com.ufistudio.hotelmediabox.repository.data.*
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.utils.MiscUtils
import com.ufistudio.hotelmediabox.utils.XTNetWorkManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.view_home_banner.*
import kotlinx.android.synthetic.main.view_home_weather.*
import java.io.File
import java.util.concurrent.TimeUnit

class HomeFragment : InteractionView<OnPageInteractionListener.Primary>(), FunctionsAdapter.OnItemClickListener,
    ViewModelsCallback {
    private val TAG_TYPE_1 = 1//Weather Information
    private val TAG_TYPE_2 = 2//Promo Banner

    private val TAG_ENABLE: Int = 1
    private val TAG_DISABLE: Int = 0

    private lateinit var mViewModel: HomeViewModel
    private var mAdapter = FunctionsAdapter()

    private lateinit var mExoPlayerHelper: ExoPlayerHelper

    private var mData: Home? = null
    private var mWeatherData: WeatherInfo? = null
    private var mChannelIndex = 0
    private var mFeatureIcons: ArrayList<HomeIcons>? = null
    private var mIsRendered: Boolean = false //判斷塞資料了沒

    private var mChannelList: ArrayList<TVChannel>? = null
    private var mPlayPosition = 0
    private var mDisposable: Disposable? = null

    private var mViewChannelName: TextView? = null
    private var mViewChannelLogo: ImageView? = null
    private var mTVChannel: TVChannel? = null

    private var mFeatureList: HashMap<Int, ArrayList<HomeFeatureEnum>> =
        HashMap() //所有的Feature列表 Int:第幾列, ArrayList<HomeFeatureEnum>:每一頁的Icon
    private var mFeatureCurrentList: ArrayList<HomeFeatureEnum> = ArrayList()      //當前的Feature列表
    private var mFeatureIconList: HashMap<Int, ArrayList<HomeIcons>> = HashMap() //Home Icon相關資料，基本上跟著mFeatureList走
    private var mFeatureIconCurrentList: ArrayList<HomeIcons> =
        ArrayList()      //當前的Home Icon相關資料，基本上跟著mFeatureCurrentList走
    private var mCurrentPosition: Int = 0                                        //當前的Feature在第幾頁
    private var mBannerList: ArrayList<ImageView>? = ArrayList()
    private var mBannerAdapter: BannerAdapter? = null
    private var mBannerDisposable: Disposable? = null

    private var mTVListener: TVController.OnTVListener = object : TVController.OnTVListener {
        override fun onScanFinish() {

        }

        override fun onChannelChange(tvChannel: TVChannel?) {
        }

        override fun initDeviceFinish() {
        }

        override fun initAVPlayerFinish() {
            TVController.playCurrent()
        }

    }

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

        mViewModel.getWeatherInfoProgress.observe(this, Observer { onProgress(it!!) })
        mViewModel.getWeatherInfoSuccess.observe(this, Observer { onSuccess(it!!) })
        mViewModel.getWeatherInfoError.observe(this, Observer { onError(it!!) })

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

//        list_functions.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//        list_functions.adapter = mAdapter

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
        mTVChannel = TVController.getCurrentChannel()
        text_channel?.text = mTVChannel?.chNum + " " + mTVChannel?.chName
        image_channel?.let { viewLogo ->

            Log.e("Ian", "mTVChannel?.chLogo?.normalIconName : ${mTVChannel?.chLogo?.normalIconName.toString()}")
            Glide.with(this)
                .load(
                    FileUtils.getFileFromStorage(
                        mTVChannel?.chLogo?.normalIconName
                            ?: ""
                    )
                )
                .skipMemoryCache(true)
                .into(viewLogo)
        }

        TVController.registerListener(mTVListener)
        TVController.initAVPlayer(TVController.SCREEN_TYPE.HOMEPAGE)
    }

    override fun onPause() {
        super.onPause()
        TVController.releaseListener(mTVListener)
        TVController.deInitAVPlayer()
    }

    override fun onStop() {
        super.onStop()
        if (mBannerDisposable != null && !mBannerDisposable!!.isDisposed) {
            mBannerDisposable?.dispose()
        }
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable?.dispose()
        }
        mExoPlayerHelper.release()
        mIsRendered = false
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_CHANNEL_UP -> {

                mTVChannel = TVController.chooseUp()
                text_channel?.text = mTVChannel?.chNum + " " + mTVChannel?.chName
                image_channel?.let { viewLogo ->
                    Log.e(
                        "Ian",
                        "mTVChannel?.chLogo?.normalIconName : ${mTVChannel?.chLogo?.normalIconName.toString()}"
                    )
                    Glide.with(this)
                        .load(
                            FileUtils.getFileFromStorage(
                                mTVChannel?.chLogo?.normalIconName
                                    ?: ""
                            )
                        )
                        .skipMemoryCache(true)
                        .into(viewLogo)
                }
                setPlayTimer()
                return true
            }
            KeyEvent.KEYCODE_CHANNEL_DOWN -> {

//                mTVChannel = mViewModel.getTVHelper().chooseDown()
                mTVChannel = TVController.chooseDown()
                text_channel?.text = mTVChannel?.chNum + " " + mTVChannel?.chName
                image_channel?.let { viewLogo ->
                    Log.e(
                        "Ian",
                        "mTVChannel?.chLogo?.normalIconName : ${mTVChannel?.chLogo?.normalIconName.toString()}"
                    )
                    Glide.with(this)
                        .load(
                            FileUtils.getFileFromStorage(
                                mTVChannel?.chLogo?.normalIconName
                                    ?: ""
                            )
                        )
                        .skipMemoryCache(true)
                        .into(viewLogo)
                }
                setPlayTimer()

                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER -> {
//                mExoPlayerHelper.fullScreen()
                clickChange()
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                if (mExoPlayerHelper.isFullscreen())
                    mExoPlayerHelper.fullScreen()
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (focusItem != 0) {
                    focusItem = 0
                    focusChange()
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (focusItem == 0) {
                    focusItem = 1
                    focusChange()
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (focusItem == 1 && mCurrentPosition != 0) {
                    focusItem = 5
                    mCurrentPosition--
                    mFeatureCurrentList = mFeatureList[mCurrentPosition]!!
                    mFeatureIconCurrentList = mFeatureIconList[mCurrentPosition]!!
                    changeFeatureInfo()

                } else {
                    if (focusItem != 1)
                        focusItem--
                }
                focusChange()
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                //判斷不是最後一列的最後一筆
                if (mCurrentPosition == mFeatureList.size - 1 && focusItem == mFeatureCurrentList.size) {
                    return true
                }

                //判斷item是最後一筆，且還有下一頁，則跳到第一個focus
                if (focusItem == 5 && mCurrentPosition != mFeatureList.size - 1) {
                    focusItem = 1
                    mCurrentPosition++
                    mFeatureCurrentList = mFeatureList[mCurrentPosition]!!
                    mFeatureIconCurrentList = mFeatureIconList[mCurrentPosition]!!
                    changeFeatureInfo()
                } else {
                    focusItem++
                }

                focusChange()
                return true
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
        if (it != null) {
            if (it is Home) {
                mData = it
                mFeatureIcons = mData?.home?.icons

                //for setting page reset language use
                if (activity?.intent?.extras?.getBoolean(Page.ARG_BUNDLE) != null && activity?.intent?.extras?.getBoolean(
                                Page.ARG_BUNDLE
                        )!!
                ) {
                    val b: Bundle = Bundle()
                    b.putParcelableArrayList(Page.ARG_BUNDLE, mFeatureIcons)
                    getInteractionListener().switchPage(R.id.fragment_container, Page.SETTING, b, true, false, true)
                    return
                }
                renderView()
            } else if (it is WeatherInfo) {
                mWeatherData = it
                switchWedge(TAG_TYPE_1)
            }
        }
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
//                mAdapter.setData(mFeatureIcons)
                mFeatureIcons?.let {
                    var index = 0
                    for (item in it) {
                        if (item.enable == TAG_ENABLE) {
                            val enumItem = HomeFeatureEnum.findItemById(item.id)
                            enumItem?.let {
                                mFeatureCurrentList.add(enumItem)
                                mFeatureIconCurrentList.add(item)

                                Log.d("neo", "index  = $index")
                                mFeatureList[index] = mFeatureCurrentList
                                mFeatureIconList[index] = mFeatureIconCurrentList
                                if (mFeatureCurrentList.size == 5) {
                                    mFeatureCurrentList = ArrayList()
                                    mFeatureIconCurrentList = ArrayList()
                                    index = index + 1
                                }
                            }
                        }
                    }
                    mFeatureCurrentList = mFeatureList[mCurrentPosition]!!
                    mFeatureIconCurrentList = mFeatureIconList[mCurrentPosition]!!
                }


                Log.d("neo", "mFeatureList = ${mFeatureList.size}")
                changeFeatureInfo()


            }
        }

//        list_functions?.requestFocus(FOCUS_LEFT)

        focusChange()
//        layout_frame2?.requestFocus()
//        layout_frame3?.requestFocus()
//        layout_frame4?.requestFocus()
//        layout_frame5?.requestFocus()
//        layout_frame1?.requestFocus()
//        videoView_frame?.requestFocus()
    }

    fun changeFeatureInfo() {
        if (mFeatureCurrentList.size > 0) {
            layout_frame1.visibility = View.VISIBLE
            image_icon.background = ContextCompat.getDrawable(context!!, mFeatureCurrentList[0].icon)
            text_title.text = mFeatureIconCurrentList[0].name
        } else {
            layout_frame1.visibility = View.GONE
        }
        if (mFeatureCurrentList.size > 1) {
            layout_frame2.visibility = View.VISIBLE
            image_icon2.background = ContextCompat.getDrawable(context!!, mFeatureCurrentList[1].icon)
            text_title2.text = mFeatureIconCurrentList[1].name
        } else {
            layout_frame2.visibility = View.GONE
        }
        if (mFeatureCurrentList.size > 2) {
            layout_frame3.visibility = View.VISIBLE
            image_icon3.background = ContextCompat.getDrawable(context!!, mFeatureCurrentList[2].icon)
            text_title3.text = mFeatureIconCurrentList[2].name
        } else {
            layout_frame3.visibility = View.GONE
        }

        if (mFeatureCurrentList.size > 3) {
            layout_frame4.visibility = View.VISIBLE
            image_icon4.background = ContextCompat.getDrawable(context!!, mFeatureCurrentList[3].icon)
            text_title4.text = mFeatureIconCurrentList[3].name
        } else {
            layout_frame4.visibility = View.GONE
        }
        if (mFeatureCurrentList.size > 4) {
            layout_frame5.visibility = View.VISIBLE
            image_icon5.background = ContextCompat.getDrawable(context!!, mFeatureCurrentList[4].icon)
            text_title5.text = mFeatureIconCurrentList[4].name
        } else {

            layout_frame5.visibility = View.GONE
        }
    }

    /**
     * 切換天氣or廣告欄
     * @type: TAG_TYPE_1:天氣
     *        TAG_TYPE_2:廣告欄
     */
    private fun switchWedge(type: Int?) {

        when (type) {
            TAG_TYPE_1 -> {
                include_home_banner.visibility = View.INVISIBLE
                include_weather.visibility = View.VISIBLE
                if (mWeatherData != null) {
                    mWeatherData?.forecasts?.get(0)?.let {
                        val icon = WeatherIconEnum.getItemByName(it.text).mIcon
                        if (icon != -1) {
                            imageView_weather.visibility = View.VISIBLE
                            Glide.with(this)
                                .load(icon)
                                .skipMemoryCache(true)
                                .into(imageView_weather)
                        } else {
                            imageView_weather.visibility = View.INVISIBLE
                        }
                        textView_value.text = String.format("%s %s", it.high, getString(R.string.symbol_temp))
                    }
                } else {
                    val weather: HomeWeather? = mData?.home?.weather
                    weather?.weather_city?.let { mViewModel.getWeather(it.toLowerCase().replace(" ", "")) }

                    textView_wifi_id.text = weather?.wifi_id
                    textView_wifiIdTitle.text = weather?.wifi_id_title
                    textView_wifi_password.text = weather?.wifi_password
                    textView_wifiPasswordTitle.text = weather?.wifi_password_title
                    textView_value.text = weather?.temp_none
                    weather_title.text = weather?.weather_title
                    imageView_weather.visibility = View.INVISIBLE
                }

            }
            TAG_TYPE_2 -> {
                include_weather.visibility = View.INVISIBLE
                include_home_banner.visibility = View.VISIBLE

                mData?.home?.promo_banner?.toList()?.let { list ->
                    context?.let {
                        var view: ImageView = ImageView(it)
                        view.scaleType = ImageView.ScaleType.FIT_XY
                        Glide.with(it)
                            .load(FileUtils.getFileFromStorage(list.last().image))
                            .into(view)
                        mBannerList?.add(view)
                    }
                    for (item in list) {
                        context?.let {
                            var view: ImageView = ImageView(it)
                            view.scaleType = ImageView.ScaleType.FIT_XY
                            Glide.with(it)
                                .load(FileUtils.getFileFromStorage(item.image))
                                .into(view)
                            mBannerList?.add(view)
                        }
                    }
                    context?.let {
                        var view: ImageView = ImageView(it)
                        view.scaleType = ImageView.ScaleType.FIT_XY
                        Glide.with(it)
                            .load(FileUtils.getFileFromStorage(list.first().image))
                            .into(view)
                        mBannerList?.add(view)
                    }
                }
                mBannerAdapter = mBannerList?.let { BannerAdapter(it) }
                image_banner.adapter = mBannerAdapter
                image_banner.currentItem = 1
                image_banner.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

                    var currentPosition: Int = 0

                    override fun onPageScrollStateChanged(state: Int) {
                        // ViewPager.SCROLL_STATE_IDLE 标识的状态是当前页面完全展现，并且没有动画正在进行中，如果不
                        // 是此状态下执行 setCurrentItem 方法回在首位替换的时候会出现跳动！
                        if (state != ViewPager.SCROLL_STATE_IDLE) return

                        mBannerList?.let { list ->
                            // 当视图在第一个时，将页面号设置为图片的最后一张。
                            if (currentPosition == 0) {
                                image_banner.setCurrentItem(list.size - 2, false)

                            } else if (currentPosition == list.size - 1) {
                                // 当视图在最后一个是,将页面号设置为图片的第一张。
                                image_banner.setCurrentItem(1, false)
                            }
                        }
                    }

                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    }

                    override fun onPageSelected(position: Int) {
                        currentPosition = position
                    }
                })
                image_banner.setOnKeyListener(object:View.OnKeyListener{
                    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                        return false
                    }

                })


                if (mBannerDisposable != null && !mBannerDisposable!!.isDisposed) {
                    mBannerDisposable?.dispose()
                }
                mBannerDisposable = Observable.interval(0,4,TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        onNext-> image_banner?.arrowScroll(View.FOCUS_RIGHT)
                    },{
                        onError ->
                    },{
                        image_banner?.arrowScroll(View.FOCUS_RIGHT)
                    })
            }
        }
    }

    var focusItem = 0
    fun focusChange() {
        Log.d("neo", "foucsTiem = $focusItem")
        when (focusItem) {
            0 -> {
                videoView_frame.setBackgroundResource(R.color.homeIconFrameFocused)
                layout_frame1.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame2.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame3.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame4.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame5.setBackgroundResource(R.drawable.home_icon_frame_frame_default)

                context?.let { text_title.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title2.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title3.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title4.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title5.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                if (mFeatureCurrentList.size > 0)
                    image_icon.setBackgroundResource(mFeatureCurrentList[0].icon)
                if (mFeatureCurrentList.size > 1)
                    image_icon2.setBackgroundResource(mFeatureCurrentList[1].icon)
                if (mFeatureCurrentList.size > 2)
                    image_icon3.setBackgroundResource(mFeatureCurrentList[2].icon)
                if (mFeatureCurrentList.size > 3)
                    image_icon4.setBackgroundResource(mFeatureCurrentList[3].icon)
                if (mFeatureCurrentList.size > 4)
                    image_icon5.setBackgroundResource(mFeatureCurrentList[4].icon)
            }
            1 -> {

                videoView_frame.setBackgroundResource(R.color.videoBackground)
                layout_frame1.setBackgroundResource(R.drawable.home_icon_frame_frame_focused)
                layout_frame2.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame3.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame4.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame5.setBackgroundResource(R.drawable.home_icon_frame_frame_default)

                context?.let { text_title.setTextColor(ContextCompat.getColor(it, R.color.homeIconFrameFocused)) }
                context?.let { text_title2.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title3.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title4.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title5.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }

                if (mFeatureCurrentList.size > 0)
                    image_icon.setBackgroundResource(mFeatureCurrentList[0].focusedIcon)
                if (mFeatureCurrentList.size > 1)
                    image_icon2.setBackgroundResource(mFeatureCurrentList[1].icon)
                if (mFeatureCurrentList.size > 2)
                    image_icon3.setBackgroundResource(mFeatureCurrentList[2].icon)
                if (mFeatureCurrentList.size > 3)
                    image_icon4.setBackgroundResource(mFeatureCurrentList[3].icon)
                if (mFeatureCurrentList.size > 4)
                    image_icon5.setBackgroundResource(mFeatureCurrentList[4].icon)
            }
            2 -> {
                videoView_frame.setBackgroundResource(R.color.videoBackground)
                layout_frame1.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame2.setBackgroundResource(R.drawable.home_icon_frame_frame_focused)
                layout_frame3.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame4.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame5.setBackgroundResource(R.drawable.home_icon_frame_frame_default)

                context?.let { text_title.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title2.setTextColor(ContextCompat.getColor(it, R.color.homeIconFrameFocused)) }
                context?.let { text_title3.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title4.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title5.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }

                if (mFeatureCurrentList.size > 0)
                    image_icon.setBackgroundResource(mFeatureCurrentList[0].icon)
                if (mFeatureCurrentList.size > 1)
                    image_icon2.setBackgroundResource(mFeatureCurrentList[1].focusedIcon)
                if (mFeatureCurrentList.size > 2)
                    image_icon3.setBackgroundResource(mFeatureCurrentList[2].icon)
                if (mFeatureCurrentList.size > 3)
                    image_icon4.setBackgroundResource(mFeatureCurrentList[3].icon)
                if (mFeatureCurrentList.size > 4)
                    image_icon5.setBackgroundResource(mFeatureCurrentList[4].icon)
            }
            3 -> {
                videoView_frame.setBackgroundResource(R.color.videoBackground)
                layout_frame1.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame2.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame3.setBackgroundResource(R.drawable.home_icon_frame_frame_focused)
                layout_frame4.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame5.setBackgroundResource(R.drawable.home_icon_frame_frame_default)

                context?.let { text_title.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title2.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title3.setTextColor(ContextCompat.getColor(it, R.color.homeIconFrameFocused)) }
                context?.let { text_title4.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title5.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }

                if (mFeatureCurrentList.size > 0)
                    image_icon.setBackgroundResource(mFeatureCurrentList[0].icon)
                if (mFeatureCurrentList.size > 1)
                    image_icon2.setBackgroundResource(mFeatureCurrentList[1].icon)
                if (mFeatureCurrentList.size > 2)
                    image_icon3.setBackgroundResource(mFeatureCurrentList[2].focusedIcon)
                if (mFeatureCurrentList.size > 3)
                    image_icon4.setBackgroundResource(mFeatureCurrentList[3].icon)
                if (mFeatureCurrentList.size > 4)
                    image_icon5.setBackgroundResource(mFeatureCurrentList[4].icon)
            }
            4 -> {
                videoView_frame.setBackgroundResource(R.color.videoBackground)
                layout_frame1.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame2.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame3.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame4.setBackgroundResource(R.drawable.home_icon_frame_frame_focused)
                layout_frame5.setBackgroundResource(R.drawable.home_icon_frame_frame_default)

                context?.let { text_title.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title2.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title3.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title4.setTextColor(ContextCompat.getColor(it, R.color.homeIconFrameFocused)) }
                context?.let { text_title5.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }

                if (mFeatureCurrentList.size > 0)
                    image_icon.setBackgroundResource(mFeatureCurrentList[0].icon)
                if (mFeatureCurrentList.size > 1)
                    image_icon2.setBackgroundResource(mFeatureCurrentList[1].icon)
                if (mFeatureCurrentList.size > 2)
                    image_icon3.setBackgroundResource(mFeatureCurrentList[2].icon)
                if (mFeatureCurrentList.size > 3)
                    image_icon4.setBackgroundResource(mFeatureCurrentList[3].focusedIcon)
                if (mFeatureCurrentList.size > 4)
                    image_icon5.setBackgroundResource(mFeatureCurrentList[4].icon)
            }
            5 -> {
                videoView_frame.setBackgroundResource(R.color.videoBackground)
                layout_frame1.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame2.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame3.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame4.setBackgroundResource(R.drawable.home_icon_frame_frame_default)
                layout_frame5.setBackgroundResource(R.drawable.home_icon_frame_frame_focused)

                context?.let { text_title.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title2.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title3.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title4.setTextColor(ContextCompat.getColor(it, android.R.color.white)) }
                context?.let { text_title5.setTextColor(ContextCompat.getColor(it, R.color.homeIconFrameFocused)) }

                if (mFeatureCurrentList.size > 0)
                    image_icon.setBackgroundResource(mFeatureCurrentList[0].icon)
                if (mFeatureCurrentList.size > 1)
                    image_icon2.setBackgroundResource(mFeatureCurrentList[1].icon)
                if (mFeatureCurrentList.size > 2)
                    image_icon3.setBackgroundResource(mFeatureCurrentList[2].icon)
                if (mFeatureCurrentList.size > 3)
                    image_icon4.setBackgroundResource(mFeatureCurrentList[3].icon)
                if (mFeatureCurrentList.size > 4)
                    image_icon5.setBackgroundResource(mFeatureCurrentList[4].focusedIcon)
            }
        }
    }

    fun clickChange() {
        when (focusItem) {
            0 -> {
                startActivity(Intent(context, FullScreenActivity::class.java))
            }
            else -> {
                val page: Int = mFeatureCurrentList[focusItem - 1].page // 因為focusItem = 0是播放器，所以要 -1
                if (page == -100) {
                    Toast.makeText(context, "尚未實作", Toast.LENGTH_SHORT).show()
                    return
                }
                val b = Bundle()
                b.putParcelableArrayList(Page.ARG_BUNDLE, mFeatureIcons)
                getInteractionListener().switchPage(R.id.fragment_container, page, b, true, false, true)
            }
        }
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
                    //                    mViewModel.getTVHelper().playCurrent()?.subscribe()
                    TVController.playCurrent()
                })
    }
}