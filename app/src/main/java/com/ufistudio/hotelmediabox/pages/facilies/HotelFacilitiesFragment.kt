package com.ufistudio.hotelmediabox.pages.facilies

import android.arch.lifecycle.Observer
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
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
import kotlinx.android.synthetic.main.fragment_facilities.*
import kotlinx.android.synthetic.main.view_bottom_back_home.*
import kotlinx.android.synthetic.main.view_bottom_ok_back_home.*

private const val TAG_IMAGE = "image"
private const val TAG_VIDEO = "video"

private const val TAG_TEMPLATE_1 = 3
private const val TAG_TEMPLATE_2 = 1
private const val TAG_TEMPLATE_3 = 2

class HotelFacilitiesFragment : InteractionView<OnPageInteractionListener.Primary>(), OnItemClickListener,
    OnItemFocusListener, ViewModelsCallback {
    private lateinit var mViewModel: HotelFacilitiesViewModel
    private var mAdapter: HotelFacilitiesAdapter = HotelFacilitiesAdapter(this, this)
    private var mCurrentCategoryIndex: Int = 0 //當前頁面category index
    private var mCurrentSideIndex: Int = -1 //當前頁面side view index
    private var mIsRendered: Boolean = false //判斷是否已經塞資料
    private var mSideViewState: HashMap<Int, String> = HashMap<Int, String>()//拿來儲存當前的sideView index與 Back上方的Title

    private var mSideViewFocus: Boolean = false
    private var mCategoryFocus: Boolean = false
    private var mContentFocus: Boolean = false //判斷目前focus是否在右邊的view
    private var mContentPlaying: Boolean = false //判斷目前是否有開始播放影片了

    private var mData: HotelFacilities? = null
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List

    private var mCurrentContentSelectIndex: HashMap<Int, Int>? =
        HashMap() //記錄當前在第幾個Item的Content, key = category index, value = content index
    private var mTotalSize: HashMap<Int, Int>? =
        HashMap()//所有category內容的size, key = category index, value = category content size
    private var mCurrentContent: List<HotelFacilitiesContent>? = null // 被選到的category內的Content
    private var mCurrentContentType: Int? = 0 // 被選到的content type
    private var mVideoFrame: ConstraintLayout? = null
    private var mVideoView1: PlayerView? = null
    private var mVideoView2: PlayerView? = null
    private var mVideoView3: PlayerView? = null

    private var mNoteBottom: NoteButton? = null//右下角提示資訊

    private var mExoPlayerHelper: ExoPlayerHelper = ExoPlayerHelper()

    companion object {
        fun newInstance(): HotelFacilitiesFragment = HotelFacilitiesFragment()
        private val TAG = HotelFacilitiesFragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initHotelFacilitiesProgress.observe(this, Observer { onProgress(it!!) })
        mViewModel.initHotelFacilitiesSuccess.observe(this, Observer {
            onSuccess(it)
        })
        mViewModel.initHotelFacilitiesError.observe(this, Observer {
            onError(it)
        })

        mHomeIcons = arguments?.getParcelableArrayList(Page.ARG_BUNDLE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_facilities, container, false)
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

    override fun onStart() {
        super.onStart()
        renderView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onPause() {
        mVideoView1?.visibility = View.GONE
        mVideoView2?.visibility = View.GONE
        mVideoView3?.visibility = View.GONE
        mExoPlayerHelper.stop()
        mExoPlayerHelper.release()
        super.onPause()
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN -> {
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
                    if (view_content_type1.isShown && mContentFocus) {
                        mVideoFrame?.setBackgroundResource(R.color.homeIconFrameFocused)
                    }
                } else if (mContentFocus) {
                    val curryIndex = mCurrentContentSelectIndex!![mCurrentCategoryIndex]!!
                    if (curryIndex < mTotalSize!![mCurrentCategoryIndex]!! - 1) {
                        mCurrentContentSelectIndex!![mCurrentCategoryIndex] = curryIndex + 1
                        switchRender()
                    }
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (mContentFocus) {
                    val curryIndex = mCurrentContentSelectIndex!![mCurrentCategoryIndex]!!
                    if (curryIndex != 0) {
                        mCurrentContentSelectIndex!![mCurrentCategoryIndex] = curryIndex - 1
                        switchRender()
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
                        if (view_content_type1.isShown)
                            mVideoFrame?.setBackgroundResource(R.color.videoBackground)
                    }
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                //TODO full screen image and video
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
            layout_back.visibility = View.GONE
            sideView.visibility = View.VISIBLE
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
            textView_ok.text = mNoteBottom?.note?.fullScreen
        }
    }

    /**
     * Init Side View
     */
    private fun initSideView() {
        mSideViewState.putAll(sideView.setFocus(mHomeIcons, HomeFeatureEnum.FACILITIES))
        mCurrentSideIndex = mSideViewState[ARG_CURRENT_INDEX]!!.toInt()
        text_back.text = mSideViewState[ARG_CURRENT_BACK_TITLE]
    }

    override fun onClick(view: View?) {
    }

    override fun onFoucsed(view: View?) {
        if (!mCategoryFocus || mContentPlaying) {
            return
        }
        val item = view?.getTag(HotelFacilitiesAdapter.TAG_ITEM) as HotelFacilitiesCategories
        val bundle = Bundle()
        bundle.putParcelable(Page.ARG_BUNDLE, item)
        mCurrentCategoryIndex = view.getTag(HotelFacilitiesAdapter.TAG_INDEX) as Int

        mCurrentContent = item.contents
        mCurrentContentType = item.content_type
        switchRender()
    }

    override fun onSuccess(it: Any?) {
        if (it != null) {
            val data: Pair<*, *> = it as Pair<*, *>
            mData = data.first as HotelFacilities
            mNoteBottom = data.second as NoteButton?
            renderView()
        }
    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "error: ${t?.message}")
    }

    override fun onProgress(b: Boolean) {
    }

    /**
     * 切換 content
     */
    private fun switchRender() {
        mExoPlayerHelper.stop()
        mVideoView1?.visibility = View.GONE
        mVideoView2?.visibility = View.GONE
        mVideoView3?.visibility = View.GONE
        when (mCurrentContentType) {
            TAG_TEMPLATE_1 -> renderTemplate1(mCurrentContent)
            TAG_TEMPLATE_2 -> renderTemplate2(mCurrentContent)
            TAG_TEMPLATE_3 -> renderTemplate3(mCurrentContent)
        }
    }

    /**
     * render template1 image/video + title + description
     */
    private fun renderTemplate1(list: List<HotelFacilitiesContent>?) {
        view_content_type1.visibility = View.VISIBLE
        view_content_type2.visibility = View.GONE
        view_content_type3.visibility = View.GONE

        checkArrow()
        showFullScreenBottomNote()

        val item = list!![mCurrentContentSelectIndex!![mCurrentCategoryIndex]!!]
        (view_content_type1.findViewById(R.id.text_total_page) as TextView).text = String.format("/%d", list.size)
        (view_content_type1.findViewById(R.id.text_current_page) as TextView).text =
            (mCurrentContentSelectIndex!![mCurrentCategoryIndex]!! + 1).toString()
        mVideoView1 = view_content_type1?.findViewById(R.id.videoView) as PlayerView
        val imageView = view_content_type1?.findViewById(R.id.image_photo) as ImageView
        mVideoFrame = view_content_type1?.findViewById(R.id.videoView_frame) as ConstraintLayout

        if (item.file_type.hashCode() == TAG_IMAGE.hashCode()) {
            mVideoView1?.visibility = View.GONE
            imageView?.visibility = View.VISIBLE
            if (imageView != null) {
                Glide.with(context!!)
                    .load(FileUtils.getFileFromStorage(item.file_name))
                    .skipMemoryCache(true)
                    .into(imageView)
            }
        } else if (item.file_type.hashCode() == TAG_VIDEO.hashCode()) {
            mContentPlaying = true
            if (mVideoView1 != null) {
                mExoPlayerHelper.initPlayer(context, mVideoView1!!)
                mExoPlayerHelper.setFileSource(Uri.parse(FileUtils.getFileFromStorage(item.file_name)?.absolutePath))
                mExoPlayerHelper.repeatMode()
                mVideoView1?.visibility = View.VISIBLE
            }
            imageView?.visibility = View.INVISIBLE
        } else {
            mVideoView1?.visibility = View.INVISIBLE
            imageView?.visibility = View.INVISIBLE
        }
    }

    /**
     * Render template2
     */
    private fun renderTemplate2(list: List<HotelFacilitiesContent>?) {
        view_content_type1.visibility = View.GONE
        view_content_type2.visibility = View.VISIBLE
        view_content_type3.visibility = View.GONE

        checkArrow()
        hideFullScreenBottomNote()

        val item = list!![mCurrentContentSelectIndex!![mCurrentCategoryIndex]!!]
        (view_content_type2.findViewById(R.id.text_total_page) as TextView).text = String.format("/%d", list.size)
        (view_content_type2.findViewById(R.id.text_current_page) as TextView).text = (mCurrentContentSelectIndex!![mCurrentCategoryIndex]!! + 1).toString()
        (view_content_type2.findViewById(R.id.text_title) as TextView).text = item.title
        (view_content_type2.findViewById(R.id.text_description) as TextView).text = item.content

        mVideoView2 = view_content_type2?.findViewById(R.id.videoView2) as PlayerView
        val imageView = view_content_type2?.findViewById(R.id.image_photo) as ImageView

        if (item.file_type.hashCode() == TAG_IMAGE.hashCode()) {
            mVideoView2?.visibility = View.GONE
            imageView?.visibility = View.VISIBLE
            if (imageView != null) {
                Glide.with(context!!)
                    .load(FileUtils.getFileFromStorage(item.file_name))
                    .skipMemoryCache(true)
                    .into(imageView)
            }
        } else if (item.file_type.hashCode() == TAG_VIDEO.hashCode()) {
            mContentPlaying = true
            if (mVideoView2 != null) {
                mExoPlayerHelper.initPlayer(context, mVideoView2!!)
                mExoPlayerHelper.setFileSource(Uri.parse(FileUtils.getFileFromStorage(item.file_name)?.absolutePath))
                mVideoView2?.visibility = View.VISIBLE
            }
            imageView?.visibility = View.INVISIBLE
        } else {
            mVideoView2?.visibility = View.INVISIBLE
            imageView?.visibility = View.INVISIBLE
        }
    }

    /**
     * Render template3
     */
    private fun renderTemplate3(list: List<HotelFacilitiesContent>?) {
        view_content_type1.visibility = View.GONE
        view_content_type2.visibility = View.GONE
        view_content_type3.visibility = View.VISIBLE

        checkArrow()
        hideFullScreenBottomNote()

        val item = list!![mCurrentContentSelectIndex!![mCurrentCategoryIndex]!!]
        (view_content_type3.findViewById(R.id.text_description) as TextView).text = item.content
        mVideoView3 = view_content_type3?.findViewById(R.id.videoView) as PlayerView
        val imageView = view_content_type3?.findViewById(R.id.image_photo) as ImageView



        if (item.file_type.hashCode() == TAG_IMAGE.hashCode()) {
            mVideoView3?.visibility = View.GONE
            imageView?.visibility = View.VISIBLE
            if (imageView != null) {
                Glide.with(context!!)
                    .load(FileUtils.getFileFromStorage(item.file_name))
                    .skipMemoryCache(true)
                    .into(imageView)
            }
        } else if (item.file_type.hashCode() == TAG_VIDEO.hashCode()) {
            mContentPlaying = true
            if (mVideoView3 != null) {
                mExoPlayerHelper.initPlayer(context, mVideoView3!!)
                mExoPlayerHelper.setFileSource(Uri.parse(FileUtils.getFileFromStorage(item.file_name)?.absolutePath))
                mVideoView3?.visibility = View.VISIBLE
            }
            imageView?.visibility = View.INVISIBLE
        } else {
            mVideoView3?.visibility = View.INVISIBLE
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

    private fun showFullScreenBottomNote() {
        if (!textView_ok.isShown)
            textView_ok.visibility = View.VISIBLE
        if (!imageView_ok.isShown)
            imageView_ok.visibility = View.VISIBLE
    }

    private fun hideFullScreenBottomNote() {
        if (textView_ok.isShown)
            textView_ok.visibility = View.GONE
        if (imageView_ok.isShown)
            imageView_ok.visibility = View.GONE
    }
}