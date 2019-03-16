package com.ufistudio.hotelmediabox.utils

import android.app.Application
import android.util.Log
import com.google.gson.GsonBuilder
import java.io.IOException
import java.nio.charset.Charset

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

    fun parseJsonFile(application: Application, jsonName: String): String {
        var jsonString = ""
        try {
            val inputStream = application.assets.open(jsonName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            jsonString = String(buffer, Charset.forName("UTF-8"))

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return jsonString
    }
}