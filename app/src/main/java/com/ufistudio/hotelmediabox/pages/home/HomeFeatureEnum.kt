package com.ufistudio.hotelmediabox.pages.home

import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Page

enum class HomeFeatureEnum(id: Int, page: Int, focusedIcon: Int, icon: Int) {
    LIVE_TV(0, Page.CHANNEL, R.drawable.ic_live_tv_1, R.drawable.ic_live_tv),
    VOD(1, Page.VOD, R.drawable.ic_vod_1, R.drawable.ic_vod),
    SMART_APP(2, -100, R.drawable.ic_smart_apps_1, R.drawable.ic_smart_apps),
    ROOM_SERVICE(3, Page.ROOM_SERVICE, R.drawable.ic_room_service_1, R.drawable.ic_room_service),
    FACILITIES(4, Page.HOTEL_FACILITIES, R.drawable.ic_facilities_1, R.drawable.ic_facilities),
    NEAR_BY(5, Page.NEARBY_ME, R.drawable.ic_nearby_me_1, R.drawable.ic_nearby_me),
    TOURIST_INFO(6, -100, R.drawable.ic_tourist_info_1, R.drawable.ic_tourist_info),
    FLIGHT_INFO(7, Page.FLIGHTS_INFO, R.drawable.ic_flight_info_1, R.drawable.ic_flight_info),
    WEATHER(8, Page.WEATHER, R.drawable.ic_weather_1, R.drawable.ic_weather),
    GUEST(9, -100, R.drawable.ic_guest_services_1, R.drawable.ic_guest_services),
    SETTING(10, Page.SETTING, R.drawable.ic_settings_1, R.drawable.ic_settings);


    companion object {
        fun findItemById(tag: Int): HomeFeatureEnum? {
            for (item in values()) {
                if (tag == item.id) {
                    return item
                }
            }
            return null
        }
    }

    var id: Int = id
    var page: Int = page
    var icon: Int = icon
    val focusedIcon: Int = focusedIcon
}