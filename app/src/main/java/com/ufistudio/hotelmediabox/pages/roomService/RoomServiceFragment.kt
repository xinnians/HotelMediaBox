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
    private var mLastSelectIndex: Int = 0 //上一次List的選擇
    private lateinit var mData: RoomServices
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_room_service, container, false)
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
        mAdapter.selectLast(mLastSelectIndex)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
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
            sideView.setLastPosition(HomeFeatureEnum.ROOM_SERVICE.ordinal)
        } else {
            sideView.visibility = View.GONE
            layout_back.visibility = View.VISIBLE
            view_line.visibility = View.GONE
        }
    }

    override fun onClick(view: View?) {
    }


    override fun onFoucsed(view: View?) {
        val item = view?.getTag(RoomServiceAdapter.TAG_ITEM) as RoomServiceCategories
        val bundle = Bundle()
        mLastSelectIndex = view.getTag(RoomServiceAdapter.TAG_INDEX) as Int
        bundle.putParcelable(Page.ARG_BUNDLE, item)
        val itemData = view.getTag(RoomServiceAdapter.TAG_ITEM) as RoomServiceCategories
        if (!mInSubContent)
            getInteractionListener().switchPage(R.id.fragment_sub_content, if (itemData.content_type == 1) Page.ROOM_SERVICE_TYPE1 else Page.ROOM_SERVICE_TYPE2, bundle, false, false)
    }

    override fun onSuccess(it: Any?) {
        mData = it as RoomServices
        mAdapter.setData(mData.categories)
    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "Error = ${t?.message}")
    }

    override fun onProgress(b: Boolean) {
    }
}