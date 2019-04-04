package com.ufistudio.hotelmediabox.interfaces

import android.view.KeyEvent

interface OnFragmentKeyListener {
    fun onKeyPress(keyCode: Int, event: KeyEvent?): Boolean
}