package com.ufistudio.hotelmediabox.pages.channel

import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import com.ufistudio.hotelmediabox.helper.TVController
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.pages.fullScreen.FullScreenActivity
import com.ufistudio.hotelmediabox.repository.data.TVChannel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import com.ufistudio.hotelmediabox.pages.home.HomeFeatureEnum
import com.ufistudio.hotelmediabox.repository.data.HomeIcons
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_BACK_TITLE
import com.ufistudio.hotelmediabox.views.ARG_CURRENT_INDEX
import kotlinx.android.synthetic.main.fragment_channel.*
import java.util.concurrent.TimeUnit

class ChannelFragment : InteractionView<OnPageInteractionListener.Primary>() {

    private lateinit var mViewModel: ChannelViewModel
    private lateinit var mExoPlayerHelper: ExoPlayerHelper

    private var mGenreAdapter: ChannelGenreAdapter = ChannelGenreAdapter()
    private var mChannelListAdapter: ChannelListAdapter = ChannelListAdapter()

    private var mGenreFocus: Boolean = false //Genre list 是否被focus
    private var mListFocus: Boolean = false //節目list 是否被focus
    private var mHomeIcons: ArrayList<HomeIcons>? = null //SideView List
    private var mDisposable: Disposable? = null
    private var mCurrentSideIndex: Int = -1 //當前頁面side view index
    private var mSideViewState: HashMap<Int, String> = HashMap<Int, String>()//拿來儲存當前的sideView index與 Back上方的Title
    private var mSideViewFocus: Boolean = false


    private var mTVListener: TVController.OnTVListener = object : TVController.OnTVListener{
        override fun onScanFinish() {

        }

        override fun onChannelChange(tvChannel: TVChannel?) {
        }

        override fun initDeviceFinish() {
        }

        override fun initAVPlayerFinish() {
            TVController.playCurrent()
        }

    }

    companion object {
        fun newInstance(): ChannelFragment = ChannelFragment()
        private val TAG = ChannelFragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = AppInjector.obtainViewModel(this)
        mExoPlayerHelper = ExoPlayerHelper()
        mHomeIcons = arguments?.getParcelableArrayList(Page.ARG_BUNDLE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_channel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        displaySideView(false)
        sideView.setAdapterList(mHomeIcons)
        sideView.setInteractionListener(getInteractionListener())
    }

    override fun onStart() {
        super.onStart()
        mChannelListAdapter.setItems(TVController.getChannelList())
    }

    override fun onResume() {
        super.onResume()
        mExoPlayerHelper.initPlayer(context, videoView)
        TVController.registerListener(mTVListener)
        TVController.initAVPlayer(TVController.SCREEN_TYPE.CHANNELPAGE)

        TVController.getCurrentChannel()?.let { mChannelListAdapter.setCurrentTVChannel(it) }
        view_channel_list?.scrollToPosition(mChannelListAdapter.getSelectPosition())
        switchFocus(false)
    }

    override fun onPause() {
        super.onPause()
//        mViewModel.getTVHelper().closeAVPlayer()
        TVController.releaseListener(mTVListener)
        TVController.deInitAVPlayer()
    }

    override fun onStop() {
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable?.dispose()
        }
        mExoPlayerHelper.release()
//        DVBHelper.getDVBPlayer().closePlayer()
        super.onStop()
    }

