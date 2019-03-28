package com.ufistudio.hotelmediabox.pages.channel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.ConnectDetail
import com.ufistudio.hotelmediabox.repository.data.TVChannel
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ChannelViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {

    val initChannelsSuccess = MutableLiveData<ArrayList<TVChannel>>()
    val initChannelsProgress = MutableLiveData<Boolean>()
    val initChannelsError = MutableLiveData<Throwable>()

    fun initChannels() {

        var iptv1 = TVChannel("1", "頻道1", "DVBT", ConnectDetail("","",581000,6000,"fa1 fa2 4 1"))
        var iptv2 = TVChannel("2", "頻道2", "DVBT", ConnectDetail("","",581000,6000,"fab fac 4 1"))
        var iptv3 = TVChannel("3", "頻道3", "DVBT", ConnectDetail("","",581000,6000,"fb5 fb6 0 3"))
        var iptv4 = TVChannel("4", "頻道4", "DVBT", ConnectDetail("","",581000,6000,"fc0 fbf 0 3"))
        var iptv5 = TVChannel("5", "頻道5", "DVBT", ConnectDetail("","",533000,6000,"3e9 3ea 4 1"))
        var iptv6 = TVChannel("6", "頻道6", "DVBT", ConnectDetail("","",533000,6000,"3f3 3f4 4 1"))
        var iptv7 = TVChannel("7", "頻道7", "DVBT", ConnectDetail("","",533000,6000,"3fd 3fe 0 3"))
        var iptv8 = TVChannel("8", "頻道8", "DVBT", ConnectDetail("","",533000,6000,"407 408 0 3"))
//        var iptv9 = TVChannel("9", "頻道9", "MTV", ConnectDetail("","",581000,6000,"fa1 fa2 4 1"))
//        var iptv10 = TVChannel("10", "頻道10", "MTV", ConnectDetail("","",581000,6000,"fa1 fa2 4 1"))

        var channelList: ArrayList<TVChannel> =
                arrayListOf(iptv1,iptv5, iptv2, iptv6,iptv3,iptv7, iptv4,    iptv8)

        compositeDisposable.add(Single.just(channelList)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { initChannelsProgress.value = true }
                .doFinally { initChannelsProgress.value = false }
                .subscribe({ initChannelsSuccess.value = it }
                        , { initChannelsError.value = it })
        )

    }

    val initGenreSuccess = MutableLiveData<ArrayList<String>>()
    val initGenreProgress = MutableLiveData<Boolean>()
    val initGenreError = MutableLiveData<Throwable>()

    fun initGenre() {
        val genreList: ArrayList<String> =
                arrayListOf("All", "Movie", "MTV", "News", "Sport")

        compositeDisposable.add(Single.just(genreList)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { initGenreProgress.value = true }
                .doFinally { initGenreProgress.value = false }
                .subscribe({ initGenreSuccess.value = it }
                        , { initGenreError.value = it })
        )
    }

}