package com.ufistudio.hotelmediabox.constants

import android.os.Bundle
import android.support.v4.app.Fragment
import com.ufistudio.hotelmediabox.pages.*
import com.ufistudio.hotelmediabox.pages.home.HomeFragment
import com.ufistudio.hotelmediabox.pages.channel.ChannelFragment
import com.ufistudio.hotelmediabox.pages.facilies.HotelFacilitiesFragment
import com.ufistudio.hotelmediabox.pages.facilies.template.FacilitiesContentFragment
import com.ufistudio.hotelmediabox.pages.nearby.NearbyMeFragment
import com.ufistudio.hotelmediabox.pages.roomService.RoomServiceFragment
import com.ufistudio.hotelmediabox.pages.setting.SettingFragment
import com.ufistudio.hotelmediabox.pages.setting.content.LanguageFragment
import com.ufistudio.hotelmediabox.pages.setting.content.UserGuideFragment
import java.lang.IllegalArgumentException

object Page {

    const val PAGE = "page"
    const val ARG_PAGE = "com.ufistudio.hotelmediabox.constants.Page.ARG_PAGE"
    const val ARG_BUNDLE = "com.ufistudio.hotelmediabox.constants.Page.ARG_BUNDLE"

    const val INVALID_PAGE = -1

    const val TEMPLATE = 1000
    const val HOME = 1001
    const val CHANNEL = 1002
    const val SMART_APPS = 1003
    const val ROOM_SERVICE = 1004
    const val ROOM_SERVICE_TYPE1 = 1005
    const val ROOM_SERVICE_TYPE2 = 1006
    const val HOTEL_FACILITIES = 1007
    const val HOTEL_FACILITIES_CONTENT = 1008
    const val SETTING = 1009
    const val LANGUAGE_SETTING = 1010
    const val USER_GUIDE = 1011
    const val NEARBY_ME = 1012

    /*--------------------------------------------------------------------------------------------*/
    /* Helpers */
    fun tag(page: Int): String = "page_$page"

    fun view(page: Int, args: Bundle): Fragment {
        var result: Fragment

        when (page) {
            TEMPLATE -> result = TemplateFragment.newInstance()
            HOME -> result = HomeFragment.newInstance()
            CHANNEL -> result = ChannelFragment.newInstance()
            SMART_APPS -> result = SmartAppsFragment.newInstance()
            ROOM_SERVICE -> result = RoomServiceFragment.newInstance()
            ROOM_SERVICE_TYPE1 -> result = TemplateType1Fragment.newInstance()
            ROOM_SERVICE_TYPE2 -> result = TemplateType2Fragment.newInstance()
            HOTEL_FACILITIES -> result = HotelFacilitiesFragment.newInstance()
            HOTEL_FACILITIES_CONTENT -> result = FacilitiesContentFragment.newInstance()
            SETTING -> result = SettingFragment.newInstance()
            LANGUAGE_SETTING -> result = LanguageFragment.newInstance()
            USER_GUIDE -> result = UserGuideFragment.newInstance()
            NEARBY_ME -> result = NearbyMeFragment.newInstance()
            else -> throw IllegalArgumentException("No match view! page = $page")
        }

        args.putInt(ARG_PAGE, page)
        putData(result, args)

        return result
    }

    private fun putData(fragment: Fragment, data: Bundle) {
        var args = fragment.arguments;
        if (args == null) {
            fragment.arguments = data
        } else {
            args.putAll(data)
        }
    }
}