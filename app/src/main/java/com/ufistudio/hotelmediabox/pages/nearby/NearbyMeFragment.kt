package com.ufistudio.hotelmediabox.pages.nearby

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
import com.ufistudio.hotelmediabox.repository.data.NearbyMe
import com.ufistudio.hotelmediabox.repository.data.NearbyMeCategories
import kotlinx.android.synthetic.main.fragment_room_service.*

class NearbyMeFragment : InteractionView<OnPageInteractionListener.Primary>(), OnItemClickListener,
        OnItemFocusListener, ViewModelsCallback {
    private lateinit var mViewModel: NearbyMeViewModel
    private var mAdapter: NearbyMeAdapter = NearbyMeAdapter(this, this)
    private var mInSubContent: Boolean = false //判斷目前focus是否在右邊的view
    private var mLastSelectIndex: Int = 0 //上一次List的選擇
    private var mCurrentSideIndex: Int = -1 //當前頁面side view index
    private var mIsRendered: Boolean = false //判斷是否已經塞資料

    private var mData: NearbyMe? = null
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List

    companion object {
        fun newInstance(): NearbyMeFragment = NearbyMeFragment()
        private val TAG = NearbyMeFragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initNearbyMeProgress.observe(this, Observer { onProgress(it!!) })
        mViewModel.initNearbyMeSuccess.observe(this, Observer {
            onSuccess(it)
        })
        mViewModel.initNearbyMeError.observe(this, Observer {
            onError(it)
        })

        mHomeIcons = arguments?.getParcelableArrayList(Page.ARG_BUNDLE)
        if (mHomeIcons != null) {
            for (i in 0 until mHomeIcons!!.size) {
                mCurrentSideIndex++
                if (mHomeIcons!![i].name == HomeFeatureEnum.FACILITIES.tag) {
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
        return inflater.inflate(R.layout.fragment_nearby_me, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displaySideView(false)
        sideView.setAdapterList(mHomeIcons)
        sideView.setInteractionListener(getInteractionListener())
    }

    override fun onStart() {
        super.onStart()
        recyclerView_service.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView_service.adapter = mAdapter
        renderView()
        mAdapter.selectLast(mLastSelectIndex)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (mInSubContent) {
                    return true
                } else {

                }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (sideView.visibility == View.VISIBLE) {
                    displaySideView(false)
                    if (!mInSubContent) {
                        mAdapter.sideViewIsShow(false)
                        mAdapter.selectLast(mLastSelectIndex)
                        return true
                    }
                } else {
                    mInSubContent = true
                }
                return false
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (mInSubContent) {
                    return true
                }
                mAdapter.selectLast()
            }
            KeyEvent.KEYCODE_BACK -> {
                if (mInSubContent) {
                    mInSubContent = false
                    mAdapter.selectLast(mLastSelectIndex)
                    return true
                }
                if (sideView.visibility == View.GONE) {
                    displaySideView(true)
                    mAdapter.sideViewIsShow(true)
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
            mAdapter.selectLast(-1)
            sideView.setLastPosition(mCurrentSideIndex)
        } else {
            sideView.visibility = View.GONE
            layout_back.visibility = View.VISIBLE
            view_line.visibility = View.GONE
        }
    }

    /**
     * 塞資料
     */
    private fun renderView() {
        if (!mIsRendered) {
            if (mData?.categories != null) {
                mIsRendered = true
                mAdapter.setData(mData?.categories!!)
            }
        }
    }

    override fun onClick(view: View?) {
    }

    override fun onFoucsed(view: View?) {
        val item = view?.getTag(NearbyMeAdapter.TAG_ITEM) as NearbyMeCategories
        val bundle = Bundle()
        mLastSelectIndex = view.getTag(NearbyMeAdapter.TAG_INDEX) as Int
        bundle.putParcelable(Page.ARG_BUNDLE, item)
        if (!mInSubContent) {
            getInteractionListener().switchPage(R.id.fragment_sub_content, Page.HOTEL_FACILITIES_CONTENT, bundle, false, false, true)
        }
    }

    override fun onSuccess(it: Any?) {
        mData = it as NearbyMe
        renderView()
    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "error: ${t?.message}")
    }

    override fun onProgress(b: Boolean) {
    }
}