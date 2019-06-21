package com.ufistudio.hotelmediabox.pages.weather

import android.text.TextUtils
import android.util.Log
import com.ufistudio.hotelmediabox.R

enum class WeatherIconEnum(name: String, icon: Int) {
//    CLOUDY("Cloudy", R.drawable.ic_weather_cloudy),
//    PARTLY_CLOUDY("Partly Cloudy", R.drawable.ic_weather_partlycloudy),
//    RAINING("Rain", R.drawable.ic_weather_raining),
//    SHOWER("Showers", R.drawable.ic_weather_shower),
//    SUNNY("Sunny", R.drawable.ic_weather_sunny),
//    THUNDERSTORM("Thunderstorms", R.drawable.ic_weather_thunderstorm),
//    WINDY("Windy", R.drawable.ic_weather_windy),
//    NONE("", -1),

    TORNADO("Tornado", R.drawable.ic_weather_00_tornado),
    TROPICAL_STORM("Tropical Storm", R.drawable.ic_weather_01_tropicalstorm),
    HURRICANE("Hurricane", R.drawable.ic_weather_02_hurricane),
    SEVERE_THUNDERSTORMS("Severe Thunderstorms", R.drawable.ic_weather_03_severethunderstorms),
    THUNDERSTORMS("Thunderstorms", R.drawable.ic_weather_04_thunderstorms),
    MIXED_RAIN_AND_SNOW("Mixed Rain And Snow", R.drawable.ic_weather_05_mixedrain_snow),
    MIXED_RAIN_AND_SLEET("Mixed Rain And Sleet", R.drawable.ic_weather_06_mixedrain_sleet),
    MIXED_SNOW_AND_SLEET("Mixed Snow And Sleet", R.drawable.ic_weather_07_mixedsnow_sleet),
    FREEZING_DRIZZLE("Freezing Drizzle", R.drawable.ic_weather_08_freezingdrizzle),
    DRIZZLE("Drizzle", R.drawable.ic_weather_09_drizzle),
    DREEZING_RAIN("Dreezing Rain", R.drawable.ic_weather_10_freezingrain),
    SHOWERS("Showers", R.drawable.ic_weather_11_showers),
    RAIN("Rain", R.drawable.ic_weather_12_rain),
    SNOW_FLURRIES("Snow Flurries", R.drawable.ic_weather_13_snowfurries),
    LIGHT_SNOW_SHOWERS("Light Snow Showers", R.drawable.ic_weather_14_lightsnowshowers),
    BLOWING_SNOW("Blowing Snow", R.drawable.ic_weather_15_blowingsnow),
    SNOW("Snow", R.drawable.ic_weather_16_snow),
    HAIL("Hail", R.drawable.ic_weather_17_hail),
    SLEET("Sleet", R.drawable.ic_weather_18_sleet),
    DUST("Dust", R.drawable.ic_weather_19_dust),
    FOGGY("Foggy", R.drawable.ic_weather_20_foggy),
    HAZE("Haze", R.drawable.ic_weather_21_haze),
    SMOKY("Smoky", R.drawable.ic_weather_22_smoky),
    BLUSTERY("Blustery", R.drawable.ic_weather_23_blustery),
    WINDY("Windy", R.drawable.ic_weather_24_windy),
    COLD("Cold", R.drawable.ic_weather_25_cold),
    CLOUDY("Cloudy", R.drawable.ic_weather_26_cloudy),
    MOSTLY_CLOUDY_NIGHT("Mostly Cloudy (Night)", R.drawable.ic_weather_27_mostlycloudynight),
    MOSTLY_CLOUDY_DAY("Mostly Cloudy (Day)", R.drawable.ic_weather_28_mostlycloudyday),
    PARTLY_CLOUDY_NIGHT("Partly Cloudy (Night)", R.drawable.ic_weather_29_partlycloudynight),
    PARTLY_CLOUDY_DAY("Partly Cloudy (Day)", R.drawable.ic_weather_30_partlycloudyday),
    CLEAR_NIGHT("Clear (Night)", R.drawable.ic_weather_31_clearnight),
    SUNNY("Sunny", R.drawable.ic_weather_32_sunny),
    FAIR_NIGHT("Fair (Night)", R.drawable.ic_weather_33_fairnite),
    FAIR_DAY("Fair (Day)", R.drawable.ic_weather_34_fairday),
    MIXED_RAIN_AND_HAIL("Mixed Rain And Hail", R.drawable.ic_weather_35_mixedrain_hail),
    HOT("Hot", R.drawable.ic_weather_36_hot),
    ISOLATED_THUNDERSTORMS("Isolated Thunderstorms", R.drawable.ic_weather_37_isolatedthunderstorms),
    SCATTERED_THUNDERSTORMS("Scattered Thunderstorms", R.drawable.ic_weather_38_scatteredthunderstorms),
    SCATTERED_SHOWERS_DAY("Scattered Showers (Day)", R.drawable.ic_weather_39_scatteredshowersday),
    HEAVY_RAIN("Heavy Rain", R.drawable.ic_weather_40_heavyrain),
    SCATTERED_SNOW_SHOWERS_DAY("Scattered Snow Showers (Day)", R.drawable.ic_weather_41_scatteredsnowshowersday),
    HEAVY_SNOW("Heavy Snow", R.drawable.ic_weather_42_heavysnow),
    BLIZZARD("Blizzard", R.drawable.ic_weather_43_blizzard),
    NOT_AVAILABLE("Not Available", R.drawable.ic_weather_44_notavailable),
    SCATTERED_SHOWERS_NIGHT("Scattered Showers (Night)", R.drawable.ic_weather_45_scatteredshowersnight),
    SCATTERED_SNOW_SHOWERS_NIGHT("Scattered Snow Showers (Night)", R.drawable.ic_weather_46_scatteredsnowshowersnight),
    SCATTERED_THUNDERSHOWERS("Scattered Thundershowers", R.drawable.ic_weather_47_scatteredthundershowers),
    NONE("", -1);

    var mName = name
    var mIcon = icon

    companion object {
        fun getItemByName(name: String?): WeatherIconEnum {
            for (item in values()) {
                if (item.mName.hashCode() == name.hashCode()) {
                    Log.e("weather","name : $name, weatherItem:${item.mName}")
                    return item
                }
            }
            return NONE
        }
    }
}