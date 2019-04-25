package com.ufistudio.hotelmediabox.pages.vod

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.ui.PlayerView
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import com.ufistudio.hotelmediabox.interfaces.OnItemClickListener
import com.ufistudio.hotelmediabox.interfaces.OnItemFocusListener
import com.ufistudio.hotelmediabox.interfaces.ViewModelsCallback
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.pages.home.HomeFeatureEnum
import com.ufistudio.hotelmediabox.repository.data.*
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_BACK_TITLE
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_INDEX
import kotlinx.android.synthetic.main.fragment_vod.*
import kotlinx.android.synthetic.main.view_bottom_back_home.*
import kotlinx.android.synthetic.main.view_bottom_ok_back_home.*

private const val TAG_VIDEO = "video"

class VodFragment : InteractionView<OnPageInteractionListener.Primary>(), OnItemClickListener,
        OnItemFocusListener, ViewModelsCallback {
    private lateinit var mViewModel: VodViewModel
    private var mAdapter: VodAdapter = VodAdapter(this, this)
    private var mContentFocus: Boolean = false //判斷目前focus是否在右邊的view
    private var mCategoryFocus: Boolean = false //判斷目前focus是否在category
    private var mContentPlaying: Boolean = false //判斷目前是否有開始播放影片了
    private var mCurrentCategoryIndex: Int = 0 //上一次List的選擇
    private var mCurrentSideIndex: Int = -1 //當前頁面side view index
    private var mIsRendered: Boolean = false //判斷是否已經塞資料
    private var mSideViewState: HashMap<Int, String> = HashMap<Int, String>()//拿來儲存當前的sideView index與 Back上方的Title

    private var mData: Vod? = null
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List

    private var mCurrentContentSelectIndex: HashMap<Int, Int>? = HashMap() //記錄當前在第幾個Item的Content, key = category index, value = content index
    private var mTotalSize: HashMap<Int, Int>? = HashMap()//所有category內容的size, key = category index, value = category content size
    private var mCurrentContent: List<VodContent>? = null // 被選到的category內的Content

    private var mVideoView: PlayerView? = null
    private var mExoPlayerHelper: ExoPlayerHelper = ExoPlayerHelper()

    private var mNoteBottom: NoteButton? = null//右下角提示資訊

    companion object {
        fun newInstance(): VodFragment = VodFragment()
        private val TAG = VodFragment::class.simpleName
        private const val TAG_FOOD: String = "1"
        private const val TAG_SHOPPING: String = "2"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initVodProgress.observe(this, Observer { onProgress(it!!) })
        mViewModel.initVodSuccess.observe(this, Observer {
            onSuccess(it)
        })
        mViewModel.initVodError.observe(this, Observer {
            onError(it)
        })

        mHomeIcons = arguments?.getParcelableArrayList(Page.ARG_BUNDLE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_vod, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSideView()
        recyclerView_service.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView_service.adapter = mAdapter

        displaySideView(false)
        sideView.setAdapterList(mHomeIcons)
        sideView.setInteractionListener(getInteractionListener())
    }

    override fun onPause() {
        mVideoView?.visibility = View.GONE
        mExoPlayerHelper.stop()
        mExoPlayerHelper.release()
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        renderView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (mContentFocus) {
                    return true
                } else {
                    //若不是在ContentFocus，則將當前在播放的label設為false好讓focus可以更新
                    mContentPlaying = false
                }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (!sideView.isShown && mCategoryFocus) {
//                    mAdapter.clearFocus(mCurrentCategoryIndex)
//                    mCategoryFocus = false
//                    mContentFocus = true
                } else if (mContentFocus) {
//                    val curryIndex = mCurrentContentSelectIndex!![mCurrentCategoryIndex]!!
//                    if (curryIndex < mTotalSize!![mCurrentCategoryIndex]!! - 1) {
//                        mCurrentContentSelectIndex!![mCurrentCategoryIndex] = curryIndex + 1
//                        renderViewContent()
//                    }
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (mContentFocus) {
//                    val curryIndex = mCurrentContentSelectIndex!![mCurrentCategoryIndex]!!
//                    if (curryIndex != 0) {
//                        mCurrentContentSelectIndex!![mCurrentCategoryIndex] = curryIndex - 1
//                        renderViewContent()
//                    }
                    return true
                }
            }
            KeyEvent.KEYCODE_BACK -> {
                if (sideView.isShown) {
                    displaySideView(false)
                } else {
                    if (!mContentFocus)
                        displaySideView(true)
                    else {
                        mContentFocus = false
                        mCategoryFocus = true
                        mAdapter.selectLast(mCurrentCategoryIndex)
                    }
                }
                return true
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
            mContentFocus = false
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
        if (!mIsRendered) {
            if (mData?.categories != null) {
                mIsRendered = true
                mCategoryFocus = true
                for (i in 0 until mData?.categories!!.size) {
                    mCurrentContentSelectIndex!![i] = 0
                    mTotalSize!![i] = mData?.categories!![i].contents.size
                }
                mAdapter.selectLast(mCurrentCategoryIndex)
                mAdapter.setData(mData?.categories!!)
            }
            textView_back.text = mNoteBottom?.note?.back
            textView_home.text = mNoteBottom?.note?.home
            textView_ok.text = mNoteBottom?.note?.fullScreen
        }
    }

    override fun onClick(view: View?) {
    }

    /**
     * Init Side View
     */
    private fun initSideView() {
        mSideViewState.putAll(sideView.setFocus(mHomeIcons, HomeFeatureEnum.VOD))
        mCurrentSideIndex = mSideViewState[ARG_CURRENT_INDEX]!!.toInt()
        text_back.text = mSideViewState[ARG_CURRENT_BACK_TITLE]
    }

    override fun onFoucsed(view: View?) {
        if (!mCategoryFocus || mContentPlaying) {
            return
        }
        val item = view?.getTag(VodAdapter.TAG_ITEM) as VodCategories
        val bundle = Bundle()
        mCurrentCategoryIndex = view.getTag(VodAdapter.TAG_INDEX) as Int
        bundle.putParcelableArrayList(Page.ARG_BUNDLE, item.contents)
        mCurrentContent = item.contents

        renderViewContent()
    }

    override fun onSuccess(it: Any?) {
        if (it != null) {
            val data: Pair<*, *> = it as Pair<*, *>
            mData = data.first as Vod
            mNoteBottom = data.second as NoteButton?
            renderView()
        }
    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "error: ${t?.message}")
    }

    override fun onProgress(b: Boolean) {
    }

    private fun renderViewContent() {
        checkArrow()

        val item = mCurrentContent!![mCurrentContentSelectIndex!![mCurrentCategoryIndex]!!]

        mVideoView = videoView

//        if (item.file_type.hashCode() == TAG_IMAGE.hashCode()) {
        mContentPlaying = false
        mVideoView?.visibility = View.GONE
        textView_test_title.text = item.title
        if (mVideoView != null) {
            Log.d("neo", "vide test")
            mExoPlayerHelper.stop()
            mExoPlayerHelper.release()
            mExoPlayerHelper.initPlayer(context, mVideoView!!)
            mExoPlayerHelper.setUdpSource("udp://${item.ip}:${item.port}")
            mVideoView?.visibility = View.VISIBLE
            mContentPlaying = true
        }
//        } else {
//            mVideoView?.visibility = View.INVISIBLE
//        }
    }

    /**
     * 判斷左右箭頭
     */
    private fun checkArrow() {
        when {
            mCurrentContent?.size == 1 -> {
                imageView_arrow_left.visibility = View.INVISIBLE
                imageView_arrow_right.visibility = View.INVISIBLE
            }
            mCurrentContentSelectIndex!![mCurrentCategoryIndex] == 0 -> {
                imageView_arrow_left.visibility = View.INVISIBLE
                imageView_arrow_right.visibility = View.VISIBLE
            }
            mCurrentContentSelectIndex!![mCurrentCategoryIndex] == mCurrentContent!!.size - 1 -> {
                imageView_arrow_left.visibility = View.VISIBLE
                imageView_arrow_right.visibility = View.INVISIBLE
            }
            else -> {
                imageView_arrow_left.visibility = View.VISIBLE
                imageView_arrow_right.visibility = View.VISIBLE
            }
        }
    }
}