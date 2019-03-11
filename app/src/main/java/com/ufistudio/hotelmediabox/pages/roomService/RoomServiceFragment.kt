package com.ufistudio.hotelmediabox.pages

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.interfaces.OnItemClickListener
import com.ufistudio.hotelmediabox.interfaces.OnItemFocusListener
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.pages.home.HomeFeatureEnum
import com.ufistudio.hotelmediabox.pages.roomService.RoomServiceAdapter
import kotlinx.android.synthetic.main.framgnet_room_service.*

class RoomServiceFragment : InteractionView<OnPageInteractionListener.Primary>(), OnItemClickListener,
        OnItemFocusListener {
    private lateinit var mViewModel: RoomServiceViewModel
    private var mAdapter: RoomServiceAdapter = RoomServiceAdapter(this, this)
    private var mInSubContent: Boolean = false //判斷目前focus是否在右邊的view
    private var mLastSelectIndex: Int = 0 //上一次List的選擇

    companion object {
        fun newInstance(): RoomServiceFragment = RoomServiceFragment()
        private val TAG = RoomServiceFragment::class.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.framgnet_room_service, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displaySideView(false)
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
        mLastSelectIndex = view?.getTag(RoomServiceAdapter.TAG_INDEX) as Int
        getInteractionListener().switchPage(R.id.fragment_sub_content, view.getTag(RoomServiceAdapter.TAG_PAGE) as Int, Bundle(), false, false)
    }
}