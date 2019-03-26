package com.ufistudio.hotelmediabox.utils

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.v4.content.FileProvider
import android.util.Log
import com.google.gson.GsonBuilder
import com.ufistudio.hotelmediabox.BuildConfig
import com.ufistudio.hotelmediabox.interfaces.OnSaveFileStatusListener
import okhttp3.ResponseBody
import java.io.*
import java.nio.charset.Charset

const val TAG_DEFAULT_LOCAL_PATH: String = "/hotelBox"

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

    /**
     * Parse Json file from assets
     * @application: Application
     * @jsonName: json file name
     *
     * @return: json string
     */
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

    /**
     * Get Json String from storage
     * @path: the file path, Base is /sdcard/hotelBox/........
     * @fileName: This is your file name, ex: welcome.json
     */
    fun getJsonFromStorage(path: String, fileName: String): String {
        return parseJsonFileByInputStream(FileInputStream(getFileFromStorage(path, fileName)))
    }

    /**
     * Parse Json file By InputStream
     * @inputStream: Json file be convert by InputStream
     */
    fun parseJsonFileByInputStream(inputStream: FileInputStream?): String {
        var jsonString = ""
        if (inputStream != null) {
            try {
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                jsonString = String(buffer, Charset.forName("UTF-8"))

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return jsonString
    }

    /**
     * Get File from Storage
     * @path: the file path, Base is /sdcard/hotelBox/........
     * @fileName: This is your file name, ex: welcome.json
     */
    fun getFileFromStorage(path: String, fileName: String): File? {
        val file: File = File(String.format("%s%s%s", Environment.getExternalStorageDirectory(), TAG_DEFAULT_LOCAL_PATH, path), fileName)
        if (file.exists()) {
            return file
        }
        return null
    }

    @Throws(IOException::class)
    fun fileIsExist(saveDir: String): String {
        val downloadFile: File = File(Environment.getExternalStorageDirectory(), saveDir)
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile()
        }
        return downloadFile.absolutePath ?: throw IOException()

    }

    /**
     * 將檔案寫入SD card 預設路徑為sdcard/hotelBox
     * @body: url responseBody stream
     * @name: 欲存檔的檔名，ex: xxx.apk
     * @filePath:欲存檔案的目錄，ex: /xxx ，xxx為sdcard底下之目錄
     */
    fun writeResponseBodyToDisk(body: ResponseBody, name: String, filePath: String = TAG_DEFAULT_LOCAL_PATH, listener: OnSaveFileStatusListener): Boolean {
        listener.savingFileStart()
        try {
            val path: String = MiscUtils.fileIsExist(filePath)
            val file: File = File(path, name)
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val filReader: ByteArray = ByteArray(4096)
                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0

                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)

                while (true) {
                    val read: Int = inputStream.read(filReader)
                    if (read == -1) {
                        break
                    }

                    outputStream.write(filReader, 0, read)
                    fileSizeDownloaded += read

                    listener.savingFile(fileSizeDownloaded)
                }

                outputStream.flush()
                listener.savingFileSuccess(path, name)
                return true
            } catch (e: IOException) {
                listener.savingFileError(e)
                return false
            } finally {
                inputStream?.close()
                outputStream?.close()
                listener.savingFileEnd()
            }
        } catch (e: IOException) {
            listener.savingFileError(e)
            return false
        }

    }

    /**
     * install apk
     * @context: Context
     * @apkName: apk名字 ex: xxx.apk
     * @apkPath: apk存在的檔案路徑
     */
    fun installApk(context: Context, apkName: String, apkPath: String = TAG_DEFAULT_LOCAL_PATH) {
        val file: File = File(fileIsExist(apkPath), apkName)
        var fileUri: Uri = Uri.fromFile(file) //for Build.VERSION.SDK_INT <= 24
        if (Build.VERSION.SDK_INT >= 24) {
            fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
        }
        val intent: Intent = Intent(Intent.ACTION_VIEW, fileUri)
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }
}