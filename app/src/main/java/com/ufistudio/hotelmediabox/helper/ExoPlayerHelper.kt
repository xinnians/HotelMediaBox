package com.ufistudio.hotelmediabox.helper

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.offline.FilteringManifestParser
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.rtsp.RtspDefaultClient
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.exoplayer2.source.rtsp.core.Client
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.ufistudio.hotelmediabox.MyApplication
import com.ufistudio.hotelmediabox.R
import java.io.File
import java.lang.Exception

open class ExoPlayerHelper {
    private var mPlayer: SimpleExoPlayer? = null
    private lateinit var mVideoView: PlayerView
    private var mContext: Context? = null
    private var mVideoFrameParams: ConstraintLayout.LayoutParams? = null
    private var mVideoParams: ConstraintLayout.LayoutParams? = null
    private var mIsFullscreen: Boolean = false

    fun initPlayer(context: Context?, videoView: PlayerView) {
        mContext = context
        val trackSelector = DefaultTrackSelector()
        mPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        mVideoView = videoView
        mVideoView.player = mPlayer
    }

    /**
     * Set Udp source.
     * @udpUrl : IP of udp
     * @playWhenReady: If you want play when ready , default:true
     */
    fun setUdpSource(udpUrl: String, playWhenReady: Boolean = true) {
        Log.d("setUdpSource", "setUdpSource udp url = $udpUrl")
        val udpDataSource = UdpDataSource()
        val dataSpec = DataSpec(Uri.parse(udpUrl))
        try {
            udpDataSource.open(dataSpec)

            val factory = com.google.android.exoplayer2.upstream.DataSource.Factory { udpDataSource }
            val videoSource = ExtractorMediaSource.Factory(factory).createMediaSource(udpDataSource.uri)
            mPlayer?.prepare(videoSource)

        }catch (e: Exception){
            e.printStackTrace()
        }

        mPlayer?.playWhenReady = playWhenReady
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
            mPlayer?.prepare(videoSource)

        } catch (e: RawResourceDataSource.RawResourceDataSourceException) {
            e.printStackTrace()
        }

