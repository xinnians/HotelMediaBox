package com.ufistudio.hotelmediabox.pages.weather

import android.text.TextUtils
import android.util.Log
import com.ufistudio.hotelmediabox.R

enum class WeatherIconEnum(name: String, icon: Int) {
    CLOUDY("Cloudy", R.drawable.ic_weather_cloudy),
    PARTLY_CLOUDY("Partly Cloudy", R.drawable.ic_weather_partlycloudy),
    RAINING("Rain", R.drawable.ic_weather_raining),
    SHOWER("Showers", R.drawable.ic_weather_shower),
    SUNNY("Sunny", R.drawable.ic_weather_sunny),
    THUNDERSTORM("Thunderstorms", R.drawable.ic_weather_thunderstorm),
    WINDY("Windy", R.drawable.ic_weather_windy),
    NONE("", -1);

    var mName = name
    var mIcon = icon

    companion object {
        fun getItemByName(name: String?): WeatherIconEnum {
            for (item in values()) {
                if (item.mName.hashCode() == name.hashCode()) {
                    return item
                }
            }
            return NONE
        }
    }
}