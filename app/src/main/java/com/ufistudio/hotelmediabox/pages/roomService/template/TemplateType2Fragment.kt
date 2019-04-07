package com.ufistudio.hotelmediabox.pages

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.interfaces.OnFragmentKeyListener
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.pages.roomService.template.TemplateType2PagerAdapter
import com.ufistudio.hotelmediabox.repository.data.RoomServiceCategories
import kotlinx.android.synthetic.main.fragment_room_service_content.*
import kotlinx.android.synthetic.main.fragment_room_service.*

class TemplateType2Fragment : InteractionView<OnPageInteractionListener.Primary>(), OnFragmentKeyListener, ViewPager.OnPageChangeListener {

    private lateinit var mViewModel: TemplateType2ViewModel
    private lateinit var mData: RoomServiceCategories
    private var mAdapter: TemplateType2PagerAdapter? = null
    private var mCurrentIndex: Int = 0
    private var mFocus: Boolean = false


    companion object {
        fun newInstance(): TemplateType2Fragment = TemplateType2Fragment()
        private val TAG = TemplateType2Fragment::class.simpleName
        private const val TAG_CURRENT_INDEX = "com.ufistudio.hotelmediabox.pages.roomService.template.current_index"
        private const val TAG_CONTENT = "com.ufistudio.hotelmediabox.pages.roomService.template.content"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mData = arguments?.getParcelable<RoomServiceCategories>(Page.ARG_BUNDLE)!!
        if (savedInstanceState != null) {
            mData = savedInstanceState.getParcelable(TAG_CONTENT)
            mCurrentIndex = savedInstanceState.getInt(TAG_CURRENT_INDEX)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_room_service_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mAdapter = TemplateType2PagerAdapter(context!!, mData)
        view_pager_content.adapter = mAdapter
        view_pager_content.addOnPageChangeListener(this)
        view_pager_content.setCurrentItem(mCurrentIndex, false)
        getInteractionListener().setOnKeyListener(this)
    }

    override fun onStop() {
        if (arguments != null)
            saveState(arguments!!)
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveState(outState)
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        mCurrentIndex = position
    }

    override fun onKeyPress(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (mCurrentIndex != 0)
                    mCurrentIndex--
                view_pager_content.setCurrentItem(mCurrentIndex, true)
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (getInteractionListener().getFragmentCacheData() as Boolean && !mFocus) {
                    view_pager_content.requestFocus()
                    mFocus = true
                    return true
                }
                if (mCurrentIndex != mData.contents.size - 1)
                    mCurrentIndex++
                view_pager_content.setCurrentItem(mCurrentIndex, true)
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                view_pager_content.clearFocus()
                mFocus = false
                return true
            }
        }
        return super.onFragmentKeyDown(keyCode, event)
    }

    private fun saveState(bundle: Bundle) {
        bundle.putInt(TAG_CURRENT_INDEX, mCurrentIndex)
        bundle.putParcelable(TAG_CONTENT, mData)
    }
}