package com.ufistudio.hotelmediabox.pages

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.pages.base.PaneViewActivity

class MainActivity : PaneViewActivity(), OnPageInteractionListener.Primary {

    private val TAG = MainActivity::class.java.simpleName

    private var mFragmentCacheData: Any? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        var intent = intent

        var args = intent.extras
        var page = args?.getInt("page")
//        intent?.let {
//            page = it.getIntExtra(PAGE,page)
//            args = it.getBundleExtra(EX)
//        }
//        switchPage(page, args)

        if (page == null || page == 0){
            switchPage(R.id.fragment_container, Page.HOME, Bundle(), true, false)
        }
        else{
            Log.e(TAG,"[get page] = $page")
            switchPage(R.id.fragment_container, page, Bundle(), true, false)
        }

    }

    private fun init() {

    }

    //    /**
//     * 切換頁面
//     * @page 傳進來的page代號
//     * @bundle 需要傳遞的bundle
//     */
//    private fun switchPage(page: Int, bundle: Bundle) {
//        when (page) {
//            Page.NEWS -> setButtonClick(view_icon_news)
//            Page.INFORMATION -> {
//                setButtonClick(view_icon_information)
//                bundle.putString(PAGE_TYPE, Constants.DataType.products.toString())
//            }
//            Page.TOPICS -> {
//                setButtonClick(view_icon_topic)
//                bundle.putString(PAGE_TYPE, Constants.DataType.goods.toString())
//            }
//            Page.LOCAL_FARMER -> {
//                setButtonClick(view_icon_local_farmer)
//                bundle.putString(PAGE_TYPE, Constants.DataType.local.toString())
//            }
//        }
//
//        switchPage(R.id.fragment_container, page, bundle, true, false)
//    }
    override fun setFragmentCacheData(data: Any?) {
        mFragmentCacheData = data
    }

    override fun getFragmentCacheData(): Any? {
        return mFragmentCacheData
    }

    override fun clearFragmentCacheData() {
        mFragmentCacheData = null
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }
}

