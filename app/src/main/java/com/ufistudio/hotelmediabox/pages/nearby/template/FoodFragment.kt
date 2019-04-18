package com.ufistudio.hotelmediabox.pages.nearby.template

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
import com.ufistudio.hotelmediabox.repository.data.NearbyMeContent
import kotlinx.android.synthetic.main.fragment_nearby_me_food.*

class FoodFragment : InteractionView<OnPageInteractionListener.Primary>(), OnFragmentKeyListener, ViewPager.OnPageChangeListener {
    private lateinit var mData: ArrayList<NearbyMeContent>
    private var mCurrentIndex: Int = 0
    private var mAdapter: FoodPagerAdapter? = null
    private var mFocus: Boolean = false


    companion object {
        fun newInstance(): FoodFragment = FoodFragment()
        private val TAG = FoodFragment::class.simpleName
        private const val TAG_CURRENT_INDEX = "com.ufistudio.hotelmediabox.pages.nearby.template.current_index"
        private const val TAG_CONTENT = "com.ufistudio.hotelmediabox.pages.nearby.template.content"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mData = arguments?.getParcelableArrayList(Page.ARG_BUNDLE)!!
        if (savedInstanceState != null) {
            mData = savedInstanceState.getParcelableArrayList(TAG_CONTENT)
            mCurrentIndex = savedInstanceState.getInt(TAG_CURRENT_INDEX)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_nearby_me_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mAdapter = FoodPagerAdapter(context!!, mData)
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
                if (mCurrentIndex != mData.size - 1)
                    mCurrentIndex++
                view_pager_content.setCurrentItem(mCurrentIndex, true)
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                mAdapter?.clearFocus()
                mFocus = false
                return true
            }
        }
        return false
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        mCurrentIndex = position
    }

    private fun saveState(bundle: Bundle) {
        bundle.putInt(TAG_CURRENT_INDEX, mCurrentIndex)
        bundle.putParcelableArrayList(TAG_CONTENT, mData)
    }

}