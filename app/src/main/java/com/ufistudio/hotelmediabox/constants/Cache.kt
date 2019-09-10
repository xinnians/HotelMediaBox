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

}