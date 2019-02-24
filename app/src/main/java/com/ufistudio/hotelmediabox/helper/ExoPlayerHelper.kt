package com.ufistudio.hotelmediabox.helper

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.UdpDataSource

open class ExoPlayerHelper {
    private lateinit var mPlayer: SimpleExoPlayer

    fun initPlayer(context: Context?, videoView: PlayerView) {
        val trackSelector = DefaultTrackSelector()
        mPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        videoView.player = mPlayer
    }

    fun setUdpSource(udpUrl:String,playWhenReady:Boolean =true) {
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

    fun play() {

    }

    fun release() {
        mPlayer.release()
    }
}