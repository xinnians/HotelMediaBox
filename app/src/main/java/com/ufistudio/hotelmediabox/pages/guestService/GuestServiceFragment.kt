package com.ufistudio.hotelmediabox.pages.guestService

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.content.ContextCompat
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
import com.ufistudio.hotelmediabox.pages.tourist.TouristAdapter
import com.ufistudio.hotelmediabox.pages.tourist.TouristFragment
import com.ufistudio.hotelmediabox.pages.tourist.TouristViewModel
import com.ufistudio.hotelmediabox.repository.data.*
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_BACK_TITLE
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_INDEX
import kotlinx.android.synthetic.main.fragment_guest_service.*
import kotlinx.android.synthetic.main.view_bottom_back_home.*
import kotlinx.android.synthetic.main.view_bottom_ok_back_home.*
import kotlinx.android.synthetic.main.view_guest_message.*

class GuestServiceFragment : InteractionView<OnPageInteractionListener.Primary>(), OnItemClickListener,
    OnItemFocusListener {

    private lateinit var mViewModel: GuestServiceViewModel
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List
    private var mAdapter: GuestServiceAdapter = GuestServiceAdapter(this, this)
    private var mContentFocus: Boolean = false //判斷目前focus是否在右邊的view
    private var mCategoryFocus: Boolean = true //判斷目前focus是否在category
    private var mCurrentCategoryIndex: Int = 0 //上一次List的選擇
    private var mCurrentContent: GuestCatagories? = null // 被選到的category內的Content
    private var mCurrentContentIndex: Int = 0 //上一次Content的瀏覽位置
    private var mSideViewState: HashMap<Int, String> = HashMap<Int, String>()//拿來儲存當前的sideView index與 Back上方的Title
    private var mCurrentSideIndex: Int = -1 //當前頁面side view index
    private var mTotalSize: HashMap<Int, Int>? =
        HashMap()//所有category內容的size, key = category index, value = category content size
    private var mData: ArrayList<GuestCatagories>? = ArrayList()
    private var mSideViewFocus: Boolean = false


    companion object {
        fun newInstance(): GuestServiceFragment = GuestServiceFragment()
        private val TAG = GuestServiceFragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = AppInjector.obtainViewModel(this)
        mViewModel.initGuestMessageProgress.observe(this, Observer { onInitProgress(it!!) })
        mViewModel.initGuestMessageSuccess.observe(this, Observer {
            onInitSuccess(it)
        })
        mViewModel.initGuestMessageError.observe(this, Observer {
            onInitError(it)
        })

        mHomeIcons = arguments?.getParcelableArrayList(Page.ARG_BUNDLE)
        mViewModel.initGuestMessage()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_guest_service, container, false)
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
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (mContentFocus) {
                    return true
                } else {
                    if (mSideViewFocus) {
                        if (sideView.getSelectPosition() + 1 < sideView.getItemSize()) {
                            sideView.setLastPosition(sideView.getSelectPosition() + 1)
                            sideView.scrollToPosition(sideView.getSelectPosition())
                        }
                    } else {
                        //若不是在ContentFocus，則將mCurrentContentIndex設為0
                        mCurrentContentIndex = 0
                        mData?.let {
                            if (mAdapter.getLastPosition() + 1 < it.size) {
                                mAdapter.setSelectPosition(mAdapter.getLastPosition() + 1)
                                recyclerView_service.scrollToPosition(mAdapter.getLastPosition())
                                mAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (mContentFocus) {
                    return true
                } else {
                    if (mSideViewFocus) {
                        if (sideView.getSelectPosition() > 0) {
                            sideView.setLastPosition(sideView.getSelectPosition() - 1)
                            sideView.scrollToPosition(sideView.getSelectPosition())
                        }
                    } else {
                        //若不是在ContentFocus，則將mCurrentContentIndex設為0
                        mCurrentContentIndex = 0
                        mData?.let {
                            if (mAdapter.getLastPosition() > 0) {
                                mAdapter.setSelectPosition(mAdapter.getLastPosition() - 1)
                                recyclerView_service.scrollToPosition(mAdapter.getLastPosition())
                                mAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (!sideView.isShown && mCategoryFocus) {
                    mAdapter.clearFocus(mCurrentCategoryIndex)
                    mCategoryFocus = false
                    mContentFocus = true
                    context?.let { ContextCompat.getColor(it, R.color.colorYellow) }?.let { type2_text_current_page.setTextColor(it) }
                } else if (mContentFocus) {
                    val curryIndex = mCurrentContentIndex
                    Log.e(TAG,
                        "[checkRightPosition] curryIndex before : $mCurrentContentIndex, mCurrentContent?.contents?.size : ${((mCurrentContent?.contents?.size
                            ?: 0) - 1)}"
                    )
                    if (curryIndex < ((mCurrentContent?.contents?.size ?: 0) - 1)) {
                        mCurrentContentIndex = curryIndex + 1
                        Log.e(TAG,
                            "[checkRightPosition] curryIndex after : $mCurrentContentIndex, mCurrentContent?.contents?.size : ${((mCurrentContent?.contents?.size
                                ?: 0) - 1)}"
                        )
                        renderViewContent()
                    }
                }
                checkSideArrow()
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
                        context?.let { ContextCompat.getColor(it, R.color.colorWhite) }?.let { type2_text_current_page.setTextColor(it) }
                    }
                    checkSideArrow()
                    return true
                }
            }
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_E -> {
                if (sideView.isShown) {
                    displaySideView(false)
                } else {
                    if (!mContentFocus)
                        displaySideView(true)
                    else {
                        mContentFocus = false
                        mCategoryFocus = true
                        mCurrentContentIndex = 0
                        context?.let { ContextCompat.getColor(it, R.color.colorWhite) }?.let { type2_text_current_page.setTextColor(it) }
                        mAdapter.selectLast(mCurrentCategoryIndex)
                        checkSideArrow()
                    }
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_Q-> {
                if (mSideViewFocus) {
                    sideView.intoPage()
                    return true
                } else {
                    if (!sideView.isShown && mCategoryFocus) {
                        mAdapter.clearFocus(mCurrentCategoryIndex)
                        mCategoryFocus = false
                        mContentFocus = true
                        context?.let { ContextCompat.getColor(it, R.color.colorYellow) }?.let { type2_text_current_page.setTextColor(it) }
                        mCurrentContentIndex = 0
                        if (mCurrentContentIndex < mTotalSize?.get(mCurrentCategoryIndex) ?: 0 - 1) {
                            mCurrentContentIndex += 1
                            renderViewContent()
                        }
                        checkSideArrow()
                    }
                    return true
                }
            }
        }
        return super.onFragmentKeyDown(keyCode, event)
    }

    private fun renderViewContent() {
        tv_content.text = mCurrentContent?.contents?.get(mCurrentContentIndex)?.content ?: ""
        type2_text_current_page.text = (mCurrentContentIndex + 1).toString()
        type2_text_total_page.text = "/" + mCurrentContent?.contents?.size.toString()
    }

    private fun onInitProgress(b: Boolean) {
        Log.e(TAG, "onInitProgress")
    }

    private fun onInitSuccess(data: Pair<PMS?, NoteButton?>?) {

        Log.e(TAG, "onInitSuccess")

//        for (i in 0 until (data?.first?.locationList?.size ?: 0)) {
//            mTotalSize?.set(i, data?.first?.locationList?.get(i)?.attractionsList?.size ?: 0)
//        }

        mData = data?.first?.catagories
        mAdapter.setData(data?.first?.catagories ?: ArrayList())

        textView_back.text = data?.second?.note?.back
        textView_home.text = data?.second?.note?.home
    }

    private fun onInitError(t: Throwable?) {
        Log.e(TAG, "onInitError : $t")
    }

    /**
     * Init Side View
     */
    private fun initSideView() {
        mSideViewState.putAll(sideView.setFocus(mHomeIcons, HomeFeatureEnum.GUEST))
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
     * 判斷新版本右半部的左右箭頭
     */
    private fun checkSideArrow() {
//        if (mCategoryFocus) {
//            imageView_arrow_left.visibility = View.GONE
//            imageView_arrow_right.visibility = View.GONE
//
//            imageView_side_arrow_left.visibility = View.GONE
//            imageView_side_arrow_right.visibility = View.GONE
//        }
//
//        if (mContentFocus) {
//
//            if (mCurrentContentIndex == 0 && mCurrentContent?.attractionsList?.size ?: -1 > 0) {
//                imageView_side_arrow_left.visibility = View.INVISIBLE
//                imageView_side_arrow_right.visibility = View.VISIBLE
//            }
//
//            if (mCurrentContentIndex > 0) {
//                imageView_side_arrow_left.visibility = View.VISIBLE
//                imageView_side_arrow_right.visibility = View.VISIBLE
//            }
//
//            if (mCurrentContentIndex == mTotalSize?.get(mCurrentCategoryIndex) ?: -1) {
//                imageView_side_arrow_left.visibility = View.VISIBLE
//                imageView_side_arrow_right.visibility = View.INVISIBLE
//            }
//        }
    }


    override fun onClick(view: View?) {
    }

    override fun onFoucsed(view: View?) {
        if (!mCategoryFocus) {
            return
        }
        mCurrentCategoryIndex = view?.getTag(TouristAdapter.TAG_INDEX) as Int
        val item = view.getTag(TouristAdapter.TAG_ITEM) as GuestCatagories
        mCurrentContent = item


        Log.e(TAG, "mCurrentCategoryIndex : $mCurrentCategoryIndex")
        renderViewContent()
    }
}