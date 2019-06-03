package com.ufistudio.hotelmediabox.pages.nearby

import android.arch.lifecycle.Observer
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
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
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_BACK_TITLE
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_INDEX
import kotlinx.android.synthetic.main.fragment_nearby_me.*
import kotlinx.android.synthetic.main.view_bottom_back_home.*
import kotlinx.android.synthetic.main.view_bottom_ok_back_home.*

private const val TAG_IMAGE = "image"
private const val TAG_VIDEO = "video"

class NearbyMeFragment : InteractionView<OnPageInteractionListener.Primary>(), OnItemClickListener,
        OnItemFocusListener, ViewModelsCallback {
    private lateinit var mViewModel: NearbyMeViewModel
    private var mAdapter: NearbyMeAdapter = NearbyMeAdapter(this, this)
    private var mContentFocus: Boolean = false //判斷目前focus是否在右邊的view
    private var mCategoryFocus: Boolean = false //判斷目前focus是否在category
    private var mContentPlaying: Boolean = false //判斷目前是否有開始播放影片了
    private var mCurrentCategoryIndex: Int = 0 //上一次List的選擇
    private var mCurrentSideIndex: Int = -1 //當前頁面side view index
    private var mIsRendered: Boolean = false //判斷是否已經塞資料
    private var mSideViewState: HashMap<Int, String> = HashMap<Int, String>()//拿來儲存當前的sideView index與 Back上方的Title

    private var mData: NearbyMe? = null
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List

    private var mCurrentContentSelectIndex: HashMap<Int, Int>? = HashMap() //記錄當前在第幾個Item的Content, key = category index, value = content index
    private var mTotalSize: HashMap<Int, Int>? = HashMap()//所有category內容的size, key = category index, value = category content size
    private var mCurrentContent: List<NearbyMeContent>? = null // 被選到的category內的Content

    private var mVideoView: PlayerView? = null
    private var mExoPlayerHelper: ExoPlayerHelper = ExoPlayerHelper()

    private var mNoteBottom: NoteButton? = null//右下角提示資訊
    private var mSideViewFocus: Boolean = false


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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_nearby_me, container, false)
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
            KeyEvent.KEYCODE_DPAD_DOWN ->{
                if (mContentFocus) {
                    return true
                } else {
                    if (mSideViewFocus) {
                        if (sideView.getSelectPosition() + 1 < sideView.getItemSize()) {
                            sideView.setLastPosition(sideView.getSelectPosition() + 1)
                            sideView.scrollToPosition(sideView.getSelectPosition())
                        }
                    } else {
                        //若不是在ContentFocus，則將當前在播放的label設為false好讓focus可以更新
                        mContentPlaying = false
                        mData?.categories?.let {
                            if (mAdapter.getLastPosition() + 1 < it.size) {
                                mAdapter.setSelectPosition(mAdapter.getLastPosition() + 1)
                                recyclerView_service.scrollToPosition(mAdapter.getLastPosition())
                                mAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (mContentFocus) {
                    return true
                } else {
                    if (mSideViewFocus) {
                        if (sideView.getSelectPosition() > 0) {
                            sideView.setLastPosition(sideView.getSelectPosition() - 1)
                            sideView.scrollToPosition(sideView.getSelectPosition())
                        }
                    } else {
                        //若不是在ContentFocus，則將當前在播放的label設為false好讓focus可以更新
                        mContentPlaying = false
                        mData?.categories?.let {
                            if (mAdapter.getLastPosition() > 0) {
                                mAdapter.setSelectPosition(mAdapter.getLastPosition() - 1)
                                recyclerView_service.scrollToPosition(mAdapter.getLastPosition())
                                mAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (!sideView.isShown && mCategoryFocus) {
                    mAdapter.clearFocus(mCurrentCategoryIndex)
                    mCategoryFocus = false
                    mContentFocus = true
                } else if (mContentFocus) {
                    val curryIndex = mCurrentContentSelectIndex!![mCurrentCategoryIndex]!!
                    if (curryIndex < mTotalSize!![mCurrentCategoryIndex]!! - 1) {
                        mCurrentContentSelectIndex!![mCurrentCategoryIndex] = curryIndex + 1
                        renderViewContent()
                    }
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (mContentFocus) {
                    val curryIndex = mCurrentContentSelectIndex!![mCurrentCategoryIndex]!!
                    if (curryIndex != 0) {
                        mCurrentContentSelectIndex!![mCurrentCategoryIndex] = curryIndex - 1
                        renderViewContent()
                    }
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
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                if (mSideViewFocus) {
                    sideView.intoPage()
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
            mContentFocus = false
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
        }
    }

    override fun onClick(view: View?) {
    }

    /**
     * Init Side View
     */
    private fun initSideView() {
        mSideViewState.putAll(sideView.setFocus(mHomeIcons, HomeFeatureEnum.NEAR_BY))
        mCurrentSideIndex = mSideViewState[ARG_CURRENT_INDEX]!!.toInt()
        text_back.text = mSideViewState[ARG_CURRENT_BACK_TITLE]
    }

    override fun onFoucsed(view: View?) {
        if (!mCategoryFocus || mContentPlaying) {
            return
        }
        val item = view?.getTag(NearbyMeAdapter.TAG_ITEM) as NearbyMeCategories
        val bundle = Bundle()
        mCurrentCategoryIndex = view.getTag(NearbyMeAdapter.TAG_INDEX) as Int
        bundle.putParcelableArrayList(Page.ARG_BUNDLE, item.contents)
        mCurrentContent = item.contents

        renderViewContent()
    }

    override fun onSuccess(it: Any?) {
        if (it != null) {
            val data: Pair<*, *> = it as Pair<*, *>
            mData = data.first as NearbyMe
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
        (view_content.findViewById(R.id.text_title) as TextView).text = item.title
        val mTextViewContent = view_content.findViewById(R.id.text_content) as TextView
//        mTextViewContent?.movementMethod = ScrollingMovementMethod()
        mTextViewContent?.text = item.content
        (view_content.findViewById(R.id.text_current_page) as TextView).text = (mCurrentContentSelectIndex!![mCurrentCategoryIndex]!! + 1).toString()
        (view_content.findViewById(R.id.text_total_page) as TextView).text = String.format("/%d", mCurrentContent!!.size)

        val imageView = view_content.findViewById(R.id.image_content) as ImageView
        mVideoView = view_content.findViewById(R.id.videoView) as PlayerView

        mExoPlayerHelper.stop()
        mExoPlayerHelper.release()
        if (item.file_type.hashCode() == TAG_IMAGE.hashCode()) {
            mContentPlaying = false
            mVideoView?.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            if (imageView != null) {

                Glide.with(context!!)
                        .load(FileUtils.getFileFromStorage(item.file_name))
                        .skipMemoryCache(true)
                        .into(imageView)
            }
        } else if (item.file_type.hashCode() == TAG_VIDEO.hashCode()) {

            if (mVideoView != null) {
                mExoPlayerHelper.initPlayer(context, mVideoView!!)
                mExoPlayerHelper.setFileSource(Uri.parse(FileUtils.getFileFromStorage(item.file_name)?.absolutePath))
                mVideoView?.visibility = View.VISIBLE
                mContentPlaying = true
            }
            imageView?.visibility = View.INVISIBLE
        } else {
            mVideoView?.visibility = View.INVISIBLE
            imageView?.visibility = View.INVISIBLE
        }
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