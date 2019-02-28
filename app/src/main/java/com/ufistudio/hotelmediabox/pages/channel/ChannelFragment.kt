package com.ufistudio.hotelmediabox.pages.channel

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.repository.data.IPTVChannel
import kotlinx.android.synthetic.main.fragment_channel.*

class ChannelFragment : InteractionView<OnPageInteractionListener.Primary>() {

    private lateinit var mViewModel: ChannelViewModel

    private var mGenreAdapter: ChannelGenreAdapter = ChannelGenreAdapter { genreType, isFocus -> onGenreChangeListener(genreType, isFocus) }
    private var mChannelListAdapter: ChannelListAdapter = ChannelListAdapter { channelInfo, isFocus -> onChannelSelectListener(channelInfo, isFocus) }

    companion object {
        fun newInstance(): ChannelFragment = ChannelFragment()
        private val TAG = ChannelFragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel = AppInjector.obtainViewModel(this)

        //initChannel
        mViewModel.initChannelsSuccess.observe(this, Observer { list -> list?.let { initChannelsSuccess(it) } })
        mViewModel.initChannelsProgress.observe(this, Observer { isProgress ->
            initChannelsProgress(isProgress ?: false)
        })
        mViewModel.initChannelsError.observe(this, Observer { throwable -> throwable?.let { initChannelsError(it) } })
        //initGenre
        mViewModel.initGenreSuccess.observe(this, Observer { list -> list?.let { initGenreSuccess(it) } })
        mViewModel.initGenreProgress.observe(this, Observer { isProgress ->
            initGenreProgress(isProgress ?: false)
        })
        mViewModel.initGenreError.observe(this, Observer { throwable -> throwable?.let { initGenreError(it) } })

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_channel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        view_genre_list.layoutManager = LinearLayoutManager(context)
        view_genre_list.adapter = mGenreAdapter

        view_channel_list.layoutManager = LinearLayoutManager(context)
        view_channel_list.adapter = mChannelListAdapter

        mViewModel.initGenre()
        mViewModel.initChannels()
    }

    private fun onGenreChangeListener(genreType: String, isFocus: Boolean) {
        Log.e(TAG, "[onGenreChangeListener] genreType:$genreType, isFocus:$isFocus")
        if (isFocus) {
            mChannelListAdapter.setGenreFilter(genreType)
        }
    }

    private fun onChannelSelectListener(channelInfo: IPTVChannel, isFocus: Boolean) {
        Log.e(TAG, "[onChannelSelectListener] channelInfo:$channelInfo, isFocus:$isFocus")
        //TODO call player 播放
    }

    private fun initChannelsSuccess(list: ArrayList<IPTVChannel>) {
        mChannelListAdapter.setItems(list)
    }

    private fun initChannelsProgress(isProgress: Boolean) {
        Log.e(TAG, "initChannelsProgress call. isProgress:$isProgress")
    }

    private fun initChannelsError(throwable: Throwable) {
        Log.e(TAG, "initChannelsError call. ${throwable.message}")
    }

    private fun initGenreSuccess(list: ArrayList<String>) {
        mGenreAdapter.setItems(list)
    }

    private fun initGenreProgress(isProgress: Boolean) {
        Log.e(TAG, "initGenreProgress call. isProgress:$isProgress")
    }

    private fun initGenreError(throwable: Throwable) {
        Log.e(TAG, "initGenreError call. ${throwable.message}")
    }

}