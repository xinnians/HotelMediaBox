package com.ufistudio.hotelmediabox.pages.channel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.IPTVChannel
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ChannelViewModel(
        application: Application,
        private val compositeDisposable: CompositeDisposable,
        private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {

    val initChannelsSuccess = MutableLiveData<ArrayList<IPTVChannel>>()
    val initChannelsProgress = MutableLiveData<Boolean>()
    val initChannelsError = MutableLiveData<Throwable>()

    fun initChannels() {

        var iptv1 = IPTVChannel("1.1.1.1", "1234", "Sport", "10", "緯來體育", "null")
        var iptv2 = IPTVChannel("1.1.1.1", "1234", "Sport", "11", "ESport", "null")
        var iptv3 = IPTVChannel("1.1.1.1", "1234", "Sport", "12", "東森體育", "null")
        var iptv4 = IPTVChannel("1.1.1.1", "1234", "News", "13", "中視", "null")
        var iptv5 = IPTVChannel("1.1.1.1", "1234", "News", "14", "台視", "null")
        var iptv6 = IPTVChannel("1.1.1.1", "1234", "Movie", "15", "drama1", "null")
        var iptv7 = IPTVChannel("1.1.1.1", "1234", "Movie", "16", "drama2", "null")
        var iptv8 = IPTVChannel("1.1.1.1", "1234", "Movie", "17", "drama3", "null")
        var iptv9 = IPTVChannel("1.1.1.1", "1234", "MTV", "18", "mtv1", "null")
        var iptv10 = IPTVChannel("1.1.1.1", "1234", "MTV", "19", "mtv2", "null")

        var channelList: ArrayList<IPTVChannel> =
                arrayListOf(iptv1, iptv2, iptv3, iptv4, iptv5, iptv6, iptv7, iptv8, iptv9, iptv10)

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