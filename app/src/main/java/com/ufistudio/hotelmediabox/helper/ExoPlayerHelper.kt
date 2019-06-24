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
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS
import com.google.android.exoplayer2.offline.FilteringManifestParser
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.rtsp.RtspDefaultClient
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.exoplayer2.source.rtsp.core.Client
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.ufistudio.hotelmediabox.MyApplication
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.utils.FileUtils
import java.io.File
import java.lang.Exception

open class ExoPlayerHelper {
    private var mPlayer: SimpleExoPlayer? = null
    private lateinit var mVideoView: PlayerView
    private var mContext: Context? = null
    private var mVideoFrameParams: ConstraintLayout.LayoutParams? = null
    private var mVideoParams: ConstraintLayout.LayoutParams? = null
    private var mIsFullscreen: Boolean = false
    private val TAG: String = ExoPlayerHelper::class.java.simpleName
    private var mMediaSource: MediaSource? = null
    private var mIsUDP: Boolean = false

    fun initPlayer(context: Context?, videoView: PlayerView) {
        mContext = context
        val trackSelector = DefaultTrackSelector()
        mPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        mVideoView = videoView
        mVideoView.player = mPlayer

        mPlayer?.addListener(object:Player.EventListener{
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
                super.onPlaybackParametersChanged(playbackParameters)

//                Log.e(TAG,"[onPlaybackParametersChanged] ${playbackParameters.toString()}")
            }

            override fun onSeekProcessed() {
                super.onSeekProcessed()
//                Log.e(TAG,"[onSeekProcessed] call.")
            }

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
                super.onTracksChanged(trackGroups, trackSelections)
//                Log.e(TAG,"[onTracksChanged] call. ")
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                super.onPlayerError(error)
//                Log.e(TAG,"[onPlayerError] call. ")
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                super.onLoadingChanged(isLoading)
//                Log.e(TAG,"[onLoadingChanged] call. isLoading : $isLoading")
            }

            override fun onPositionDiscontinuity(reason: Int) {
                super.onPositionDiscontinuity(reason)
//                Log.e(TAG,"[onPositionDiscontinuity] call. reason : $reason")
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                super.onRepeatModeChanged(repeatMode)
//                Log.e(TAG,"[onRepeatModeChanged] call. repeatMode : $repeatMode")
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                super.onShuffleModeEnabledChanged(shuffleModeEnabled)
//                Log.e(TAG,"[onShuffleModeEnabledChanged] call. shuffleModeEnabled : $shuffleModeEnabled")
            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
                super.onTimelineChanged(timeline, manifest, reason)
//                Log.e(TAG,"[onTimelineChanged] call.")
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                Log.e(TAG,"[onPlayerStateChanged] call. playWhenReady : $playWhenReady playbackState : $playbackState")
                if(playbackState == 2 && mIsUDP){
                    mMediaSource?.let {
                        mPlayer?.prepare(it)
                        mPlayer?.playWhenReady = playWhenReady
                    }
                }
            }
        })
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

        mMediaSource = null
        mIsUDP = false

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
                    mMediaSource = RtspMediaSource.Factory(RtspDefaultClient.factory()
                        .setFlags(Client.FLAG_ENABLE_RTCP_SUPPORT)
                        .setNatMethod(Client.RTSP_NAT_DUMMY))
                        .createMediaSource(datauri)
                }else if(source is String && source.contains("udp")){
                    mIsUDP = true
                    val udpDataSource = UdpDataSource(2000,20000)
                    val dataSpec = DataSpec(datauri)
                    udpDataSource.open(dataSpec)
                    val myDataSourceFactory = DefaultDataSourceFactory(mContext, null) { -> UdpDataSource( 2000, 20000) }
                    mMediaSource = ExtractorMediaSource.Factory(myDataSourceFactory).createMediaSource(udpDataSource.uri)

                }else{
                    mMediaSource = ExtractorMediaSource.Factory((mContext as MyApplication).buildDataSourceFactory())
//                        .setExtractorsFactory(DefaultExtractorsFactory().setTsExtractorFlags(FLAG_DETECT_ACCESS_UNITS))
                        .createMediaSource(datauri)
                }
            }
            else ->{

            }
        }

        mMediaSource?.let {
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
        mIsUDP = false
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