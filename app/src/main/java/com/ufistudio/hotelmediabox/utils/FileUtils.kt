package com.ufistudio.hotelmediabox.utils

import android.os.Environment
import android.text.TextUtils
import android.util.Log
import com.ufistudio.hotelmediabox.interfaces.OnSaveFileStatusListener
import com.ufistudio.hotelmediabox.interfaces.OnSimpleListener
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import java.io.*
import java.lang.Exception
import java.lang.NullPointerException
import java.nio.channels.FileChannel
import java.nio.charset.Charset

object FileUtils {

    val TAG: String = FileUtils.javaClass.simpleName

    /**
     * 取得裝置內的Hotel資料夾
     * @return: all of the hotel directory in device directory
     */
    fun getInsideHotel(): File? {
        return if (File("/data$TAG_DEFAULT_LOCAL_PATH").exists()) File("/data$TAG_DEFAULT_LOCAL_PATH") else null
    }

    /**
     * 取得USB的整個儲存體內容
     * @return: all of the usb directory
     */
    fun getUSBFiles(): File? {
        for (file in File("/storage/").listFiles()) {
            if (!TextUtils.equals(file.name, "self") && !TextUtils.equals(file.name, "emulated")) {
                return file
            }
        }
        return null
    }

    /**
     * Get File from Storage
     * @path: the file path, Base is /sdcard/hotel/........
     * @fileName: This is your file name, ex: welcome.json
     */
    fun getFileFromStorage(fileName: String, path: String = ""): File? {
        var file: File? = null
        try {
            file = File(String.format("%s%s%s", "/data", TAG_DEFAULT_LOCAL_PATH, path), fileName)
            Log.d("getFileFromStorage", "file path = ${file.absolutePath}")
        } catch (e: NullPointerException) {
            Log.e("getFileFromStorage", "error = $e")
        } catch (e: FileNotFoundException) {
            Log.e("FileNotFoundException", "error = $e")
        }
        if (file?.exists()!!) {
            return file
        }
        return null
    }

