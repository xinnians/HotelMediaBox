package com.ufistudio.hotelmediabox.pages.nearby

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
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
import com.ufistudio.hotelmediabox.repository.data.NearbyMe
import com.ufistudio.hotelmediabox.repository.data.NearbyMeCategories
import kotlinx.android.synthetic.main.fragment_room_service.*

class NearbyMeFragment : InteractionView<OnPageInteractionListener.Primary>(), OnItemClickListener,
        OnItemFocusListener, ViewModelsCallback {
    private lateinit var mViewModel: NearbyMeViewModel
    private var mAdapter: NearbyMeAdapter = NearbyMeAdapter(this, this)
    private var mInSubContent: Boolean = false //判斷目前focus是否在右邊的view
    private var mCategoryFocus: Boolean = false //判斷目前focus是否在category
    private var mCurrentCategoryIndex: Int = 0 //上一次List的選擇
    private var mCurrentSideIndex: Int = -1 //當前頁面side view index
    private var mIsRendered: Boolean = false //判斷是否已經塞資料
    private var mTextBackTitle: String = ""

    private var mData: NearbyMe? = null
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List

    companion object {
        fun newInstance(): NearbyMeFragment = NearbyMeFragment()
        private val TAG = NearbyMeFragment::class.simpleName
        private const val TAG_FOOD: String = "1"
        private const val TAG_SHOPPING: String = "2"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initNearbyMeProgress.observe(this, Observer { onProgress(it!!) })
        mViewModel.initNearbyMeSuccess.observe(this, Observer {
            onSuccess(it)
        })
        mViewModel.initNearbyMeError.observe(this, Observer {
            onError(it)
        })

        mHomeIcons = arguments?.getParcelableArrayList(Page.ARG_BUNDLE)
        if (mHomeIcons != null) {
            for (i in 0 until mHomeIcons!!.size) {
                mCurrentSideIndex++
                if (mHomeIcons!![i].name == HomeFeatureEnum.NEAR_BY.tag) {
                    mTextBackTitle = mHomeIcons!![i].name
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
        return inflater.inflate(R.layout.fragment_nearby_me, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView_service.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView_service.adapter = mAdapter

        displaySideView(false)
        sideView.setAdapterList(mHomeIcons)
        sideView.setInteractionListener(getInteractionListener())
    }

    override fun onStart() {
        super.onStart()
        renderView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (mInSubContent) {
            if (getInteractionListener().getOnKeyListener() != null && getInteractionListener().getOnKeyListener()?.onKeyPress(keyCode, event)!!) {
                when (keyCode) {
                    KeyEvent.KEYCODE_BACK -> {
                        mAdapter.selectLast(mCurrentCategoryIndex)
                        mCategoryFocus = true
                        mInSubContent = false
                    }
                }
                return true
            }
        } else {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (!sideView.isShown && mCategoryFocus) {
                        mAdapter.clearFocus(mCurrentCategoryIndex)
                        mCategoryFocus = false
                        mInSubContent = true
                        if (getInteractionListener().getOnKeyListener() != null) {
                            getInteractionListener().setFragmentCacheData(true)
                            getInteractionListener().getOnKeyListener()?.onKeyPress(keyCode, event)!!
                        }
                    }
                    return true
                }
                KeyEvent.KEYCODE_BACK -> {
                    if (sideView.isShown) {
                        displaySideView(false)
                    } else {
                        displaySideView(true)
                    }
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
            mAdapter.sideViewIsShow(true)
            mCategoryFocus = false
            mInSubContent = false
            Log.d("neo", "mcurrent side = $mCurrentSideIndex")
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

    /**
     * 塞資料
     */
    private fun renderView() {
        text_back.text = mTextBackTitle
        if (!mIsRendered && mData?.categories != null) {
            mIsRendered = true
            mCategoryFocus = true
            mAdapter.selectLast(mCurrentCategoryIndex)
            mAdapter.setData(mData?.categories!!)
        }
    }

    override fun onClick(view: View?) {
    }

    override fun onFoucsed(view: View?) {
        if (!mCategoryFocus)
            return
        val item = view?.getTag(NearbyMeAdapter.TAG_ITEM) as NearbyMeCategories
        val bundle = Bundle()
        mCurrentCategoryIndex = view.getTag(NearbyMeAdapter.TAG_INDEX) as Int
        bundle.putParcelableArrayList(Page.ARG_BUNDLE, item.contents)
        if (TextUtils.isEmpty(item.category_id))
            return
        when (item.category_id) {
            TAG_FOOD -> {
                getInteractionListener().switchPage(R.id.fragment_sub_content, Page.NEARBY_ME_FOOD, bundle, true, false)
            }
            TAG_SHOPPING -> {
                getInteractionListener().switchPage(R.id.fragment_sub_content, Page.NEARBY_ME_SHOPPING, bundle, true, false)
            }
        }
    }

    override fun onSuccess(it: Any?) {
        mData = it as NearbyMe
        renderView()
    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "error: ${t?.message}")
    }

    override fun onProgress(b: Boolean) {
    }
}