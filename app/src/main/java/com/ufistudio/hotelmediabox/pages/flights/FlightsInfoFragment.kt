package com.ufistudio.hotelmediabox.pages.flights

import android.arch.lifecycle.Observer
import android.net.Uri
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
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import com.ufistudio.hotelmediabox.interfaces.OnItemClickListener
import com.ufistudio.hotelmediabox.interfaces.OnItemFocusListener
import com.ufistudio.hotelmediabox.interfaces.ViewModelsCallback
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.pages.home.HomeFeatureEnum
import com.ufistudio.hotelmediabox.repository.data.*
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_BACK_TITLE
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_INDEX
import kotlinx.android.synthetic.main.fragment_flights_info.*

class FlightsInfoFragment : InteractionView<OnPageInteractionListener.Primary>(), OnItemClickListener,
        OnItemFocusListener, ViewModelsCallback {
    private lateinit var mViewModel: FlightsInfoViewModel
    private var mAdapter: FlightsInfoAdapter = FlightsInfoAdapter(this, this)
    private var mCurrentCategoryIndex: Int = 0 //當前頁面category index
    private var mCurrentSideIndex: Int = -1 //當前頁面side view index
    private var mIsRendered: Boolean = false //判斷是否已經塞資料
    private var mSideViewState: HashMap<Int, String> = HashMap<Int, String>()//拿來儲存當前的sideView index與 Back上方的Title

    private var mSideViewFocus: Boolean = false
    private var mCategoryFocus: Boolean = false
    private var mContentFocus: Boolean = false //判斷目前focus是否在右邊的view
    private var mContentPlaying: Boolean = false //判斷目前是否有開始播放影片了

    private var mData: FlightsInfo? = null
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List
    private var mExoPlayerHelper: ExoPlayerHelper = ExoPlayerHelper()

    private var mCurrentIpTvSelectIndex: HashMap<Int, Int>? = HashMap() //記錄當前在第幾個Item的Ip Tv, key = category index, value = Ip tv index
    private var mTotalSize: HashMap<Int, Int>? = HashMap()//所有category內容的size, key = category index, value = category content size
    private var mCurrentContent: List<FlightsInfoContent>? = null // 被選到的category內的IP tv address

    companion object {
        fun newInstance(): FlightsInfoFragment = FlightsInfoFragment()
        private val TAG = FlightsInfoFragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initFlightsInfoProgress.observe(this, Observer { onProgress(it!!) })
        mViewModel.initFlightsInfoSuccess.observe(this, Observer {
            onSuccess(it)
        })
        mViewModel.initFlightsInfoError.observe(this, Observer {
            onError(it)
        })

        mHomeIcons = arguments?.getParcelableArrayList(Page.ARG_BUNDLE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_flights_info, container, false)
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

    override fun onStop() {
        mExoPlayerHelper.release()
        super.onStop()
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (mContentFocus) {
                    return true
                } else {
                    //若不是在ContentFocus，則將當前在播放的label設為false好讓focus可以更新
                    mContentPlaying = false
                }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (!sideView.isShown && mCategoryFocus) {
                    mAdapter.clearFocus(mCurrentCategoryIndex)
                    mCategoryFocus = false
                    mContentFocus = true
                } else if (mContentFocus) {
                    val curryIndex = mCurrentIpTvSelectIndex!![mCurrentCategoryIndex]!!
                    if (curryIndex < mTotalSize!![mCurrentCategoryIndex]!! - 1) {
                        mCurrentIpTvSelectIndex!![mCurrentCategoryIndex] = curryIndex + 1
                        setAndPlayVideo()
                    }
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (mContentFocus) {
                    val curryIndex = mCurrentIpTvSelectIndex!![mCurrentCategoryIndex]!!
                    if (curryIndex != 0) {
                        mCurrentIpTvSelectIndex!![mCurrentCategoryIndex] = curryIndex - 1
                        setAndPlayVideo()
                    }

                    return true
                }
            }
            KeyEvent.KEYCODE_BACK -> {
                if (sideView.isShown) {
                    displaySideView(false)
                } else {
                    if (!mContentFocus)
                        displaySideView(true)
                    else {
                        mContentFocus = false
                        mCategoryFocus = true
                        mAdapter.selectLast(mCurrentCategoryIndex)
                    }
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
                mAdapter.selectLast(mCurrentCategoryIndex)
                for (i in 0 until mData?.categories!!.size) {
                    mCurrentIpTvSelectIndex!![i] = 0
                    mTotalSize!![i] = mData?.categories!![i].contents.size
                }
                mAdapter.setData(mData?.categories!!)
                mExoPlayerHelper.initPlayer(context, videoView)
            }
        }
    }

    /**
     * Init Side View
     */
    private fun initSideView() {
        mSideViewState.putAll(sideView.setFocus(mHomeIcons, HomeFeatureEnum.FLIGHT_INFO))
        mCurrentSideIndex = mSideViewState[ARG_CURRENT_INDEX]!!.toInt()
        text_back.text = mSideViewState[ARG_CURRENT_BACK_TITLE]
    }

    override fun onClick(view: View?) {
    }

    override fun onFoucsed(view: View?) {
        if (!mCategoryFocus || mContentPlaying) {
            return
        }
        mCurrentContent = view?.getTag(FlightsInfoAdapter.TAG_ITEM) as ArrayList<FlightsInfoContent>
        mCurrentCategoryIndex = view.getTag(FlightsInfoAdapter.TAG_INDEX) as Int

        imageView_arrow_left.visibility = View.INVISIBLE
        //若ip tv的list只有一筆或沒有資料則不顯示arrow
        if (mCurrentContent?.size!! <= 1) {
            imageView_arrow_right.visibility = View.INVISIBLE
        }
        setAndPlayVideo()
    }

    override fun onSuccess(it: Any?) {
        if (it != null) {
            mData = it as FlightsInfo?
            renderView()
        }
    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "error: ${t?.message}")
    }

    override fun onProgress(b: Boolean) {
    }

    /**
     * 設置播放影片
     */
    private fun setAndPlayVideo() {
        /*
            設置左右箭頭
            只有一筆的時候箭頭不顯示
            index在0的時候左鍵頭部顯示
            index在最後一筆的時候右箭頭不顯示
         */
        when {
            mCurrentContent?.size == 1 -> {
                imageView_arrow_left.visibility = View.INVISIBLE
                imageView_arrow_right.visibility = View.INVISIBLE
            }
            mCurrentIpTvSelectIndex!![mCurrentCategoryIndex] == 0 -> {
                imageView_arrow_left.visibility = View.INVISIBLE
                imageView_arrow_right.visibility = View.VISIBLE
            }
            mCurrentIpTvSelectIndex!![mCurrentCategoryIndex] == mCurrentContent!!.size - 1 -> {
                imageView_arrow_left.visibility = View.VISIBLE
                imageView_arrow_right.visibility = View.INVISIBLE
            }
            else -> {
                imageView_arrow_left.visibility = View.VISIBLE
                imageView_arrow_right.visibility = View.VISIBLE
            }
        }

        mExoPlayerHelper.stop()
        try {
            mContentPlaying = true
            //TODO 改成播放IP
            mExoPlayerHelper.setFileSource(Uri.parse(FileUtils.getFileFromStorage(mCurrentContent!![mCurrentIpTvSelectIndex!![mCurrentCategoryIndex]!!].iptv)?.absolutePath))
        } catch (e: NullPointerException) {
            mExoPlayerHelper.release()
            mExoPlayerHelper.initPlayer(context, videoView)
            Log.e(TAG, "e = $e")
        }
    }
}