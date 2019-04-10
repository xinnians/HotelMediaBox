package com.ufistudio.hotelmediabox.pages.facilies

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
import com.ufistudio.hotelmediabox.repository.data.HotelFacilities
import com.ufistudio.hotelmediabox.repository.data.HotelFacilitiesCategories
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_BACK_TITLE
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_INDEX
import kotlinx.android.synthetic.main.fragment_room_service.*

class HotelFacilitiesFragment : InteractionView<OnPageInteractionListener.Primary>(), OnItemClickListener,
        OnItemFocusListener, ViewModelsCallback {
    private lateinit var mViewModel: HotelFacilitiesViewModel
    private var mAdapter: HotelFacilitiesAdapter = HotelFacilitiesAdapter(this, this)
    private var mCurrentCategoryIndex: Int = 0 //當前頁面category index
    private var mCurrentSideIndex: Int = -1 //當前頁面side view index
    private var mIsRendered: Boolean = false //判斷是否已經塞資料
    private var mSideViewState: HashMap<Int, String> = HashMap<Int, String>()//拿來儲存當前的sideView index與 Back上方的Title

    private var mSideViewFocus: Boolean = false
    private var mCategoryFocus: Boolean = false
    private var mContentFocus: Boolean = false //判斷目前focus是否在右邊的view

    private var mData: HotelFacilities? = null
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List

    companion object {
        fun newInstance(): HotelFacilitiesFragment = HotelFacilitiesFragment()
        private val TAG = HotelFacilitiesFragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initHotelFacilitiesProgress.observe(this, Observer { onProgress(it!!) })
        mViewModel.initHotelFacilitiesSuccess.observe(this, Observer {
            onSuccess(it)
        })
        mViewModel.initHotelFacilitiesError.observe(this, Observer {
            onError(it)
        })

        mHomeIcons = arguments?.getParcelableArrayList(Page.ARG_BUNDLE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_room_service, container, false)
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
        if (mContentFocus) {
            if (getInteractionListener().getOnKeyListener() != null && getInteractionListener().getOnKeyListener()?.onKeyPress(keyCode, event)!!) {
                when (keyCode) {
                    KeyEvent.KEYCODE_BACK -> {
                        mAdapter.selectLast(mCurrentCategoryIndex)
                        mCategoryFocus = true
                        mContentFocus = false
                    }
                }
                return true
            }
        } else {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (mContentFocus) {
                        return true
                    } else {

                    }
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (!sideView.isShown && mCategoryFocus) {
                        mAdapter.clearFocus(mCurrentCategoryIndex)
                        mCategoryFocus = false
                        mContentFocus = true
                        if (getInteractionListener().getOnKeyListener() != null) {
                            getInteractionListener().setFragmentCacheData(true)
                            getInteractionListener().getOnKeyListener()?.onKeyPress(keyCode, event)!!
                        }
                    }
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if (mContentFocus) {
                        return true
                    }
                    mAdapter.selectLast()
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
                mAdapter.setData(mData?.categories!!)
            }
        }
    }

    /**
     * Init Side View
     */
    private fun initSideView() {
        mSideViewState.putAll(sideView.setFocus(mHomeIcons, HomeFeatureEnum.FACILITIES))
        mCurrentSideIndex = mSideViewState[ARG_CURRENT_INDEX]!!.toInt()
        text_back.text = mSideViewState[ARG_CURRENT_BACK_TITLE]
    }

    override fun onClick(view: View?) {
    }

    override fun onFoucsed(view: View?) {
        if (!mCategoryFocus) {
            return
        }
        val item = view?.getTag(HotelFacilitiesAdapter.TAG_ITEM) as HotelFacilitiesCategories
        val bundle = Bundle()
        bundle.putParcelable(Page.ARG_BUNDLE, item)
        mCurrentCategoryIndex = view.getTag(HotelFacilitiesAdapter.TAG_INDEX) as Int
        getInteractionListener().switchPage(R.id.fragment_sub_content, Page.HOTEL_FACILITIES_CONTENT, bundle, true, false, true)
    }

    override fun onSuccess(it: Any?) {
        if (it != null) {
            mData = it as HotelFacilities
            renderView()
        }
    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "error: ${t?.message}")
    }

    override fun onProgress(b: Boolean) {
    }
}