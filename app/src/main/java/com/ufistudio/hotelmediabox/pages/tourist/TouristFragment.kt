package com.ufistudio.hotelmediabox.pages.tourist

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.interfaces.OnItemClickListener
import com.ufistudio.hotelmediabox.interfaces.OnItemFocusListener
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.pages.home.HomeFeatureEnum
import com.ufistudio.hotelmediabox.repository.data.HomeIcons
import com.ufistudio.hotelmediabox.repository.data.NoteButton
import com.ufistudio.hotelmediabox.repository.data.TouristInfo
import com.ufistudio.hotelmediabox.repository.data.TouristLocation
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_BACK_TITLE
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_INDEX
import kotlinx.android.synthetic.main.fragment_tourist.*
import kotlinx.android.synthetic.main.view_bottom_back_home.*
import kotlinx.android.synthetic.main.view_bottom_ok_back_home.*
import kotlinx.android.synthetic.main.view_tourist.*
import kotlinx.android.synthetic.main.view_tourist_map.*

class TouristFragment : InteractionView<OnPageInteractionListener.Primary>(), OnItemClickListener, OnItemFocusListener {

    private lateinit var mViewModel: TouristViewModel
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List
    private var mAdapter: TouristAdapter = TouristAdapter(this, this)
    private var mContentFocus: Boolean = false //判斷目前focus是否在右邊的view
    private var mCategoryFocus: Boolean = true //判斷目前focus是否在category
    private var mCurrentCategoryIndex: Int = 0 //上一次List的選擇
    private var mCurrentContent: TouristLocation? = null // 被選到的category內的Content
    private var mCurrentContentIndex: Int = 0 //上一次Content的瀏覽位置
    private var mSideViewState: HashMap<Int, String> = HashMap<Int, String>()//拿來儲存當前的sideView index與 Back上方的Title
    private var mCurrentSideIndex: Int = -1 //當前頁面side view index
    private var mTotalSize: HashMap<Int, Int>? =
        HashMap()//所有category內容的size, key = category index, value = category content size

