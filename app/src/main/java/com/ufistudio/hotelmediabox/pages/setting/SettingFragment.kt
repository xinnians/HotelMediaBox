package com.ufistudio.hotelmediabox.pages.setting

import android.arch.lifecycle.Observer
import android.content.Intent
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
import com.ufistudio.hotelmediabox.pages.MainActivity
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.pages.home.HomeFeatureEnum
import com.ufistudio.hotelmediabox.repository.data.HomeIcons
import com.ufistudio.hotelmediabox.repository.data.Setting
import com.ufistudio.hotelmediabox.repository.data.SettingCategories
import kotlinx.android.synthetic.main.fragment_setting.*

class SettingFragment : InteractionView<OnPageInteractionListener.Primary>(), ViewModelsCallback, OnItemClickListener, OnItemFocusListener {
    private lateinit var mViewModel: SettingViewModel
    private var mData: Setting? = null
    private val mAdapter: SettingAdapter = SettingAdapter(this, this)
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List
    private var mCurrentSideIndex: Int = -1 //當前頁面side view index
    private var mCurrentCategoryIndex: Int = 0 //當前頁面category index

    private var mRendered: Boolean = false
    private var mSideViewFocus: Boolean = false
    private var mCategoryFocus: Boolean = false
    private var mContentFocus: Boolean = false
    private var mGuidLi: Boolean = false

    companion object {
        fun newInstance(): SettingFragment = SettingFragment()
        private val TAG = SettingFragment::class.simpleName
        private const val TAG_LANGUAGE: String = "1"
        private const val TAG_USER_GUIDE: String = "2"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initSettingServiceProgress.observe(this, Observer { })
        mViewModel.initSettingServiceSuccess.observe(this, Observer {
            onSuccess(it)
        })
        mViewModel.initSettingServiceError.observe(this, Observer { })

        mHomeIcons = arguments?.getParcelableArrayList(Page.ARG_BUNDLE)
        if (mHomeIcons != null) {
            for (i in 0 until mHomeIcons!!.size) {
                mCurrentSideIndex++
                if (mHomeIcons!![i].name == HomeFeatureEnum.SETTING.tag) {
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
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView_category.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView_category.adapter = mAdapter

        displaySideView(false)
        sideView.setAdapterList(mHomeIcons)
        sideView.setInteractionListener(getInteractionListener())
    }

    override fun onStart() {
        super.onStart()
        renderView()

    }

    override fun onClick(view: View?) {
    }

    override fun onSuccess(it: Any?) {
        if (it != null) {
            mData = it as Setting?
            renderView()
        }
    }

    override fun onError(t: Throwable?) {
        Log.e(TAG, "error = $t")
    }

    override fun onProgress(b: Boolean) {

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

    override fun onFoucsed(view: View?) {
        if (mCategoryFocus) {
            val category: SettingCategories = view?.getTag(SettingAdapter.TAG_ITEM) as SettingCategories
            mCurrentCategoryIndex = view.getTag(SettingAdapter.TAG_INDEX) as Int
            when (category.type) {
                TAG_LANGUAGE -> {
                    val bundle: Bundle = Bundle()
                    bundle.putParcelable(Page.ARG_BUNDLE, category.contents)
                    getInteractionListener().switchPage(R.id.fragment_sub_content, Page.LANGUAGE_SETTING, bundle, true, false)
                }
                TAG_USER_GUIDE -> {
                    val bundle: Bundle = Bundle()
                    bundle.putParcelable(Page.ARG_BUNDLE, category.contents)
                    getInteractionListener().switchPage(R.id.fragment_sub_content, Page.USER_GUIDE, bundle, false, false)
                }
            }
        }
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
                KeyEvent.KEYCODE_DPAD_UP -> {
                }
                KeyEvent.KEYCODE_BACK -> {
                    if (sideView.isShown) {
                        displaySideView(false)
                    } else {
                        displaySideView(true)
                    }
                    return true
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
                    } else {

                    }
                    return true
                }
                KeyEvent.KEYCODE_F1 -> {
                    startActivity(Intent(context, MainActivity::class.java))
                }
            }
        }
        return super.onFragmentKeyDown(keyCode, event)
    }

    private fun renderView() {
        if (!mRendered && mData?.categories != null) {
            mRendered = true
            mCategoryFocus = true
            mAdapter.selectLast(mCurrentCategoryIndex)
            mAdapter.setData(mData?.categories!!)
        }
    }
}