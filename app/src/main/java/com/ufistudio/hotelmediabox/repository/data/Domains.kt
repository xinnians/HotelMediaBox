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

/*
Hotel Facilities
 */
@Parcelize
data class HotelFacilitiesContent(
        var title: String,
        var file_type: String,
        var content: String,
        var file_name: String
) : Parcelable

@Parcelize
data class HotelFacilitiesCategories(
        val title: String,
        val content_type: Int,
        val description: String,
        val total: Int,
        val contents: ArrayList<HotelFacilitiesContent>
) : Parcelable


@Parcelize
data class HotelFacilities(
        val categories: ArrayList<HotelFacilitiesCategories>
) : Parcelable

/*
Room Service
 */
@Parcelize
data class RoomServices(
        val categories: ArrayList<RoomServiceCategories>
) : Parcelable

@Parcelize
data class RoomServiceCategories(
        val title: String,
        val content_type: Int,
        val description: String,
        val total: Int,
        val contents: ArrayList<RoomServiceContent>
) : Parcelable

@Parcelize
data class RoomServiceContent(
        var title: String,
        var price: String,
        var type: String,
        var file_type: String,
        var content: String,
        var image: String
) : Parcelable