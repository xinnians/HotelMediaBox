package com.ufistudio.hotelmediabox.helper

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.upstream.UdpDataSource
import com.ufistudio.hotelmediabox.R

open class ExoPlayerHelper {
    private lateinit var mPlayer: SimpleExoPlayer
    private lateinit var mVideoView: PlayerView
    private var mContext: Context? = null

    fun initPlayer(context: Context?, videoView: PlayerView) {
        mContext = context
        val trackSelector = DefaultTrackSelector()
        mPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        mVideoView = videoView
        mVideoView.player = mPlayer

        Log.d("neo", "videoView.isControllerVisible = $videoView.isControllerVisible")
    }

    /**
     * Set Udp source.
     * @udpUrl : IP of udp
     * @playWhenReady: If you want play when ready , default:true
     */
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

    /**
     * Set Udp source.
     * @mp4Uri : Uri of mp4
     * @playWhenReady: If you want play when ready , default:true
     */
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

    /**
     * 改變頻道，要做的參數設置
     */
    fun changeFullScreenInfo() {
        //TODO("應該還要加上傳入的Object")
        mVideoView.findViewById<TextView>(R.id.text_bottom_title).text = "TV 2"
        Glide.with(mContext!!)
            .load(ColorDrawable(ContextCompat.getColor(mContext!!, android.R.color.holo_blue_dark)))
            .apply(RequestOptions.circleCropTransform())
            .into(mVideoView.findViewById<ImageView>(R.id.image_channel_center))
        var requestOptions = RequestOptions()
        requestOptions = requestOptions.transform(CenterCrop(), RoundedCorners(16))
        Glide.with(mContext!!)
            .load(ColorDrawable(ContextCompat.getColor(mContext!!, android.R.color.holo_blue_dark)))
            .apply(requestOptions)
            .into(mVideoView.findViewById<ImageView>(R.id.image_bottom_channel))
    }

    /**
     * stop video
     */
    fun stop() {
        mPlayer.playWhenReady = false
    }

    /**
     * Start video
     */
    fun play() {
        mPlayer.playWhenReady = true
    }

    fun release() {
        mPlayer.release()
    }
}