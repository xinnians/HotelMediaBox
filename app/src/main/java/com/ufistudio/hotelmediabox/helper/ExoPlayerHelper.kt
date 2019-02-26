package com.ufistudio.hotelmediabox.helper

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.upstream.UdpDataSource
import com.ufistudio.hotelmediabox.R

open class ExoPlayerHelper {
    private lateinit var mPlayer: SimpleExoPlayer
    private var mContext: Context? = null

    fun initPlayer(context: Context?, videoView: PlayerView) {
        mContext = context
        val trackSelector = DefaultTrackSelector()
        mPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        videoView.player = mPlayer
        videoView
    }

    fun setUdpSource(udpUrl: String, playWhenReady: Boolean = true) {
        val udpDataSource = UdpDataSource()
        val dataSpec = DataSpec(Uri.parse(udpUrl))
        try {
            udpDataSource.open(dataSpec)

            val factory = com.google.android.exoplayer2.upstream.DataSource.Factory { udpDataSource }
            val videoSource = ExtractorMediaSource.Factory(factory).createMediaSource(udpDataSource.uri)
            mPlayer.prepare(videoSource)

        } catch (e: UdpDataSource.UdpDataSourceException) {
            e.printStackTrace()
        }

        mPlayer.playWhenReady = playWhenReady
    }

    fun setMp4Source(mp4Uri: Int, playWhenReady: Boolean = true) {
        val dtaSource = RawResourceDataSource(mContext)
        val dataSpec = DataSpec(RawResourceDataSource.buildRawResourceUri(mp4Uri))
        try {
            dtaSource.open(dataSpec)

            val factory = com.google.android.exoplayer2.upstream.DataSource.Factory { dtaSource }
            val videoSource = ExtractorMediaSource.Factory(factory).createMediaSource(dtaSource.uri)
            mPlayer.prepare(videoSource)

        } catch (e: UdpDataSource.UdpDataSourceException) {
            e.printStackTrace()
        }

        mPlayer.playWhenReady = playWhenReady
    }

    fun play() {

    }

    fun release() {
        mPlayer.release()
    }
}