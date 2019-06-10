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
import com.ufistudio.hotelmediabox.repository.data.NoteButton
import com.ufistudio.hotelmediabox.repository.data.Setting
import com.ufistudio.hotelmediabox.repository.data.SettingCategories
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_BACK_TITLE
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_INDEX
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.fragment_setting.layout_back
import kotlinx.android.synthetic.main.fragment_setting.sideView
import kotlinx.android.synthetic.main.fragment_setting.text_back
import kotlinx.android.synthetic.main.fragment_setting.view_line
import kotlinx.android.synthetic.main.fragment_weather.*
import kotlinx.android.synthetic.main.view_bottom_back_home.*
import kotlinx.android.synthetic.main.view_bottom_ok_back_home.*
import kotlinx.android.synthetic.main.view_bottom_up_down_ok_back_home.*

class SettingFragment : InteractionView<OnPageInteractionListener.Primary>(), ViewModelsCallback, OnItemClickListener, OnItemFocusListener {
    private lateinit var mViewModel: SettingViewModel
    private var mData: Setting? = null
    private val mAdapter: SettingAdapter = SettingAdapter(this, this)
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List
    private var mCurrentSideIndex: Int = -1 //當前頁面side view index
    private var mCurrentCategoryIndex: Int = 0 //當前頁面category index
    private var mSideViewState: HashMap<Int, String> = HashMap<Int, String>()//拿來儲存當前的sideView index與 Back上方的Title

    private var mRendered: Boolean = false
    private var mSideViewFocus: Boolean = false
    private var mCategoryFocus: Boolean = false
    private var mContentFocus: Boolean = false
    private var mGuidLi: Boolean = false
    private var mIsUserGuideFocus: Boolean = false

    private var mNoteBottom: NoteButton? = null//右下角提示資訊

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSideView()
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
            val data: Pair<*, *> = it as Pair<*, *>
            mData = data.first as Setting?
            mNoteBottom = data.second as NoteButton?
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
            mSideViewFocus = true
            sideView.scrollToPosition(mCurrentSideIndex)
            sideView.setLastPosition(mCurrentSideIndex)
        } else {
            sideView.visibility = View.GONE
            layout_back.visibility = View.VISIBLE
            view_line.visibility = View.GONE
            mAdapter.fromSideViewBack(mCurrentCategoryIndex)
            mCategoryFocus = true
            mSideViewFocus = false
        }
    }

    override fun onFoucsed(view: View?) {
        if (mCategoryFocus) {
            val category: SettingCategories = view?.getTag(SettingAdapter.TAG_ITEM) as SettingCategories
            mCurrentCategoryIndex = view.getTag(SettingAdapter.TAG_INDEX) as Int
            when (category.type) {
                TAG_LANGUAGE -> {
                    mIsUserGuideFocus = false
                    val bundle: Bundle = Bundle()
                    bundle.putParcelable(Page.ARG_BUNDLE, category.contents)
                    getInteractionListener().switchPage(R.id.fragment_sub_content, Page.LANGUAGE_SETTING, bundle, true, false)
                    showSelectBottomNote()
                }
                TAG_USER_GUIDE -> {
                    mIsUserGuideFocus = true
                    val bundle: Bundle = Bundle()
                    bundle.putParcelable(Page.ARG_BUNDLE, category.contents)
                    getInteractionListener().switchPage(R.id.fragment_sub_content, Page.USER_GUIDE, bundle, true, false)
                    showBasicBottomNote()
                }
            }
        }
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (mContentFocus && !mIsUserGuideFocus) {
            if (getInteractionListener().getOnKeyListener() != null && getInteractionListener().getOnKeyListener()?.onKeyPress(keyCode, event)!!) {
                when (keyCode) {
                    KeyEvent.KEYCODE_BACK -> {
                        mAdapter.selectLast(mCurrentCategoryIndex)
                        mCategoryFocus = true
                        mContentFocus = false
                    }
                    KeyEvent.KEYCODE_DPAD_CENTER -> {
                        val intent: Intent = Intent(activity, MainActivity::class.java)
                        val bundle: Bundle = Bundle()
                        bundle.putBoolean(Page.ARG_BUNDLE, true)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                }
                return true
            }
        } else {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (mSideViewFocus) {
                        if (sideView.getSelectPosition() > 0) {
                            sideView.setLastPosition(sideView.getSelectPosition() - 1)
                            sideView.scrollToPosition(sideView.getSelectPosition())
                        }
                    } else {
                        mData?.categories?.let {
                            if (mAdapter.getLastPosition() > 0) {
                                mAdapter.setSelectPosition(mAdapter.getLastPosition() - 1)
                                recyclerView_category.scrollToPosition(mAdapter.getLastPosition())
                                mAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (mSideViewFocus) {
                        if (sideView.getSelectPosition() + 1 < sideView.getItemSize()) {
                            sideView.setLastPosition(sideView.getSelectPosition() + 1)
                            sideView.scrollToPosition(sideView.getSelectPosition())
                        }
                    } else {
                        mData?.categories?.let {
                            if (mAdapter.getLastPosition() + 1 < it.size) {
                                mAdapter.setSelectPosition(mAdapter.getLastPosition() + 1)
                                recyclerView_category.scrollToPosition(mAdapter.getLastPosition())
                                mAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER -> {
                    if (mSideViewFocus) {
                        sideView.intoPage()
                        return true
                    }
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
                    if (!sideView.isShown && mCategoryFocus && !mIsUserGuideFocus) {
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

        textView_back.text = mNoteBottom?.note?.back
        textView_home.text = mNoteBottom?.note?.home
        textView_ok.text = mNoteBottom?.note?.select
        textView_up_down.text = mNoteBottom?.note?.toScroll
    }

    /**
     * Init Side View
     */
    private fun initSideView() {
        mSideViewState.putAll(sideView.setFocus(mHomeIcons, HomeFeatureEnum.SETTING))
        mCurrentSideIndex = mSideViewState[ARG_CURRENT_INDEX]!!.toInt()
        text_back.text = mSideViewState[ARG_CURRENT_BACK_TITLE]
    }

    /**
     * show only [home back]
     */
    private fun showBasicBottomNote() {
        textView_ok.visibility = View.GONE
        imageView_ok.visibility = View.GONE
        textView_up_down.visibility = View.GONE
        imageView_up_down.visibility = View.GONE
    }

    /**
     * show only [scroll select home back]
     */
    private fun showSelectBottomNote() {
        textView_ok.visibility = View.VISIBLE
        imageView_ok.visibility = View.VISIBLE
        textView_up_down.visibility = View.VISIBLE
        imageView_up_down.visibility = View.VISIBLE
    }
}