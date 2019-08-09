package com.ufistudio.hotelmediabox.constants

object Cache {

    var RoomNumber: String? = ""
    var AppVersion: String? = ""
    var JVersion: String? = ""
    var IsDHCP: Boolean = true
    var IsVODEnable: Boolean = false

    var VodWatchHistory: MutableMap<String,Long> = mutableMapOf()
}