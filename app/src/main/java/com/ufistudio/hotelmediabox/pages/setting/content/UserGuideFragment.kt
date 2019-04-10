package com.ufistudio.hotelmediabox.pages.setting.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import android.view.KeyEvent
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.interfaces.OnFragmentKeyListener
import com.ufistudio.hotelmediabox.repository.data.*
import com.ufistudio.hotelmediabox.utils.FileUtils
import kotlinx.android.synthetic.main.fragment_user_guide.*

class UserGuideFragment : InteractionView<OnPageInteractionListener.Primary>(), OnFragmentKeyListener {
    private var mData: SettingContent? = null
    private var mIsRendered: Boolean = false //判斷塞資料了沒


    companion object {
        fun newInstance(): UserGuideFragment = UserGuideFragment()
        private val TAG = UserGuideFragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mData = arguments?.getParcelable<SettingContent>(Page.ARG_BUNDLE)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_user_guide, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mData = arguments?.getParcelable<SettingContent>(Page.ARG_BUNDLE)!!

    }

    override fun onStart() {
        super.onStart()
        renderView()
        getInteractionListener().setOnKeyListener(this)
    }

    override fun onKeyPress(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                return true
            }
        }
        return false
    }


    /**
     * 塞資料
     */
    private fun renderView() {
        if (!mIsRendered) {
            if (mData?.image != null) {

                Glide.with(context!!)
                        .load(FileUtils.getFileFromStorage(mData?.image!!))
                        .skipMemoryCache(true)
                        .into(imageView)

            }
        }
    }
}