package com.ufistudio.hotelmediabox.utils

import android.util.Log
import com.google.gson.GsonBuilder

object MiscUtils {

    private val TAG = MiscUtils::class.simpleName

    fun toJSONString(obj: Any?): String {
        var result = ""

        if (obj == null)
            return result
        else {
            try {
                result = GsonBuilder().create().toJson(obj)
            } catch (e: Exception) {
                Log.e(TAG, "Fail to serialize object!", e);
            }
        }
        return result
    }

    inline fun <reified T> parseJSONList(jsonArray: String?): ArrayList<T> {
        if (!jsonArray.isNullOrEmpty())
            return GsonBuilder().create().fromJson(jsonArray, ArrayList<T>()::class.java)
        else
            return ArrayList()
    }

}