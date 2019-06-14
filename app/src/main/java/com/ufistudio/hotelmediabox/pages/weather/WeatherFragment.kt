package com.ufistudio.hotelmediabox.pages.weather

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.interfaces.OnItemClickListener
import com.ufistudio.hotelmediabox.interfaces.OnItemFocusListener
import com.ufistudio.hotelmediabox.interfaces.ViewModelsCallback
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.pages.home.HomeFeatureEnum
import com.ufistudio.hotelmediabox.repository.data.*
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_BACK_TITLE
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_INDEX
import kotlinx.android.synthetic.main.fragment_weather.*
import kotlinx.android.synthetic.main.view_bottom_back_home.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private const val TAG_DEFAULT_FORMAT = "dd MMM"
private const val TAG_UPDATE_FORMAT = "yyyyMMdd kkmm"

class WeatherFragment : InteractionView<OnPageInteractionListener.Primary>(), OnItemClickListener,
        OnItemFocusListener, ViewModelsCallback {
    private lateinit var mViewModel: WeatherViewModel
    private var mAdapter: WeatherAdapter = WeatherAdapter(this, this)
    private var mCurrentCategoryIndex: Int = 0 //當前頁面category index
    private var mCurrentSideIndex: Int = -1 //當前頁面side view index
    private var mIsRendered: Boolean = false //判斷是否已經塞資料
    private var mSideViewState: HashMap<Int, String> = HashMap<Int, String>()//拿來儲存當前的sideView index與 Back上方的Title

    private var mSideViewFocus: Boolean = false
    private var mCategoryFocus: Boolean = false
    private var mContentFocus: Boolean = false //判斷目前focus是否在右邊的view

    private var mData: Weather? = null
    private var mWeatherData: WeatherInfo? = null
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List
    private var mCityCode: String = ""//要帶入的城市編號

    private var mCurrentContent: WeatherContent? = null // 被選到的category內的 content
    private var mDf: DateFormat = SimpleDateFormat(TAG_DEFAULT_FORMAT, Locale.getDefault())


    private var mNoteBottom: NoteButton? = null//右下角提示資訊

    companion object {
        fun newInstance(): WeatherFragment = WeatherFragment()
        private val TAG = WeatherFragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initWeatherProgress.observe(this, Observer { onProgress(it!!) })
        mViewModel.initWeatherSuccess.observe(this, Observer {
            onSuccess(it)
        })
        mViewModel.initWeatherError.observe(this, Observer {
            onError(it)
        })

        mViewModel.getWeatherInfoProgress.observe(this, Observer { onProgress(it!!) })
        mViewModel.getWeatherInfoSuccess.observe(this, Observer { onSuccess(it!!) })
        mViewModel.getWeatherInfoError.observe(this, Observer { onError(it!!) })

        mHomeIcons = arguments?.getParcelableArrayList(Page.ARG_BUNDLE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSideView()
        recyclerView_service.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView_service.adapter = mAdapter

        displaySideView(false)
        sideView.setAdapterList(mHomeIcons)
        sideView.setInteractionListener(getInteractionListener())
    }

    override fun onStart() {
        super.onStart()

        renderCategoryView()
        renderEmptyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (mSideViewFocus) {
                    if (sideView.getSelectPosition() > 0) {
                        sideView.setLastPosition(sideView.getSelectPosition() - 1)
                        sideView.scrollToPosition(sideView.getSelectPosition())
                    }
                } else {
                    mData?.categories?.let {
                        if (mAdapter.getLastPosition() > 0) {
                            mAdapter.setSelectPosition(mAdapter.getLastPosition() - 1)
                            recyclerView_service.scrollToPosition(mAdapter.getLastPosition())
                            mAdapter.notifyDataSetChanged()
                        }
                    }
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (mSideViewFocus) {
                    if (sideView.getSelectPosition() + 1 < sideView.getItemSize()) {
                        sideView.setLastPosition(sideView.getSelectPosition() + 1)
                        sideView.scrollToPosition(sideView.getSelectPosition())
                    }
                } else {
                    mData?.categories?.let {
                        if (mAdapter.getLastPosition() + 1 < it.size) {
                            mAdapter.setSelectPosition(mAdapter.getLastPosition() + 1)
                            recyclerView_service.scrollToPosition(mAdapter.getLastPosition())
                            mAdapter.notifyDataSetChanged()
                        }
                    }
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                if (mSideViewFocus) {
                    sideView.intoPage()
                    return true
                }
            }
            KeyEvent.KEYCODE_BACK -> {
                if (sideView.isShown) {
                    displaySideView(false)
                } else {
                    displaySideView(true)
                }
                return true
            }
        }
        return super.onFragmentKeyDown(keyCode, event)
    }

    /**
     * Set SideView show or hide
     * @show: True : Show
     *        False: Hide
     */
    private fun displaySideView(show: Boolean) {
        if (show) {
            layout_back.visibility = View.GONE
            sideView.visibility = View.VISIBLE
            view_line.visibility = View.VISIBLE
            mAdapter.sideViewIsShow(true)
            mCategoryFocus = false
            mSideViewFocus = true
            sideView.scrollToPosition(mCurrentSideIndex)
            sideView.setLastPosition(mCurrentSideIndex)
        } else {
            sideView.visibility = View.GONE
            layout_back.visibility = View.VISIBLE
            view_line.visibility = View.GONE
            mAdapter.fromSideViewBack(mCurrentCategoryIndex)
            mCategoryFocus = true
            mSideViewFocus = false
        }
    }

    /**
     * Render category View
     */
    private fun renderCategoryView() {
        if (!mIsRendered) {
            if (mData?.categories != null) {
                mIsRendered = true
                mCategoryFocus = true
                mAdapter.setData(mData?.categories!!)
                mAdapter.selectLast(mCurrentCategoryIndex)
            }

            textView_back.text = mNoteBottom?.note?.back
            textView_home.text = mNoteBottom?.note?.home
        }
    }

    /**
     * Render 空資料時的UI
     */
    private fun renderEmptyView() {
        imageView_today.visibility = View.INVISIBLE
        imageView_weather1.visibility = View.GONE
        imageView_weather2.visibility = View.GONE
        imageView_weather3.visibility = View.GONE
        imageView_weather4.visibility = View.GONE
        imageView_weather5.visibility = View.GONE
        imageView_weather6.visibility = View.GONE
        textView_temperature1.visibility = View.GONE
        textView_temperature2.visibility = View.GONE
        textView_temperature3.visibility = View.GONE
        textView_temperature4.visibility = View.GONE
        textView_temperature5.visibility = View.GONE
        textView_temperature6.visibility = View.GONE

        textView_location.text = String.format("%s\n%s %s", mCityCode, "--", getString(R.string.symbol_temp))

        textView_title.text = mData?.title
        textView_subtitle.text = mData?.subtitle
        textView_last_update.text = String.format("%s --", mData?.update)
        textView_none1.visibility = View.VISIBLE
        textView_none2.visibility = View.VISIBLE
        textView_none3.visibility = View.VISIBLE
        textView_none4.visibility = View.VISIBLE
        textView_none5.visibility = View.VISIBLE
        textView_none6.visibility = View.VISIBLE
        textView_none1.text = mData?.temp_none
        textView_none2.text = mData?.temp_none
        textView_none3.text = mData?.temp_none
        textView_none4.text = mData?.temp_none
        textView_none5.text = mData?.temp_none
        textView_none6.text = mData?.temp_none
    }

    /**
     * 塞天氣的資料
     */
    private fun renderView() {
        var toDayTemp: String? = "--"
        if (mWeatherData != null) {
            if (!TextUtils.isEmpty(mWeatherData?.current_observation?.pubDate)) {
                textView_last_update.text = String.format("%s %s", mData?.update, parseDate(mWeatherData?.current_observation?.pubDate!!.toLong()))
            } else {
                textView_last_update.text = String.format("%s %s", mData?.update, "--")
            }


            if (mWeatherData?.forecasts != null) {
                with(mWeatherData?.forecasts!!) {
                    for (i in 0 until this.size) {
                        if (this[i] != null) {
                            when (i) {
                                0 -> {
                                    toDayTemp = this[i].high
                                    textView_location.text = String.format("%s\n%s %s", mCityCode, toDayTemp, getString(R.string.symbol_temp))
                                    val icon = WeatherIconEnum.getItemByName(this[i].text).mIcon
                                    if (icon != -1) {
                                        imageView_today.visibility = View.VISIBLE
                                        imageView_today.background = ContextCompat.getDrawable(imageView_today.context, icon)
                                    }
                                }
                                1 -> {
                                    textView_day1.text = this[i].day
                                    textView_date1.text = mDf.format(Date(this[i].date * 1000L))
                                    textView_none1.visibility = View.INVISIBLE
                                    textView_temperature1.visibility = View.VISIBLE
                                    textView_temperature1.text = String.format("%s %s", this[i].high, getString(R.string.symbol_temp))
                                    imageView_weather1.visibility = View.VISIBLE
                                    val icon = WeatherIconEnum.getItemByName(this[i].text).mIcon
                                    if (icon != -1)
                                        imageView_weather1.background = ContextCompat.getDrawable(imageView_weather1.context, icon)
                                }
                                2 -> {
                                    textView_day2.text = this[i].day
                                    textView_date2.text = mDf.format(Date(this[i].date * 1000L))
                                    textView_none2.visibility = View.INVISIBLE
                                    textView_temperature2.visibility = View.VISIBLE
                                    textView_temperature2.text = String.format("%s %s", this[i].high, getString(R.string.symbol_temp))
                                    imageView_weather2.visibility = View.VISIBLE
                                    val icon = WeatherIconEnum.getItemByName(this[i].text).mIcon
                                    if (icon != -1)
                                        imageView_weather2.background = ContextCompat.getDrawable(imageView_weather2.context, icon)
                                }
                                3 -> {
                                    textView_day3.text = this[i].day
                                    textView_date3.text = mDf.format(Date(this[i].date * 1000L))
                                    textView_none3.visibility = View.INVISIBLE
                                    textView_temperature3.visibility = View.VISIBLE
                                    textView_temperature3.text = String.format("%s %s", this[i].high, getString(R.string.symbol_temp))
                                    imageView_weather3.visibility = View.VISIBLE
                                    val icon = WeatherIconEnum.getItemByName(this[i].text).mIcon
                                    if (icon != -1)
                                        imageView_weather3.background = ContextCompat.getDrawable(imageView_weather3.context, icon)
                                }
                                4 -> {
                                    textView_day4.text = this[i].day
                                    textView_date4.text = mDf.format(Date(this[i].date * 1000L))
                                    textView_none4.visibility = View.INVISIBLE
                                    textView_temperature4.visibility = View.VISIBLE
                                    textView_temperature4.text = String.format("%s %s", this[i].high, getString(R.string.symbol_temp))
                                    imageView_weather4.visibility = View.VISIBLE
                                    val icon = WeatherIconEnum.getItemByName(this[i].text).mIcon
                                    if (icon != -1)
                                        imageView_weather4.background = ContextCompat.getDrawable(imageView_weather4.context, icon)
                                }
                                5 -> {
                                    textView_day5.text = this[i].day
                                    textView_date5.text = mDf.format(Date(this[i].date * 1000L))
                                    textView_none5.visibility = View.INVISIBLE
                                    textView_temperature5.visibility = View.VISIBLE
                                    textView_temperature5.text = String.format("%s %s", this[i].high, getString(R.string.symbol_temp))
                                    imageView_weather5.visibility = View.VISIBLE
                                    val icon = WeatherIconEnum.getItemByName(this[i].text).mIcon
                                    if (icon != -1)
                                        imageView_weather5.background = ContextCompat.getDrawable(imageView_weather5.context, icon)
                                }
                                6 -> {
                                    textView_day6.text = this[i].day
                                    textView_date6.text = mDf.format(Date(this[i].date * 1000L))
                                    textView_none6.visibility = View.INVISIBLE
                                    textView_temperature6.visibility = View.VISIBLE
                                    textView_temperature6.text = String.format("%s %s", this[i].high, getString(R.string.symbol_temp))
                                    imageView_weather6.visibility = View.VISIBLE
                                    val icon = WeatherIconEnum.getItemByName(this[i].text).mIcon
                                    if (icon != -1)
                                        imageView_weather6.background = ContextCompat.getDrawable(imageView_weather6.context, icon)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Init Side View
     */
    private fun initSideView() {
        mSideViewState.putAll(sideView.setFocus(mHomeIcons, HomeFeatureEnum.WEATHER))
        mCurrentSideIndex = mSideViewState[ARG_CURRENT_INDEX]!!.toInt()
        text_back.text = mSideViewState[ARG_CURRENT_BACK_TITLE]
    }

    override fun onClick(view: View?) {
    }

    override fun onFoucsed(view: View?) {
        if (!mCategoryFocus) {
            return
        }
        mCurrentContent = view?.getTag(WeatherAdapter.TAG_ITEM) as WeatherContent
        mCurrentCategoryIndex = view.getTag(WeatherAdapter.TAG_INDEX) as Int
        mCityCode = (view.getTag(WeatherAdapter.TAG_TITLE) as String)
        renderEmptyView()
        mViewModel.getWeather(mCityCode)
    }

    override fun onSuccess(it: Any?) {
        if (it != null) {
            if (it is WeatherInfo) {
                if (it.location != null && !TextUtils.isEmpty(it.location.city)) {
                    mWeatherData = it
                    renderView()
                }
            } else if (it is Pair<*, *>) {
                val data: Pair<*, *> = it
                mData = data.first as Weather?
                mNoteBottom = data.second as NoteButton?
                renderCategoryView()
            }

        }
    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "error: ${t?.message}")
        renderEmptyView()
    }

    override fun onProgress(b: Boolean) {
    }

    /**
     * parse last update time
     */
    private fun parseDate(time: Long): String? {
        return SimpleDateFormat(TAG_UPDATE_FORMAT, Locale.TAIWAN).format("${time}000".toLong())
    }
}