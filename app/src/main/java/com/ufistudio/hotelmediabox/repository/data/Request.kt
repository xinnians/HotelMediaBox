package com.ufistudio.hotelmediabox.repository.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BroadcastRequest(
        val mac: String,
        val ip: String,
        val status: String,
        val room: String = "",
        val file: String = ""
) : Parcelable