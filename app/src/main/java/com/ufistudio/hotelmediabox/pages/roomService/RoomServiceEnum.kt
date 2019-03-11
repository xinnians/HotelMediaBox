package com.ufistudio.hotelmediabox.pages.roomService

import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Page

enum class RoomServiceEnum(title: Int, page: Int) {
    HOUSE_KEEPING(R.string.room_service_title_house_keeping, Page.HOUSE_KEEPING),
    FOOD_BEVERAGE(R.string.room_service_title_food_beverage, Page.FOOD_BEVERAGE)
    ;


    var title: Int = title
    var page: Int = page
}