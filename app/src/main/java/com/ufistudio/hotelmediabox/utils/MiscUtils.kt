package com.ufistudio.hotelmediabox.utils

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ufistudio.hotelmediabox.BuildConfig
import com.ufistudio.hotelmediabox.repository.data.Config
import java.io.*
import java.nio.charset.Charset

const val TAG_DEFAULT_LOCAL_PATH: String = "/hotel/"
const val TAG_DEFAULT_CORRECTION_PATH: String = "/correction/"
const val TAG_DEFAULT_APK_NAME: String = "box_hotel.apk"
const val TAG_DEFAULT_HOTEL_TAR_FILE_NAME: String = "hotel.tar"

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
                Log.e(TAG, "Fail to serialize object!", e)
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
     * 根據語言選取json file
     * @fileName: json file不加前、後贅字 ex: box_home_en.json 只需要傳 home
     *
     * 若找不到，預設英文
     */
    fun getJsonLanguageAutoSwitch(fileName: String): String {
        Log.d("neo","安安 $fileName")
        val gson = Gson()
        var config: Config? = null
        try {
            config = gson.fromJson(getJsonFromStorage("box_config.json"), Config::class.java)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "getJsonLanguageAutoSwitch = $e")
            return ""
        }

        if (config == null) {
            return ""
        }

        var finalName: String = ""
        finalName = if (File("/data/$TAG_DEFAULT_LOCAL_PATH${fileName}_${config.config.language}.json").exists()) {
            "box_${fileName}_${config.config.language}.json"
        } else {
            "box_${fileName}_en.json"
        }

        Log.d(TAG, "new file name = $finalName")
        return getJsonFromStorage(finalName)
    }

    /**
     * Get Json String from storage
     * @path: the file path, Base is /sdcard/hotelBox/........
     * @fileName: This is your file name, ex: welcome.json
     */
//    fun getJsonFromStorage(fileName: String, path: String = ""): String {
//        try {
//            return parseJsonFileByInputStream(FileInputStream(FileUtils.getFileFromStorage(path, fileName)))
//        } catch (e: java.lang.NullPointerException) {
////
//            Log.e("parseJsonFile", "Error ${e}")
//        }
//        return ""
//    }
    fun getJsonFromStorage(fileName: String, path: String = ""): String {

        return FileUtils.getFileFromStorage(fileName, path)?.let { file ->
            parseJsonFileByInputStream(
                    FileInputStream(
                            file
                    )
            )
        }
                ?: ""

//        return parseJsonFileByInputStream(FileInputStream(FileUtils.getFileFromStorage(path, fileName)))
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
                Log.e("parseJsonFile", "Error ${e}")
            } catch (e: NullPointerException) {
                Log.e("parseJsonFile", "Error ${e}")
            }
        }
        return jsonString
    }


