package com.ufistudio.hotelmediabox.pages.weather

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
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
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List

    private var mCurrentContent: WeatherContent? = null // 被選到的category內的 content

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
        renderView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_UP -> {
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                return true
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
            sideView.scrollToPosition(mCurrentSideIndex)
            sideView.setLastPosition(mCurrentSideIndex)
        } else {
            sideView.visibility = View.GONE
            layout_back.visibility = View.VISIBLE
            view_line.visibility = View.GONE
            mAdapter.fromSideViewBack(mCurrentCategoryIndex)
            mCategoryFocus = true
        }
    }

    /**
     * 塞資料
     */
    private fun renderView() {
        if (!mIsRendered) {
            if (mData?.categories != null) {
                mIsRendered = true
                mCategoryFocus = true
                mAdapter.setData(mData?.categories!!)
                mAdapter.selectLast(mCurrentCategoryIndex)

            }

            textView_title.text = mData?.title
            textView_subtitle.text = mData?.subtitle
            textView_last_update.text = String.format("%s --", mData?.update)
            textView_none1.visibility = view?.visibility!!
            textView_none2.visibility = view?.visibility!!
            textView_none3.visibility = view?.visibility!!
            textView_none4.visibility = view?.visibility!!
            textView_none5.visibility = view?.visibility!!
            textView_none6.visibility = view?.visibility!!
            textView_none1.text = mData?.temp_none
            textView_none2.text = mData?.temp_none
            textView_none3.text = mData?.temp_none
            textView_none4.text = mData?.temp_none
            textView_none5.text = mData?.temp_none
            textView_none6.text = mData?.temp_none

            textView_back.text = mNoteBottom?.note?.back
            textView_home.text = mNoteBottom?.note?.home
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
        textView_location.text = String.format("%s\n-- %s", view.getTag(WeatherAdapter.TAG_TITLE) as String, getString(R.string.symbol_temp))

    }

    override fun onSuccess(it: Any?) {
        if (it != null) {
            val data: Pair<*, *> = it as Pair<*, *>
            mData = data.first as Weather?
            mNoteBottom = data.second as NoteButton?
            renderView()
        }
    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "error: ${t?.message}")
    }

    override fun onProgress(b: Boolean) {
    }
}