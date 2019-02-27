package com.ufistudio.hotelmediabox.pages.home

import com.ufistudio.hotelmediabox.R

enum class HomeFeatherEnum(title: Int, icon: Int, focusedIcon: Int) {
    LIVE_TV(R.string.home_icon_live_tv, R.drawable.ic_live_tv, R.drawable.ic_live_tv_1),
    VOD(R.string.home_icon_vod, R.drawable.ic_vod, R.drawable.ic_vod_1),
    SMART_APP(R.string.home_icon_smart_app, R.drawable.ic_smart_apps, R.drawable.ic_smart_apps_1),
    ROOM_SERVICE(R.string.home_icon_room_service, R.drawable.ic_room_service, R.drawable.ic_room_service_1),
    FACILITIES(R.string.home_icon_facilities, R.drawable.ic_facilities, R.drawable.ic_facilities_1),
    NEAR_BY(R.string.home_icon_near_by, R.drawable.ic_nearby_me, R.drawable.ic_nearby_me_1),
    TOURIST_INFO(R.string.home_icon_tourist, R.drawable.ic_tourist_info, R.drawable.ic_tourist_info_1),
    FLIGHT_INFO(R.string.home_icon_flight, R.drawable.ic_flight_info, R.drawable.ic_flight_info_1),
    WEATHER(R.string.home_icon_weather, R.drawable.ic_weather, R.drawable.ic_weather_1),
    GUEST(R.string.home_icon_guest, R.drawable.ic_guest_services, R.drawable.ic_guest_services_1),
    SETTING(R.string.home_icon_setting, R.drawable.ic_settings, R.drawable.ic_settings_1);


    var title: Int = title
    var icon: Int = icon
    val focusedIcon: Int = focusedIcon

}