    private fun initView() {
        mSideViewState.putAll(sideView.setFocus(mHomeIcons, HomeFeatureEnum.LIVE_TV))
        mCurrentSideIndex = mSideViewState[ARG_CURRENT_INDEX]!!.toInt()
        text_back.text = mSideViewState[ARG_CURRENT_BACK_TITLE]
        dvbView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                holder?.setFormat(PixelFormat.TRANSPARENT)
            }
        })

        view_genre_list.layoutManager = LinearLayoutManager(context)
        view_genre_list.adapter = mGenreAdapter
        mGenreAdapter.setItemClickListener(object : ChannelGenreAdapter.OnItemClickListener {
            override fun onClick(view: View) {
                Log.e(TAG, "mGenreAdapter onClick()")
            }
        })


        view_channel_list.layoutManager = LinearLayoutManager(context)
        view_channel_list.adapter = mChannelListAdapter
        mChannelListAdapter.setItemClickListener(object : ChannelListAdapter.OnItemClickListener {
            override fun onClick(view: View) {
                Log.e(TAG, "mChannelListAdapter onClick()")
//                activity?.startActivity(Intent(activity, FullScreenActivity::class.java))
//                mExoPlayerHelper.fullScreen()

                startActivity(Intent(context, FullScreenActivity::class.java))
            }
        })
    }

    private fun onChannelSelectListener(channelInfo: TVChannel) {
        Log.e(TAG, "[onChannelSelectListener] channelInfo:$channelInfo")

        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable?.dispose()
        }

        mDisposable = Observable.timer(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {}, { onError -> Log.e(TAG, "error:$onError") }, {

                    if (channelInfo.chType == "DVBT") {

//                        mViewModel.getTVHelper().play(channelInfo).subscribe()
                        TVController.play(channelInfo)

                    } else {
                        mExoPlayerHelper.setMp4Source(R.raw.videoplayback, true)
                        mExoPlayerHelper.play()
                    }

                })


    }

    private fun switchFocus(isGenre: Boolean) {
        Log.e(TAG, "[switchFocus] isGenre:$isGenre")
        mGenreFocus = isGenre
        mListFocus = !isGenre

        mGenreAdapter.setFocus(isGenre)
        mChannelListAdapter.setFocus(!isGenre)
        if (!isGenre) {
            mChannelListAdapter.getCurrentTVChannel()?.let { channel ->
                Log.e(TAG, "[switchFocus] $channel")
                var name =
                        channel.chNum + ": " + channel.chName + " (${channel.chIp.frequency}mhz,${channel.chIp.dvbParameter})"
                text_channel_info.text = name
                onChannelSelectListener(channel)
            }

//            TVController.getCurrentChannel()?.let { channel ->
//                Log.e(TAG, "[switchFocus] $channel")
//                var name =
//                    channel.chNum + ": " + channel.chName + " (${channel.chIp.frequency}mhz,${channel.chIp.dvbParameter})"
//                text_channel_info.text = name
//                onChannelSelectListener(channel)
//            }
        }
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.e(TAG, "event:${event?.characters ?: ""}, keycode:$keyCode, GenreFocus:$mGenreFocus, ListFocus:$mListFocus, SideViewFocus:$mSideViewFocus")

        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (mGenreFocus) {
                    mChannelListAdapter.setGenreFilter(mGenreAdapter.selectDown().name)
                } else if (mListFocus) {
                    mChannelListAdapter.selectDownItem()?.let { channel ->
                        var name =
                            channel.chNum + ": " + channel.chName + " (${channel.chIp.frequency}mhz,${channel.chIp.dvbParameter})"
                        text_channel_info.text = name
                        onChannelSelectListener(channel) }
                    view_channel_list.scrollToPosition(mChannelListAdapter.getSelectPosition())
                } else if(mSideViewFocus){
                    if (sideView.getSelectPosition() > 0) {
                        sideView.setLastPosition(sideView.getSelectPosition() - 1)
                        sideView.scrollToPosition(sideView.getSelectPosition())
                    }
                }
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (mGenreFocus) {
                    mChannelListAdapter.setGenreFilter(mGenreAdapter.selectUp().name)
                } else if (mListFocus) {
                    mChannelListAdapter.selectUPItem()?.let { channel ->
                        var name =
                            channel.chNum + ": " + channel.chName + " (${channel.chIp.frequency}mhz,${channel.chIp.dvbParameter})"
                        text_channel_info.text = name
                        onChannelSelectListener(channel) }
                    view_channel_list.scrollToPosition(mChannelListAdapter.getSelectPosition())
                } else if(mSideViewFocus){
                    if (sideView.getSelectPosition() + 1 < sideView.getItemSize()) {
                        sideView.setLastPosition(sideView.getSelectPosition() + 1)
                        sideView.scrollToPosition(sideView.getSelectPosition())
                    }
                }
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                switchFocus(true)
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                switchFocus(false)
            }
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                if (!mGenreFocus) {
                    if (mSideViewFocus) {
                        sideView.intoPage()
                        return true
                    }
                    startActivity(Intent(context, FullScreenActivity::class.java))
                }
            }

            KeyEvent.KEYCODE_CHANNEL_UP -> {
            }
            KeyEvent.KEYCODE_CHANNEL_DOWN -> {
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

        return super.onFragmentKeyDown(keyCode, event)
    }

    private fun initChannelsSuccess(list: ArrayList<TVChannel>) {
        mChannelListAdapter.setItems(list)
        switchFocus(false)
    }

    private fun initChannelsProgress(isProgress: Boolean) {
        Log.e(TAG, "initChannelsProgress call. isProgress:$isProgress")
    }

    private fun initChannelsError(throwable: Throwable) {
        Log.e(TAG, "initChannelsError call. ${throwable.message}")
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
            switchFocus(false)
            mListFocus = false
            mSideViewFocus = true
            sideView.scrollToPosition(mCurrentSideIndex)
            sideView.setLastPosition(mCurrentSideIndex)
        } else {
            sideView.visibility = View.GONE
            layout_back.visibility = View.VISIBLE
            view_line.visibility = View.GONE
            switchFocus(true)
            mSideViewFocus = false
        }
    }
}