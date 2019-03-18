package com.ufistudio.hotelmediabox.pages.home

import android.text.TextUtils
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Page

enum class HomeFeatureEnum(tag: String, title: Int, page: Int, focusedIcon: Int, icon: Int) {
    LIVE_TV("live_tv_channel", R.string.home_icon_live_tv, Page.CHANNEL, R.drawable.ic_live_tv_1, R.drawable.ic_live_tv),
    VOD("vod_movies", R.string.home_icon_vod, Page.CHANNEL, R.drawable.ic_vod_1, R.drawable.ic_vod),
    SMART_APP("smart_apps", R.string.home_icon_smart_app, Page.SMART_APPS, R.drawable.ic_smart_apps_1, R.drawable.ic_smart_apps),
    ROOM_SERVICE("room_service", R.string.home_icon_room_service, Page.ROOM_SERVICE, R.drawable.ic_room_service_1, R.drawable.ic_room_service),
    FACILITIES("hotel_facilities", R.string.home_icon_facilities, Page.HOTEL_FACILITIES, R.drawable.ic_facilities_1, R.drawable.ic_facilities),
    NEAR_BY("nearby_me", R.string.home_icon_near_by, Page.CHANNEL, R.drawable.ic_nearby_me_1, R.drawable.ic_nearby_me),
    TOURIST_INFO("tourist_info", R.string.home_icon_tourist, Page.CHANNEL, R.drawable.ic_tourist_info_1, R.drawable.ic_tourist_info),
    FLIGHT_INFO("flight_info", R.string.home_icon_flight, Page.CHANNEL, R.drawable.ic_flight_info_1, R.drawable.ic_flight_info),
    WEATHER("weather_forecast", R.string.home_icon_weather, Page.CHANNEL, R.drawable.ic_weather_1, R.drawable.ic_weather),
    GUEST("guest", R.string.home_icon_guest, Page.CHANNEL, R.drawable.ic_guest_services_1, R.drawable.ic_guest_services),
    SETTING("settings", R.string.home_icon_setting, Page.CHANNEL, R.drawable.ic_settings_1, R.drawable.ic_settings);


    companion object {
        fun findItemByTag(tag: String): HomeFeatureEnum? {
            for (item in values()) {
                if (TextUtils.equals(tag, item.tag)) {
                    return item
                }
            }
            return null
        }
    }

    var tag: String = tag
    var title: Int = title
    var page: Int = page
    var icon: Int = icon
    val focusedIcon: Int = focusedIcon
}