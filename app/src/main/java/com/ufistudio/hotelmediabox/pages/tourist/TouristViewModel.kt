package com.ufistudio.hotelmediabox.pages.tourist

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.repository.Repository
import com.ufistudio.hotelmediabox.repository.data.NoteButton
import com.ufistudio.hotelmediabox.repository.data.TouristInfo
import com.ufistudio.hotelmediabox.repository.data.TouristLocation
import com.ufistudio.hotelmediabox.repository.viewModel.BaseViewModel
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers

class TouristViewModel(
    application: Application,
    private val compositeDisposable: CompositeDisposable,
    private val repository: Repository
) : BaseViewModel(application, compositeDisposable) {

    val initTouristSuccess = MutableLiveData<Pair<TouristInfo?, NoteButton?>>()
    val initTouristProgress = MutableLiveData<Boolean>()
    val initTouristError = MutableLiveData<Throwable>()

    val mGson = Gson()

    fun initTourist() {
        compositeDisposable.add(Single.fromCallable { getJsonObject() }
            .zipWith(Single.fromCallable { getNoteButton() })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { initTouristProgress.value = true }
            .doFinally { initTouristProgress.value = false }
            .subscribe({ initTouristSuccess.value = it }
                , { initTouristError.value = it })
        )
    }

    private fun getJsonObject(): TouristInfo? {
        return mGson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("tourist"), TouristInfo::class.java) ?: TouristInfo(
            arrayListOf(
                TouristLocation("Johor",R.drawable.touristinfo_johor, R.drawable.touristinfo_wholemap),
                TouristLocation("Kedah",R.drawable.touristinfo_kedah, R.drawable.touristinfo_wholemap),
                TouristLocation("Kelantan",R.drawable.touristinfo_kelantan, R.drawable.touristinfo_wholemap),
                TouristLocation("Malacca",R.drawable.touristinfo_malacca, R.drawable.touristinfo_wholemap),
                TouristLocation("NegeriSembilan",R.drawable.touristinfo_negerisembilan, R.drawable.touristinfo_wholemap),
                TouristLocation("Pahang",R.drawable.touristinfo_pahang, R.drawable.touristinfo_wholemap),
                TouristLocation("Penang",R.drawable.touristinfo_penang, R.drawable.touristinfo_wholemap),
                TouristLocation("Perak",R.drawable.touristinfo_perak, R.drawable.touristinfo_wholemap),
                TouristLocation("Perlis",R.drawable.touristinfo_perlis, R.drawable.touristinfo_wholemap),
                TouristLocation("Putrajaya",R.drawable.touristinfo_putrajaya, R.drawable.touristinfo_wholemap),
                TouristLocation("Sabah",R.drawable.touristinfo_sabah, R.drawable.touristinfo_wholemap),
                TouristLocation("Sarawak",R.drawable.touristinfo_sarawak, R.drawable.touristinfo_wholemap),
                TouristLocation("Selangor",R.drawable.touristinfo_selangor, R.drawable.touristinfo_wholemap),
                TouristLocation("Terengganu",R.drawable.touristinfo_terengganu, R.drawable.touristinfo_wholemap),
                TouristLocation("WilayahPersekutuan",R.drawable.touristinfo_wilayahpersekutuan, R.drawable.touristinfo_wholemap)
            )
        )
    }

    private fun getNoteButton(): NoteButton? {
        return mGson.fromJson(MiscUtils.getJsonLanguageAutoSwitch("bottom_note"), NoteButton::class.java)
            ?: NoteButton()
    }
}