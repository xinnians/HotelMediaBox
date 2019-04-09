package com.ufistudio.hotelmediabox.pages.channel

import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.view.View.*
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.DVBHelper
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.helper.ExoPlayerHelper
import com.ufistudio.hotelmediabox.helper.TVHelper
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.pages.fullScreen.FullScreenActivity
import com.ufistudio.hotelmediabox.repository.data.TVChannel
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import com.ufistudio.hotelmediabox.pages.home.HomeFeatureEnum
import com.ufistudio.hotelmediabox.repository.data.HomeIcons
import com.ufistudio.hotelmediabox.utils.FileUtils
import kotlinx.android.synthetic.main.fragment_channel.*
import kotlinx.android.synthetic.main.view_bottom_fullscreen.*
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

    companion object {
        fun newInstance(): ChannelFragment = ChannelFragment()
        private val TAG = ChannelFragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = AppInjector.obtainViewModel(this)

//        //initChannel
//        mViewModel.initChannelsSuccess.observe(this, Observer { list -> list?.let { initChannelsSuccess(it) } })
//        mViewModel.initChannelsProgress.observe(this, Observer { isProgress ->
//            initChannelsProgress(isProgress ?: false)
//        })
//        mViewModel.initChannelsError.observe(this, Observer { throwable -> throwable?.let { initChannelsError(it) } })

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

//        mViewModel.initChannels()

        mChannelListAdapter.setItems(mViewModel.getTVHelper().getChannelList())
//        switchFocus(false)
    }

    override fun onResume() {
        super.onResume()
        mExoPlayerHelper.initPlayer(context, videoView)
        mViewModel.getTVHelper().initAVPlayer(TVHelper.SCREEN_TYPE.CHANNELPAGE)
        mViewModel.getTVHelper().getCurrentChannel()?.let { mChannelListAdapter.setCurrentTVChannel(it) }
        switchFocus(false)
//        mViewModel.getTVHelper().playCurrent()?.observeOn(AndroidSchedulers.mainThread())?.subscribe({
//            mViewModel.getTVHelper().getCurrentChannel()?.let { tvChannel ->
//                textChannelName?.text = tvChannel.chNum + ": " + tvChannel.chName + " (${tvChannel.chIp.frequency}mhz,${tvChannel.chIp.dvbParameter})"
//            }
//        }, {})
    }

    override fun onPause() {
        super.onPause()
        mViewModel.getTVHelper().closeAVPlayer()
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

        mDisposable = Observable.timer(400, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {}, { onError -> Log.e(TAG, "error:$onError") }, {

                    if (channelInfo.chType == "DVBT") {

                        mViewModel.getTVHelper().play(channelInfo).subscribe()

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
                var name =
                    channel.chNum + ": " + channel.chName + " (${channel.chIp.frequency}mhz,${channel.chIp.dvbParameter})"
                text_channel_info.text = name
                onChannelSelectListener(channel)
            }
        }
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.e(TAG, "event:${event?.characters ?: ""}, keycode:$keyCode")

        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (mGenreFocus) {
                    mChannelListAdapter.setGenreFilter(mGenreAdapter.selectDown().name)
                } else if (mListFocus) {
                    view_channel_list.scrollToPosition(mChannelListAdapter.getSelectPosition())
                    mChannelListAdapter.selectDownItem()?.let { channel ->
                        var name =
                            channel.chNum + ": " + channel.chName + " (${channel.chIp.frequency}mhz,${channel.chIp.dvbParameter})"
                        text_channel_info.text = name
                        onChannelSelectListener(channel) }

                }
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (mGenreFocus) {
                    mChannelListAdapter.setGenreFilter(mGenreAdapter.selectUp().name)
                } else if (mListFocus) {
                    view_channel_list.scrollToPosition(mChannelListAdapter.getSelectPosition())
                    mChannelListAdapter.selectUPItem()?.let { channel ->
                        var name =
                            channel.chNum + ": " + channel.chName + " (${channel.chIp.frequency}mhz,${channel.chIp.dvbParameter})"
                        text_channel_info.text = name
                        onChannelSelectListener(channel) }

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
                    startActivity(Intent(context, FullScreenActivity::class.java))
                }
            }

            KeyEvent.KEYCODE_CHANNEL_UP -> {
            }
            KeyEvent.KEYCODE_CHANNEL_DOWN -> {
            }
            KeyEvent.KEYCODE_BACK -> {
                if (!sideView.isShown) {
                    mGenreFocus = false
                    mListFocus = false
                    displaySideView(true)
//                    mGenreAdapter.selectLast(mLastGenreSelectIndex)
                    return true
                }
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
            sideView.visibility = View.VISIBLE
//            layout_back.visibility = View.GONE
//            view_line.visibility = View.VISIBLE
//            mGenreAdapter.selectLast(-1)
            sideView.setLastPosition(HomeFeatureEnum.LIVE_TV.ordinal)
        } else {
            sideView.visibility = View.GONE
//            layout_back.visibility = View.VISIBLE
//            view_line.visibility = View.GONE
        }
    }
}