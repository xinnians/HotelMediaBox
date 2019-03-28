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
Home
 */
@Parcelize
data class Home(
        val home: HomeContent
) : Parcelable

@Parcelize
data class HomeContent(
        val stage_type: HomeStageType,
        val icons: ArrayList<HomeIcons>,
        val promo_banner: ArrayList<HomePromoBanner>
) : Parcelable

@Parcelize
data class HomeStageType(
        val type: Int,
        val description: String
) : Parcelable

@Parcelize
data class HomeIcons(
        val name: String,
        val enable: Int
) : Parcelable

@Parcelize
data class HomePromoBanner(
        val image: String
) : Parcelable

@Parcelize
data class HomeVodMovie(
        val server_ip: String,
        val port: Int
) : Parcelable

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
        val bottomNote: String,
        val contents: ArrayList<RoomServiceContent>
) : Parcelable

@Parcelize
data class RoomServiceContent(
        var title: String,
        var price: String,
        var type: String,
        var file_type: String,
        var file_name: String,
        var content: String,
        var image: String
) : Parcelable

/*
    Welcome
 */
@Parcelize
data class Welcome(
        var welcome: WelcomeContent
) : Parcelable

@Parcelize
data class WelcomeContent(
        var room: String,
        var description: String,
        var title: String,
        var name: String,
        var time: String,
        var titleImage: String,
        var background: String,
        var music: String,
        var entryButton: String
) : Parcelable

/*
    Config
 */
@Parcelize
data class Config(
        var config: ConfigContent
) : Parcelable

@Parcelize
data class ConfigContent(
        var language: String,
        var upgradeUrl: String
) : Parcelable