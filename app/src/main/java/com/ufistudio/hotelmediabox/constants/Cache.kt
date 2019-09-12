package com.ufistudio.hotelmediabox.constants

import com.ufistudio.hotelmediabox.repository.data.HotelFacilitiesContent
import com.ufistudio.hotelmediabox.repository.data.Memo

object Cache {

    var RoomNumber: String? = ""
    var AppVersion: String? = ""
    var JVersion: String? = ""
    var IsDHCP: Boolean = true
    var IsVODEnable: Boolean = false

    var VodWatchHistory: MutableMap<String,Long> = mutableMapOf()

    var Memos: ArrayList<Memo> = arrayListOf()
    var WifiId: String = ""
    var WifiPassword: String = ""

    var HotelFacilitiesContents: List<HotelFacilitiesContent>? = arrayListOf()

    //PMS Function
    var ServerIP: String? = ""
    var IsMessageHintShow: Boolean = false
    var IsMessageUpdate: Boolean = false
    val TempAPIHeader: String = "http://"
    val TempAPIGetGuestMessage: String = "/api/message/list"
}