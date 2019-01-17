package com.ufistudio.hotelmediabox.pages.base

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.utils.ActivityUtils

open class PaneViewActivity : BaseActivity(), OnPageInteractionListener.Pane {

    private val TAG = PaneViewActivity::class.simpleName

    private var mTopFragment: SparseArray<String> = SparseArray(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.addOnBackStackChangedListener(object : FragmentManager.OnBackStackChangedListener {
            override fun onBackStackChanged() {
                for (i in 0 until mTopFragment.size()) {
                    var container = mTopFragment.keyAt(i)
                    var tag = mTopFragment.valueAt(i)
                    var currentTop = supportFragmentManager.findFragmentById(container)

                    if (currentTop != null && !TextUtils.equals(tag, currentTop.tag)) {

                        mTopFragment.put(container, currentTop.tag)
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        // Whether any fragment has consumed the back event
        var isConsumed = false
        var size = mTopFragment.size()
        var top: Fragment? = null
        for (i in 0 until size) {
            var container = mTopFragment.keyAt(i)
            top = supportFragmentManager.findFragmentById(container)
            if (top !is AppBaseView) continue
            var view: AppBaseView = top as AppBaseView
            if (view.onBackPressed()) {
                isConsumed = true
            }
        }
        if (isConsumed) return

//        if (supportFragmentManager.backStackEntryCount < 2) {
//            if (top != null && (top is InformationFragment || top is NewsFragment)) {
//                return
//            } else {
//                finish()
//                return
//            }
//        }
        super.onBackPressed()
    }

    override fun switchPage(@IdRes container: Int, page: Int, args: Bundle, addToBackStack: Boolean, withAnimation: Boolean) {
        if (isActivityDestroying())
            return
        Log.d(TAG, "[switchPage] page = $page")
        var nextView = findViewByPage(page)
        if (nextView == null)
            nextView = createNewPage(container, page, args)

        var tag = Page.tag(page)

        if (mTopFragment.indexOfKey(container) < 0)
            mTopFragment.put(container, tag)

        ActivityUtils.replcaeFragment(supportFragmentManager, nextView, container, tag, addToBackStack, withAnimation)
    }

    override fun addPage(@IdRes container: Int, page: Int, args: Bundle, addToBackStack: Boolean, withAnimation: Boolean) {
        if (isActivityDestroying())
            return
        Log.d(TAG, "[addPage] page = $page")
        var nextView = findViewByPage(page)

        if (nextView != null) {
            var tag = nextView.tag
            ActivityUtils.popBackByPageTag(supportFragmentManager, tag)
        } else {
            nextView = createNewPage(container, page, args)
            var tag = Page.tag(page)

            if (mTopFragment.indexOfKey(container) < 0)
                mTopFragment.put(container, tag)

            ActivityUtils.addFragment(supportFragmentManager, nextView, container, tag, addToBackStack, withAnimation)
        }
    }

    /*--------------------------------------------------------------------------------------------*/
    /* Internal helpers */

    private fun findViewByPage(page: Int): Fragment? {
        var view: Fragment? = null
        var tag = Page.tag(page)
        view = supportFragmentManager.findFragmentByTag(tag)
        return view
    }

    private fun createNewPage(@IdRes container: Int, page: Int, args: Bundle): Fragment {
        args.putInt(PaneView.ARG_CONTAINER, container)
        return Page.view(page, args)
    }
}