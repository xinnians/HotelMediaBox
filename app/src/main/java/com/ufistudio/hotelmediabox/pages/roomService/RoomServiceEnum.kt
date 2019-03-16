package com.ufistudio.hotelmediabox.pages.roomService

import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Page

enum class RoomServiceEnum(title: Int, page: Int) {
    HOUSE_KEEPING(R.string.room_service_title_house_keeping, Page.ROOM_SERVICE_TYPE1),
    FOOD_BEVERAGE(R.string.room_service_title_food_beverage, Page.ROOM_SERVICE_TYPE2)
    ;


    var title: Int = title
    var page: Int = page
}