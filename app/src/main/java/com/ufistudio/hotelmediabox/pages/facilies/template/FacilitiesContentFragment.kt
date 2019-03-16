package com.ufistudio.hotelmediabox.pages.facilies.template

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.pages.TemplateType1ViewModel
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.repository.data.HotelFacilitiesCategories
import kotlinx.android.synthetic.main.fragment_room_service.*
import kotlinx.android.synthetic.main.fragment_room_service_content.*

class FacilitiesContentFragment : InteractionView<OnPageInteractionListener.Primary>() {

    private lateinit var mViewModel: TemplateType1ViewModel

    private lateinit var mData: HotelFacilitiesCategories


    companion object {
        fun newInstance(): FacilitiesContentFragment = FacilitiesContentFragment()
        private val TAG = FacilitiesContentFragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("neo","onCreate");
        mData = arguments?.getParcelable<HotelFacilitiesCategories>(Page.ARG_BUNDLE)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_room_service_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view_pager_content.adapter = HotelFacilitiesPagerAdapter(context!!, mData)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                return false
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                return false
            }
            KeyEvent.KEYCODE_BACK -> {
                if (sideView.visibility == View.GONE) {
                    return true
                }
            }
        }
        return super.onFragmentKeyDown(keyCode, event)
    }
}