        mPlayer?.playWhenReady = playWhenReady
    }

    fun setSource(source: Any, playWhenReady: Boolean = true){
        var datauri: Uri? = when (source) {
            is String -> Uri.parse(source)
            is Uri -> source
            is Int -> RawResourceDataSource.buildRawResourceUri(source)
            else -> null
        }

        var type = Util.inferContentType(datauri)

        Log.e("EXOPLAYER","source : $source, type : $type")

        var mediaSource: MediaSource? = null

        when(type){
//            等後續有需要再加，需要+cache
//            C.TYPE_DASH ->{
//                mediaSource = DashMediaSource.Factory((mContext as MyApplication).buildDataSourceFactory())
//                    .setManifestParser(FilteringManifestParser<>(DashManifestParser(),))
//            }
//            C.TYPE_SS ->{
//
//            }
//            C.TYPE_HLS ->{
//
//            }
            C.TYPE_OTHER ->{
                if (Util.isRtspUri(datauri)) {
                    mediaSource = RtspMediaSource.Factory(RtspDefaultClient.factory()
                        .setFlags(Client.FLAG_ENABLE_RTCP_SUPPORT)
                        .setNatMethod(Client.RTSP_NAT_DUMMY))
                        .createMediaSource(datauri)
                }else if(source is String && source.contains("udp")){
                    val udpDataSource = UdpDataSource()
                    val dataSpec = DataSpec(datauri)
                    udpDataSource.open(dataSpec)
                    val factory = com.google.android.exoplayer2.upstream.DataSource.Factory { udpDataSource }
                    mediaSource = ExtractorMediaSource.Factory(factory).createMediaSource(udpDataSource.uri)
                }else{
                    mediaSource = ExtractorMediaSource.Factory((mContext as MyApplication).buildDataSourceFactory()).createMediaSource(datauri)
                }
            }
            else ->{

            }
        }

//        var t: DefaultHttpDataSourceFactory = DefaultHttpDataSourceFactory(Util.getUserAgent(mContext,"ExoPlayerDemo"))
//        var mediaDataSourceFactory = DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext,"ExoPlayerDemo"))
//        var mediaSource = ExtractorMediaSource.Factory(t).createMediaSource(Uri.parse(source))

//        var mediaSource = RtspMediaSource.Factory(RtspDefaultClient.factory()
//            .setFlags(Client.FLAG_ENABLE_RTCP_SUPPORT)
//            .setNatMethod(Client.RTSP_NAT_DUMMY))
//            .createMediaSource(Uri.parse(source))
//        var mediaSource = HlsMediaSource(Uri.parse(source))

        mediaSource?.let {
            mPlayer?.prepare(it)
            mPlayer?.playWhenReady = playWhenReady
        }
    }

    /**
     * Set File source.
     * @fileUri : Uri of file
     * @playWhenReady: If you want play when ready , default:true
     */
    fun setFileSource(fileUri: Uri, playWhenReady: Boolean = true) {
        val dtaSource = FileDataSource()
        val dataSpec = DataSpec(fileUri)
        try {
            dtaSource.open(dataSpec)

            val factory = com.google.android.exoplayer2.upstream.DataSource.Factory { dtaSource }
            val videoSource = ExtractorMediaSource.Factory(factory).createMediaSource(dtaSource.uri)
            mPlayer?.prepare(videoSource)

        } catch (e: FileDataSource.FileDataSourceException) {
            e.printStackTrace()
        }

        mPlayer?.playWhenReady = playWhenReady
    }

    /**
     * 改變頻道，要做的參數設置
     */
    fun changeFullScreenInfo() {
        //TODO("應該還要加上傳入的Object")
        (mVideoView.findViewById(R.id.text_bottom_title) as TextView).text = "TV 2"
        Glide.with(mContext!!)
                .load(ColorDrawable(ContextCompat.getColor(mContext!!, android.R.color.holo_blue_dark)))
                .skipMemoryCache(true)
                .apply(RequestOptions.circleCropTransform())
                .into((mVideoView.findViewById(R.id.image_channel_center) as ImageView))
        var requestOptions = RequestOptions()
        requestOptions = requestOptions.transform(CenterCrop(), RoundedCorners(16))
        Glide.with(mContext!!)
                .load(ColorDrawable(ContextCompat.getColor(mContext!!, android.R.color.holo_blue_dark)))
                .skipMemoryCache(true)
                .apply(requestOptions)
                .into((mVideoView.findViewById(R.id.image_bottom_channel) as ImageView))
    }

    /**
     * stop video
     */
    fun stop() {
        mPlayer?.playWhenReady = false
    }

    /**
     * Start video
     */
    fun play() {
        mPlayer?.playWhenReady = true
    }

    /**
     * When you stop the page.
     * You have to call this Method.
     */
    fun release() {
        mPlayer?.release()
        mIsFullscreen = false
    }

    /**
     * Set video repeat play
     */
    fun repeatMode() {
        mPlayer?.repeatMode = Player.REPEAT_MODE_ALL
    }

    /**
     * Switch the fullscreen and normal screen
     */
    fun fullScreen() {
        val parent: ConstraintLayout = mVideoView.parent as ConstraintLayout
        if (!isFullscreen()) {

            val parentParams = parent.layoutParams as ConstraintLayout.LayoutParams

            mVideoFrameParams = ConstraintLayout.LayoutParams(parentParams)
            mVideoParams = ConstraintLayout.LayoutParams(mVideoView.layoutParams as ConstraintLayout.LayoutParams)

            parentParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT
            parentParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT
            parentParams.marginStart = 0
            parentParams.topMargin = 0
            parentParams.bottomMargin = 0
            parentParams.marginEnd = 0
            parent.layoutParams = parentParams
            mVideoView.layoutParams = parentParams
            mIsFullscreen = true
        } else {
            parent.layoutParams = mVideoFrameParams
            mVideoView.layoutParams = mVideoParams
            mIsFullscreen = false
        }
    }

    /**
     * Check is fullscreen or not
     */
    fun isFullscreen(): Boolean {
        return mIsFullscreen
    }
}