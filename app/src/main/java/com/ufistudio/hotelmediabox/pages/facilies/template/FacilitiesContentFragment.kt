package com.ufistudio.hotelmediabox.pages.facilies.template

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.interfaces.OnFragmentKeyListener
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.repository.data.HotelFacilitiesCategories
import kotlinx.android.synthetic.main.fragment_room_service_content.*

class FacilitiesContentFragment : InteractionView<OnPageInteractionListener.Primary>(), OnFragmentKeyListener {

    private lateinit var mData: HotelFacilitiesCategories
    private var mCurrentIndex: Int = 0
    private var mCurrentContentIndex: Int = 0
    private var mFocus: Boolean = false
    private var mAdapter: HotelFacilitiesPagerAdapter? = null


    companion object {
        fun newInstance(): FacilitiesContentFragment = FacilitiesContentFragment()
        private val TAG = FacilitiesContentFragment::class.simpleName
        private const val TAG_CURRENT_INDEX = "com.ufistudio.hotelmediabox.pages.facilies.template.current_index"
        private const val TAG_CURRENT_CONTENT_INDEX = "com.ufistudio.hotelmediabox.pages.facilies.template.current_content_index"
        private const val TAG_CONTENT = "com.ufistudio.hotelmediabox.pages.nearby.template.content"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mData = arguments?.getParcelable<HotelFacilitiesCategories>(Page.ARG_BUNDLE)!!
        val gson= Gson()
        if (savedInstanceState != null) {
            mData = savedInstanceState.getParcelable(TAG_CONTENT)
            mCurrentIndex = savedInstanceState.getInt(TAG_CURRENT_INDEX)
            mCurrentContentIndex = savedInstanceState.getInt(TAG_CURRENT_CONTENT_INDEX)
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
        mAdapter = HotelFacilitiesPagerAdapter(context!!, mData)
        view_pager_content.adapter = mAdapter
        getInteractionListener().setOnKeyListener(this)

    }

    override fun onResume() {
        super.onResume()
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
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (getInteractionListener().getFragmentCacheData() as Boolean && !mFocus) {
                    view_pager_content.requestFocus()
                    mFocus = true
                    return true
                }
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                view_pager_content.clearFocus()
                mFocus = false
                return true
            }
        }
        return false
    }


    private fun saveState(bundle: Bundle) {
        bundle.putInt(TAG_CURRENT_INDEX, mCurrentIndex)
        bundle.putInt(TAG_CURRENT_CONTENT_INDEX, mCurrentIndex)
        bundle.putParcelable(TAG_CONTENT, mData)
    }
}