//    /**
//     * 將檔案寫入SD card 預設路徑為sdcard/hotelBox
//     * @body: url responseBody stream
//     * @name: 欲存檔的檔名，ex: xxx.apk
//     * @filePath:欲存檔案的目錄，ex: /xxx ，xxx為sdcard底下之目錄
//     */
//    fun writeResponseBodyToDisk(
//        body: ResponseBody,
//        name: String,
//        filePath: String = TAG_DEFAULT_LOCAL_PATH,
//        listener: OnSaveFileStatusListener
//    ): Boolean {
//        listener.savingFileStart()
//        try {
//            val path: String = MiscUtils.fileIsExist(filePath)
//            val file: File = File(path, name)
//            var inputStream: InputStream? = null
//            var outputStream: OutputStream? = null
//            try {
//                val filReader: ByteArray = ByteArray(4096)
//                val fileSize = body.contentLength()
//                var fileSizeDownloaded: Long = 0
//
//                inputStream = body.byteStream()
//                outputStream = FileOutputStream(file)
//
//                while (true) {
//                    val read: Int = inputStream.read(filReader)
//                    if (read == -1) {
//                        break
//                    }
//
//                    outputStream.write(filReader, 0, read)
//                    fileSizeDownloaded += read
//
//                    listener.savingFile(fileSizeDownloaded)
//                }
//
//                outputStream.flush()
//                listener.savingFileSuccess(path, name)
//                return true
//            } catch (e: IOException) {
//                listener.savingFileError(e)
//                return false
//            } finally {
//                inputStream?.close()
//                outputStream?.close()
//                listener.savingFileEnd()
//            }
//        } catch (e: IOException) {
//            listener.savingFileError(e)
//            return false
//        }
//
//    }

    /**
     * install apk
     * @context: Context
     * @apkName: apk名字 ex: xxx.apk
     * @apkPath: apk存在的檔案路徑
     */
    fun installApk(context: Context, apkName: String, apkPath: String = TAG_DEFAULT_LOCAL_PATH): Boolean {
        var haveFile: Boolean = true
        if (File(apkPath, apkName).exists()) {
            val file: File = File(apkPath, apkName)
            var fileUri: Uri = Uri.fromFile(file) //for Build.VERSION.SDK_INT <= 24
            if (Build.VERSION.SDK_INT >= 24) {
                fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
            }
            val intent: Intent = Intent(Intent.ACTION_VIEW, fileUri)
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            intent.setDataAndType(fileUri, "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
        } else {
            haveFile = false
        }
        return haveFile
    }


    @Throws(IOException::class)
    fun execCommand(command: String) {

        //        String cmds = "su;" + command;


        val runtime = Runtime.getRuntime()
        //        Process proc = runtime.exec(cmds);
        val proc = runtime.exec(command)
        //        DataOutputStream os = new DataOutputStream(proc.getOutputStream());
        //        os.writeBytes(command+"\n");
        //        os.writeBytes("exit\n");
        //        os.flush();
        try {
            val stdInput = BufferedReader(InputStreamReader(proc.inputStream))

            val stdError = BufferedReader(InputStreamReader(proc.errorStream))

            Log.e(TAG, "Here is the standard output of the command")

            var s: String? = stdInput.readLine()
            while ((s) != null) {
                //                System.out.println(s);
                Log.e(TAG, s)
            }

            // read any errors from the attempted command
            Log.e(TAG, "Here is the standard error of the command")
            s = stdError.readLine()
            while ((s) != null) {
                //                System.out.println(s);
                Log.e(TAG, s)
            }


            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue())
            }
        } catch (e: InterruptedException) {
            System.err.println(e)
        }
    }

    /**
     * Open System Setting page
     */
    fun openSetting(context: Context) {
        context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
    }

    /**
     * Get Wifi MAC address
     */
    fun getWifiMACAddress(context: Context): String {
        return (context.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo.macAddress
    }

    /**
     * Get IP address
     *
     * 取得的值是錯的
     */
    fun getIpAddress(context: Context): String {
        val ip: Int = (context.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo.ipAddress

        return ip.toString()
    }

    /**
     * Get Room Number
     */
    fun getRoomNumber(): String {
        val gson = Gson()
        val json = getJsonFromStorage("box_config.json")
        return if (!TextUtils.isEmpty(json)) {
            val config = gson.fromJson(json, Config::class.java)
            config.config.room
        } else {
            ""
        }
    }

    /**
     * reboot device
     */
    fun reboot(context: Context) {
        try {
            Log.e("reboot", "reboot now")
            val pm: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.reboot(null)
        } catch (e: SecurityException) {
            Log.e("reboot", "error = $e")
        }
    }

    fun getLocalVersion(ctx: Context): Int {
        var localVersion: Int = 0
        try {
            var packageInfo: PackageInfo = ctx.applicationContext
                    .packageManager
                    .getPackageInfo(ctx.packageName, 0)
            localVersion = packageInfo.versionCode
            Log.d(TAG, "版本号：$localVersion")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return localVersion
    }

    fun getLocalVersionName(ctx: Context): String {
        var localVersion: String = ""
        try {
            var packageInfo: PackageInfo = ctx.applicationContext
                    .packageManager
                    .getPackageInfo(ctx.packageName, 0)
            localVersion = packageInfo.versionName
            Log.d(TAG, "版本名：$localVersion")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return localVersion
    }
}