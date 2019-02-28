package com.ufistudio.hotelmediabox.repository.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

abstract class BaseChannel {
    abstract var genre: String
    abstract var number: String
    abstract var name: String
    abstract var icon: String
}

@Parcelize
data class IPTVChannel(
    val ip: String,
    val port: String,
    override var genre: String,
    override var number: String,
    override var name: String,
    override var icon: String
) : BaseChannel(), Parcelable

@Parcelize
data class DTVChannel(
    val frequency: String,
    val bandwidth: String,
    override var genre: String,
    override var number: String,
    override var name: String,
    override var icon: String
) : BaseChannel(), Parcelable