    companion object {
        fun newInstance(): TouristFragment = TouristFragment()
        private val TAG = TouristFragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = AppInjector.obtainViewModel(this)
        mViewModel.initTouristProgress.observe(this, Observer { onInitProgress(it!!) })
        mViewModel.initTouristSuccess.observe(this, Observer {
            onInitSuccess(it)
        })
        mViewModel.initTouristError.observe(this, Observer {
            onInitError(it)
        })

        mHomeIcons = arguments?.getParcelableArrayList(Page.ARG_BUNDLE)
        mViewModel.initTourist()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_tourist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView_service.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView_service.adapter = mAdapter
        initSideView()
        displaySideView(false)
        sideView.setAdapterList(mHomeIcons)
        sideView.setInteractionListener(getInteractionListener())
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (mContentFocus) {
                    return true
                } else {
                    //若不是在ContentFocus，則將mCurrentContentIndex設為0
                    mCurrentContentIndex = 0
                }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (!sideView.isShown && mCategoryFocus) {
                    mAdapter.clearFocus(mCurrentCategoryIndex)
                    mCategoryFocus = false
                    mContentFocus = true
                } else if (mContentFocus) {
                    val curryIndex = mCurrentContentIndex
                    if (curryIndex < mTotalSize?.get(mCurrentCategoryIndex) ?: 0 - 1) {
                        mCurrentContentIndex = curryIndex + 1
                        renderViewContent()
                    }
                }
                checkArrow()
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (mContentFocus) {
                    val curryIndex = mCurrentContentIndex
                    if (curryIndex != 0) {
                        mCurrentContentIndex = curryIndex - 1
                        renderViewContent()
                    } else if (curryIndex == 0) {
                        mCategoryFocus = true
                        mContentFocus = false
                        mAdapter.selectLast(mCurrentCategoryIndex)
                    }
                    checkArrow()
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

    private fun onInitProgress(b: Boolean) {
        Log.e(TAG, "onInitProgress")
    }

    private fun onInitSuccess(data: Pair<TouristInfo?, NoteButton?>?) {

        Log.e(TAG, "onInitSuccess")

        for (i in 0 until (data?.first?.locationList?.size ?: 0)) {
            mTotalSize?.set(i, data?.first?.locationList?.get(i)?.attractionsList?.size ?: 0)
        }
        mAdapter.setData(data?.first?.locationList ?: ArrayList())

        textView_back.text = data?.second?.note?.back
        textView_home.text = data?.second?.note?.home
        textView_ok.text = data?.second?.note?.fullScreen
    }

    private fun onInitError(t: Throwable?) {
        Log.e(TAG, "onInitError : $t")
    }

    override fun onClick(view: View?) {
    }

    override fun onFoucsed(view: View?) {
        if (!mCategoryFocus) {
            return
        }
        mCurrentCategoryIndex = view?.getTag(TouristAdapter.TAG_INDEX) as Int
        val item = view.getTag(TouristAdapter.TAG_ITEM) as TouristLocation
        mCurrentContent = item


        Log.e(TAG,"mCurrentCategoryIndex : $mCurrentCategoryIndex")
        renderViewContent()
    }

    private fun renderViewContent() {
        Log.e(TAG, "[renderViewContent] call")

        //TODO 要根據mCurrentContentIndex來判斷顯示map或景點資訊，上下切換時皆須將mCurrentContentIndex重置為0後再顯示畫面

        if (mCurrentContentIndex == 0) {
            view_content.visibility = View.GONE
            view_map.visibility = View.VISIBLE
            context?.let {
                Glide.with(it)
                    .load(
                        when (mCurrentContent?.background) {
                            is String -> {
                                FileUtils.getFileFromStorage(mCurrentContent?.background as String)
                            }
                            is Int -> {
                                mCurrentContent?.background
                            }
                            else -> ""
                        }
                    )
                    .into(view_background)

                Glide.with(it)
                    .load(
                        when (mCurrentContent?.focus) {
                            is String -> {
                                FileUtils.getFileFromStorage(mCurrentContent?.focus as String)
                            }
                            is Int -> {
                                mCurrentContent?.focus
                            }
                            else -> ""
                        }
                    )
                    .into(view_focus)
            }
        } else {
            view_content.visibility = View.VISIBLE
            view_map.visibility = View.GONE
            text_current_page.text = mCurrentContentIndex.toString()
            text_total_page.text = "/" + mTotalSize?.get(mCurrentCategoryIndex).toString()
            text_attr_title.text = mCurrentContent?.attractionsList?.get(mCurrentContentIndex - 1)?.title ?: ""
            context?.let {
                Glide.with(it)
                    .load(FileUtils.getFileFromStorage(mCurrentContent?.attractionsList?.get(mCurrentContentIndex - 1)?.imageName.toString()))
                    .into(image_content)
            }
            text_content.text = mCurrentContent?.attractionsList?.get(mCurrentContentIndex - 1)?.description ?: ""
        }
    }

    /**
     * Init Side View
     */
    private fun initSideView() {
        mSideViewState.putAll(sideView.setFocus(mHomeIcons, HomeFeatureEnum.NEAR_BY))
        mCurrentSideIndex = mSideViewState[ARG_CURRENT_INDEX]!!.toInt()
        text_back.text = mSideViewState[ARG_CURRENT_BACK_TITLE]
    }

    /**
     * Set SideView show or hide
     * @show: True : Show
     *        False: Hide
     */
    private fun displaySideView(show: Boolean) {
        if (show) {
            sideView.visibility = View.VISIBLE
            layout_back.visibility = View.GONE
            view_line.visibility = View.VISIBLE
            mAdapter.sideViewIsShow(true)
            mCategoryFocus = false
            mContentFocus = false
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
     * 判斷左右箭頭
     */
    private fun checkArrow() {
        if (mCategoryFocus) {
            imageView_arrow_left.visibility = View.INVISIBLE
            imageView_arrow_right.visibility = View.INVISIBLE
        }

        if (mContentFocus) {

            if (mCurrentContentIndex == 0 && mCurrentContent?.attractionsList?.size ?: -1 > 0) {
                imageView_arrow_left.visibility = View.INVISIBLE
                imageView_arrow_right.visibility = View.VISIBLE
            }

            if (mCurrentContentIndex > 0) {
                imageView_arrow_left.visibility = View.VISIBLE
                imageView_arrow_right.visibility = View.VISIBLE
            }

            if (mCurrentContentIndex == mTotalSize?.get(mCurrentCategoryIndex) ?: -1) {
                imageView_arrow_left.visibility = View.VISIBLE
                imageView_arrow_right.visibility = View.INVISIBLE
            }
        }
    }
}