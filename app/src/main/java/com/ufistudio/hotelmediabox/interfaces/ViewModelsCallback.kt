package com.ufistudio.hotelmediabox.interfaces

interface ViewModelsCallback {
    fun onSuccess(it: Any? = null)
    fun onError(t: Throwable? = null)
    fun onProgress(b: Boolean = false)
}