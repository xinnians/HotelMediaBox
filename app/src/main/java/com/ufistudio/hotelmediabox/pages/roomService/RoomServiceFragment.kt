package com.ufistudio.hotelmediabox.pages.roomService

import android.arch.lifecycle.Observer
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
import com.ufistudio.hotelmediabox.pages.roomService.template.TemplateType2RecyclerViewAdapter
import com.ufistudio.hotelmediabox.repository.data.HomeIcons
import com.ufistudio.hotelmediabox.repository.data.RoomServiceCategories
import com.ufistudio.hotelmediabox.repository.data.RoomServiceContent
import com.ufistudio.hotelmediabox.repository.data.RoomServices
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_BACK_TITLE
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_INDEX
import kotlinx.android.synthetic.main.fragment_room_service.*

private const val TAG_IMAGE = "image"
private const val TAG_VIDEO = "video"
private const val TAG_TEMPLATE1 = 1
private const val TAG_TEMPLATE2 = 2

class RoomServiceFragment : InteractionView<OnPageInteractionListener.Primary>(), OnItemClickListener,
        OnItemFocusListener, ViewModelsCallback {
    private lateinit var mViewModel: RoomServiceViewModel
    private var mAdapter: RoomServiceAdapter = RoomServiceAdapter(this, this)
    //    private var mLastSelectIndex: Int = 0 //上一次List的選擇
    private var mCurrentSideIndex: Int = -1 //當前SideView index
    private var mCurrentCategoryIndex: Int = 0 //當前頁面category index
    private var mData: RoomServices? = null
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List
    private var mIsRendered: Boolean = false //判斷是否已經塞資料
    private var mContentFocus: Boolean = false //判斷目前focus是否在右邊的view
    private var mCategoryFocus: Boolean = false
    private var mContentPlaying: Boolean = false //判斷目前是否有開始播放影片了
    private var mSideViewState: HashMap<Int, String> = HashMap<Int, String>()//拿來儲存當前的sideView index與 Back上方的Title
    private val mTemplate2Adapter: TemplateType2RecyclerViewAdapter = TemplateType2RecyclerViewAdapter()

    private var mCurrentContentSelectIndex: HashMap<Int, Int>? = HashMap() //記錄當前在第幾個Item的Content, key = category index, value = content index
    private var mTotalSize: HashMap<Int, Int>? = HashMap()//所有category內容的size, key = category index, value = category content size
    private var mCurrentContent: List<RoomServiceContent>? = null // 被選到的category內的Content
    private var mCurrentContentType: Int? = 0 // 被選到的content type
    private var mTemplate2BottomNote: String = ""//template2 底下的提示字

    private var mExoPlayerHelper: ExoPlayerHelper = ExoPlayerHelper()

    companion object {
        fun newInstance(): RoomServiceFragment = RoomServiceFragment()
        private val TAG = RoomServiceFragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initRoomServiceProgress.observe(this, Observer { onProgress(it!!) })
        mViewModel.initRoomServiceSuccess.observe(this, Observer {
            onSuccess(it)
        })
        mViewModel.initRoomServiceError.observe(this, Observer { onError(it) })

        mHomeIcons = arguments?.getParcelableArrayList(Page.ARG_BUNDLE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_room_service, container, false)
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

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (mContentFocus) {
                    return true
                } else {
                    //若不是在ContentFocus，則將當前在播放的label設為false好讓focus可以更新
                    mContentPlaying = false
                }
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
                    if (mData?.categories!![i].content_type == TAG_TEMPLATE2)
                        mTotalSize!![i] = mData?.categories!![i].contents.size / 3 + 1
                    else
                        mTotalSize!![i] = mData?.categories!![i].contents.size
                }
                mAdapter.selectLast(mCurrentCategoryIndex)
                mAdapter.setData(mData?.categories!!)
            }
        }
    }

    /**
     * Init Side View
     */
    private fun initSideView() {
        mSideViewState.putAll(sideView.setFocus(mHomeIcons, HomeFeatureEnum.ROOM_SERVICE))
        mCurrentSideIndex = mSideViewState[ARG_CURRENT_INDEX]!!.toInt()
        text_back.text = mSideViewState[ARG_CURRENT_BACK_TITLE]
    }

    override fun onClick(view: View?) {
    }

    override fun onFoucsed(view: View?) {
        if (!mCategoryFocus || mContentPlaying)
            return
        val item = view?.getTag(RoomServiceAdapter.TAG_ITEM) as RoomServiceCategories
        val bundle = Bundle()
        mCurrentCategoryIndex = view.getTag(RoomServiceAdapter.TAG_INDEX) as Int
        bundle.putParcelable(Page.ARG_BUNDLE, item)
        val itemData = view.getTag(RoomServiceAdapter.TAG_ITEM) as RoomServiceCategories
        mCurrentContent = itemData.contents
        mCurrentContentType = itemData.content_type
        mTemplate2BottomNote = itemData.bottomNote
        switchRender()
    }

    override fun onSuccess(it: Any?) {
        mData = it as RoomServices
        renderView()
    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "Error = ${t?.message}")
    }

    override fun onProgress(b: Boolean) {
    }

    /**
     * 切換 content
     */
    private fun switchRender() {
        mExoPlayerHelper.stop()
        mExoPlayerHelper.release()
        when (mCurrentContentType) {
            TAG_TEMPLATE1 -> renderTemplate1((mCurrentContent as ArrayList<RoomServiceContent>)[mCurrentContentSelectIndex?.get(mCurrentCategoryIndex)!!])
            TAG_TEMPLATE2 -> renderTemplate2(mCurrentContent)
        }
    }

    /**
     * render template1 image/video + title + description
     */
    private fun renderTemplate1(item: RoomServiceContent) {
        view_content_type1.visibility = View.VISIBLE
        view_content_type2.visibility = View.GONE
        checkArrow()
        view_content_type1?.findViewById<TextView>(R.id.text_title)!!.text = item.title
        view_content_type1?.findViewById<TextView>(R.id.text_content)!!.text = item.content
        view_content_type1?.findViewById<TextView>(R.id.text_current_page)!!.text = (mCurrentContentSelectIndex?.get(mCurrentCategoryIndex)!! + 1).toString()
        view_content_type1?.findViewById<TextView>(R.id.text_total_page)!!.text = String.format("/%d", mTotalSize?.get(mCurrentCategoryIndex))
        val videoView = view_content_type1?.findViewById<PlayerView>(R.id.videoView)
        val imageView = view_content_type1?.findViewById<ImageView>(R.id.image_content)

        if (item.file_type.hashCode() == TAG_IMAGE.hashCode()) {
            videoView?.visibility = View.GONE
            imageView?.visibility = View.VISIBLE
            Glide.with(context!!)
                    .load(FileUtils.getFileFromStorage(item.file_name))
                    .skipMemoryCache(true)
                    .into(imageView!!)
        } else if (item.file_type.hashCode() == TAG_VIDEO.hashCode()) {
            mContentPlaying = true
            mExoPlayerHelper.initPlayer(context, videoView!!)
            mExoPlayerHelper.setFileSource(Uri.parse(FileUtils.getFileFromStorage(item.file_name)?.absolutePath))
            videoView.visibility = View.VISIBLE
            imageView?.visibility = View.INVISIBLE
        }
    }

    /**
     * Render template2
     */
    private fun renderTemplate2(totalList: List<RoomServiceContent>?) {
        view_content_type1.visibility = View.GONE
        view_content_type2.visibility = View.VISIBLE

        checkArrow()
        val listData = ArrayList<RoomServiceContent>()
        for (i in 0..2) {
            if (totalList?.size!! - 1 >= i + mCurrentContentSelectIndex!![mCurrentCategoryIndex]!! * 3)
                listData.add(totalList[i + mCurrentContentSelectIndex!![mCurrentCategoryIndex]!! * 3])
        }
        mTemplate2Adapter.setData(listData)
        val recyclerView: RecyclerView = view_content_type2?.findViewById<RecyclerView>(R.id.recyclerview_content)!!
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = mTemplate2Adapter

        view?.findViewById<TextView>(R.id.textview_bottom_note)?.text = mTemplate2BottomNote
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
            //因應Template2 是3個資料為一頁做的調整，第二行為一般判斷
            mData?.categories!![mCurrentCategoryIndex].content_type == TAG_TEMPLATE2 && mCurrentContentSelectIndex!![mCurrentCategoryIndex] == mCurrentContent!!.size / 3 ||
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