    /**
     * 檢查檔案是否存在，不存在則新增
     * @saveDir:檢查在 /storage/0/$saveDir 是否存在
     *
     * @return 該檔案的absolutePath
     */
    @Throws(IOException::class)
    fun fileIsExist(saveDir: String): String {
        val downloadFile: File = File("/data", saveDir)
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile()
        }
        return downloadFile.absolutePath ?: throw IOException()

    }

    /**
     * 檢查檔案是否存在
     *
     *
     * @return 該檔案是否存在
     */
    fun fileIsExists(fileName: String): Boolean {
        try {
            var file = File("/data", TAG_DEFAULT_LOCAL_PATH + fileName)
            if (!file.exists()) {
                return false
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    /**
     * 將檔案寫入SD card 預設路徑為sdcard/hotelBox
     * @body: url responseBody stream
     * @name: 欲存檔的檔名，ex: xxx.apk
     * @filePath:欲存檔案的目錄，ex: /xxx ，xxx為sdcard底下之目錄
     */
    fun writeResponseBodyToDisk(
            body: ResponseBody,
            name: String,
            filePath: String = TAG_DEFAULT_LOCAL_PATH,
            listener: OnSaveFileStatusListener? = null
    ): Boolean {
        listener?.savingFileStart()
        try {
            val path: String = fileIsExist(filePath)
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

                    listener?.savingFile(fileSizeDownloaded)
                }

                outputStream.flush()
                listener?.savingFileSuccess(path, name)
                return true
            } catch (e: IOException) {
                listener?.savingFileError(e)
                return false
            } finally {
                inputStream?.close()
                outputStream?.close()
                listener?.savingFileEnd()
            }
        } catch (e: IOException) {
            listener?.savingFileError(e)
            return false
        }

    }

    /**
     * copy file
     * @sourceFile:
     * @targetFile:
     */
    @Throws(FileNotFoundException::class)
    fun copyFile(sourceFile: File, targetFile: File, listener: OnSaveFileStatusListener? = null) {
        listener?.savingFileStart()
        if (!targetFile.parentFile.exists()) {
            targetFile.parentFile.mkdirs()
        }

        if (!targetFile.exists()) {
            targetFile.createNewFile()
        }
        var source: FileChannel? = null
        var target: FileChannel? = null
        try {
            source = FileInputStream(sourceFile).channel
            target = FileOutputStream(targetFile).channel
            target.transferFrom(source, 0, source.size())
            listener?.savingFileSuccess(targetFile.path, targetFile.name)
        } catch (e: IOException) {
            Log.e("copyFile", "$e")
            listener?.savingFileError(e)
        } finally {
            if (source != null) {
                source.close()
            }
            if (target != null) {
                target.close()
            }
            listener?.savingFileEnd()
        }
    }

    /**
     * copy file or directory
     * @sourceLocation:
     * @targetLocation:
     */
    fun copyFileOrDirectory(
            sourceLocation: String,
            targetLocation: String,
            listener: OnSaveFileStatusListener? = null
    ) {
        val source: File = File(sourceLocation)
        val target: File = File(targetLocation, source.name)

        Log.d("neo", "source = " + source.absoluteFile)
        Log.d("neo", "target = " + target.absoluteFile)
        try {
            if (source.isDirectory) {
                val files: Array<out String> = source.list()
                val fileLength: Int = files.size
                for (i in 0 until fileLength) {
                    val source1 = File(source, files[i]).path
                    val target1 = target.path
                    copyFileOrDirectory(source1, target1)
                }
            } else {
                copyFile(source, target, listener)
            }
        } catch (e: IOException) {
            Log.e("copyFileOrDirectory", "$e")
        } catch (e: NoSuchFileException) {
            Log.e("copyFileOrDirectory", "$e")
        }
    }

    /**
     * 從USB根目錄Import hotel資料
     */
    fun importFile(listener: OnSimpleListener? = null) {
        listener?.callback("start import")
        val usbDir = getUSBFiles()
        if (usbDir == null) {
            listener?.callback("can't find usb")
            return
        }
        listener?.callback("usb = ${usbDir.name}")
        try {
            for (item in File("/mnt/media_rw/${usbDir.name}").list()) {
                Log.d("importFile", "item = $item")
                if (TextUtils.equals(item.substring(0, 4), "box_")) {
                    listener?.callback("coping file -> $item")
                    FileUtils.copyFile(
                            File("/mnt/media_rw/${usbDir.name}/$item"),
                            File("/data/hotel/$item")
                    )
                } else {
                    Log.d("importFile", "$item is not box_ file")
                }
            }
        } catch (e: IOException) {
            Log.e("importFile", "Error = $e")
            listener?.callback("error occur -> $e")
        } catch (e: NullPointerException) {
            Log.e("importFile", "Error = $e")
            listener?.callback("error occur -> $e")
        }
        listener?.callback("import finish")
    }

    /**
     * 輸出hotel資料夾至Usb
     */
    fun exportFile(listener: OnSimpleListener?) {
        listener?.callback("start export")
        val usbDir = getUSBFiles()
        if (usbDir == null) {
            listener?.callback("can't find usb")
            return
        }
        listener?.callback("usb = ${usbDir.name}")

        try {

            for (item in File("/data/hotel").list()) {
                Log.d("exportFile", "item = $item")
                listener?.callback("coping file -> $item")

                FileUtils.copyFile(
                        File("/data/hotel/$item"),
                        File("/mnt/media_rw/$usbDir/$item")
                )
            }
        } catch (e: IOException) {
            Log.e("exportFile", "Error = $e")
            listener?.callback("error occur -> $e")
        } catch (e: NullPointerException) {
            Log.e("exportFile", "Error = $e")
            listener?.callback("error occur -> $e")
        }


        listener?.callback("export finish")
    }

    /**
     * Write string to file
     */
    fun writeToFile(file: File, data: String) {
        var osw: FileOutputStream? = null
        try {
            osw = FileOutputStream(file)
            osw.write("".toByteArray())
            osw.flush()
            Log.d("writeToFile", "data.() = ${data}")
            osw.write(data.toByteArray(Charset.defaultCharset()))
            osw.flush()
        } catch (e: Exception) {
        } finally {
            try {
                osw!!.close()
            } catch (e: Exception) {
            }

        }
    }
}