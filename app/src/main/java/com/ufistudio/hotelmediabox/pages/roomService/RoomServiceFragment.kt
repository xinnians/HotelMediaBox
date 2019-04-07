package com.ufistudio.hotelmediabox.pages.roomService

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
import com.ufistudio.hotelmediabox.repository.data.HomeIcons
import com.ufistudio.hotelmediabox.repository.data.RoomServiceCategories
import com.ufistudio.hotelmediabox.repository.data.RoomServices
import kotlinx.android.synthetic.main.fragment_room_service.*

class RoomServiceFragment : InteractionView<OnPageInteractionListener.Primary>(), OnItemClickListener,
        OnItemFocusListener, ViewModelsCallback {
    private lateinit var mViewModel: RoomServiceViewModel
    private var mAdapter: RoomServiceAdapter = RoomServiceAdapter(this, this)
    private var mInSubContent: Boolean = false //判斷目前focus是否在右邊的view
    //    private var mLastSelectIndex: Int = 0 //上一次List的選擇
    private var mCurrentSideIndex: Int = -1 //當前SideView index
    private var mCurrentCategoryIndex: Int = 0 //當前頁面category index
    private var mData: RoomServices? = null
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List
    private var mIsRendered: Boolean = false //判斷是否已經塞資料
    private var mCategoryFocus: Boolean = false
    private var mTextBackTitle: String = ""


    companion object {
        fun newInstance(): RoomServiceFragment = RoomServiceFragment()
        private val TAG = RoomServiceFragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initRoomServiceProgress.observe(this, Observer { onProgress(it!!) })
        mViewModel.initRoomServiceSuccess.observe(this, Observer {
            onSuccess(it)
        })
        mViewModel.initRoomServiceError.observe(this, Observer { onError(it) })

        mHomeIcons = arguments?.getParcelableArrayList(Page.ARG_BUNDLE)

        if (mHomeIcons != null) {
            for (i in 0 until mHomeIcons!!.size) {
                mCurrentSideIndex++
                if (mHomeIcons!![i].id == HomeFeatureEnum.ROOM_SERVICE.id) {
                    mTextBackTitle = mHomeIcons!![i].name
                    break
                }
                if (mHomeIcons!![i].enable == 0) {
                    mCurrentSideIndex--
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_room_service, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        if (mInSubContent) {
            if (getInteractionListener().getOnKeyListener() != null && getInteractionListener().getOnKeyListener()?.onKeyPress(keyCode, event)!!) {
                when (keyCode) {
                    KeyEvent.KEYCODE_BACK -> {
                        mAdapter.selectLast(mCurrentCategoryIndex)
                        mCategoryFocus = true
                        mInSubContent = false
                    }
                }
                return true
            }
        } else {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (!sideView.isShown && mCategoryFocus) {
                        mAdapter.clearFocus(mCurrentCategoryIndex)
                        mCategoryFocus = false
                        mInSubContent = true
                        if (getInteractionListener().getOnKeyListener() != null) {
                            getInteractionListener().setFragmentCacheData(true)
                            getInteractionListener().getOnKeyListener()?.onKeyPress(keyCode, event)!!
                        }
                    }
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
            sideView.visibility = View.VISIBLE
            layout_back.visibility = View.GONE
            view_line.visibility = View.VISIBLE
            mAdapter.sideViewIsShow(true)
            mCategoryFocus = false
            mInSubContent = false
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
        text_back.text = mTextBackTitle
        if (!mIsRendered) {
            if (mData?.categories != null) {
                mIsRendered = true
                mCategoryFocus = true
                mAdapter.selectLast(mCurrentCategoryIndex)
                mAdapter.setData(mData?.categories!!)
            }
        }
    }

    override fun onClick(view: View?) {
    }


    override fun onFoucsed(view: View?) {
        if (!mCategoryFocus)
            return
        val item = view?.getTag(RoomServiceAdapter.TAG_ITEM) as RoomServiceCategories
        val bundle = Bundle()
        mCurrentCategoryIndex = view.getTag(RoomServiceAdapter.TAG_INDEX) as Int
        bundle.putParcelable(Page.ARG_BUNDLE, item)
        val itemData = view.getTag(RoomServiceAdapter.TAG_ITEM) as RoomServiceCategories
        getInteractionListener().switchPage(R.id.fragment_sub_content, if (itemData.content_type == 1) Page.ROOM_SERVICE_TYPE1 else Page.ROOM_SERVICE_TYPE2, bundle, true, false)
    }

    override fun onSuccess(it: Any?) {
        mData = it as RoomServices
        renderView()
    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "Error = ${t?.message}")
    }

    override fun onProgress(b: Boolean) {
    }
}