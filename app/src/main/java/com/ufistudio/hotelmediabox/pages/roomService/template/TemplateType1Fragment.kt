package com.ufistudio.hotelmediabox.pages

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.pages.roomService.template.TemplateType1PagerAdapter
import com.ufistudio.hotelmediabox.repository.data.RoomServiceCategories
import kotlinx.android.synthetic.main.fragment_room_service_content.*
import kotlinx.android.synthetic.main.fragment_room_service.*

class TemplateType1Fragment : InteractionView<OnPageInteractionListener.Primary>() {

    private lateinit var mViewModel: TemplateType1ViewModel
    private lateinit var mData: RoomServiceCategories


    companion object {
        fun newInstance(): TemplateType1Fragment = TemplateType1Fragment()
        private val TAG = TemplateType1Fragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mData = arguments?.getParcelable<RoomServiceCategories>(Page.ARG_BUNDLE)!!
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
        view_pager_content.adapter = TemplateType1PagerAdapter(context!!, mData)
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (sideView.visibility == View.VISIBLE) {
                }
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