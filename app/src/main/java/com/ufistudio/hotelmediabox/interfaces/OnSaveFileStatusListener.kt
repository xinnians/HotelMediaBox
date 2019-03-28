package com.ufistudio.hotelmediabox.interfaces

import java.io.IOException

interface OnSaveFileStatusListener {

    /**
     * 開始儲存
     */
    fun savingFileStart()

    /**
     * 儲存中
     * @progress:目前寫入長度
     */
    fun savingFile(progress: Long)

    /**
     * 儲存結束
     */
    fun savingFileEnd()

    /**
     * 儲存失敗
     * e:Exception
     */
    fun savingFileError(e: IOException)

    /**
     * 儲存成功
     * @path:檔案位置
     * @chName:檔案名稱
     */
    fun savingFileSuccess(path: String, name: String)
}


