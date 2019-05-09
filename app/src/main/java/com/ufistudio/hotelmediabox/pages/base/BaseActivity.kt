package com.ufistudio.hotelmediabox.pages.base

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ufistudio.hotelmediabox.pages.base.BaseActivity.TypeFaceProvider.Montserrat_Light
import com.ufistudio.hotelmediabox.pages.base.BaseActivity.TypeFaceProvider.Montserrat_Medium
import java.util.*

abstract class BaseActivity : AppCompatActivity(), OnPageInteractionListener.Base {

    private val TAG = BaseActivity::class.simpleName

//    private var mFullScreenMessage: FullScreenMessage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initFullScreenMessage()
        initFontStyle()
    }

    /**
     * 將系統預設的Serif字型設成Montserrat_Light 以及Monospace替換成Montserrat_Medium
     * 而預設使用字型經由style設成Serif，所以若需要使用Montserrat_Medium須在該元件上進行設定android:typeface="monospace"
     * */
    private fun initFontStyle() {
        try {
            val fieldSerif = Typeface::class.java.getDeclaredField("SERIF")
            fieldSerif.isAccessible = true
            fieldSerif.set(null, TypeFaceProvider.getTypeFace(this, Montserrat_Light))

            val fieldMonospace = Typeface::class.java.getDeclaredField("MONOSPACE")
            fieldMonospace.isAccessible = true
            fieldMonospace.set(null, TypeFaceProvider.getTypeFace(this, Montserrat_Medium))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun pressBack() {
        if (isActivityDestroying())
            return
        onBackPressed()
    }

    override fun showFullScreenLoading() {
//        if (isActivityDestroying()) return
//        else mFullScreenMessage?.let { it.showLoading(supportFragmentManager) }
    }

    override fun hideFullScreenOverlay() {
//        if (isActivityDestroying() || mFullScreenMessage == null || !mFullScreenMessage?.isShowing()!!)
//            return
//        mFullScreenMessage?.dismissAllowingStateLoss()
    }

    /*--------------------------------------------------------------------------------------------*/
    /* Internal helpers */
    fun isActivityDestroying(): Boolean {
        if (isFinishing)
            return true
        return false
    }

    private fun initFullScreenMessage() {
//        mFullScreenMessage = FullScreenMessage.newInstance()
//        mFullScreenMessage?.isCancelable = false
    }

    object TypeFaceProvider {

        const val Montserrat_Light = "Montserrat-Light.ttf"
        const val Montserrat_Medium = "Montserrat-Medium.ttf"

        private val typeFaces = Hashtable<String, Typeface>(3)

        fun getTypeFace(context: Context, fontName: String): Typeface? {
            var typeface = typeFaces[fontName]

            if (typeface == null) run {
                var fontPath: String = "fonts/$fontName"
                typeface = Typeface.createFromAsset(context.assets, fontPath)

                typeFaces.put(fontName, typeface)
            }

            return typeface
        }

    }
}