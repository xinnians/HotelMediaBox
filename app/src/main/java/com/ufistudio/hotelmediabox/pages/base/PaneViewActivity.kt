package com.ufistudio.hotelmediabox.pages.base

import android.content.Intent
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.KeyEvent
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.pages.MainActivity
import com.ufistudio.hotelmediabox.receivers.MyReceiver
import com.ufistudio.hotelmediabox.utils.ActivityUtils
import android.content.IntentFilter
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.factory.FactoryActivity
import com.ufistudio.hotelmediabox.receivers.ACTION_UPDATE_APK
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList


open class PaneViewActivity : BaseActivity(), OnPageInteractionListener.Pane {

    private val TAG = PaneViewActivity::class.simpleName
    private var mReceiver: MyReceiver = MyReceiver()

    private var mTopFragment: SparseArray<String> = SparseArray(2)
    private val mFactoryKeyInput: ArrayList<Int> = ArrayList()
    private var mDisposable: Disposable? = null

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

    override fun switchPage(@IdRes container: Int, page: Int, args: Bundle, addToBackStack: Boolean, withAnimation: Boolean, createNewFragment: Boolean) {
        if (isActivityDestroying())
            return
        Log.d(TAG, "[switchPage] page = $page")
        var nextView = findViewByPage(page)
        if (nextView == null || createNewFragment)
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(TAG, "keycode = $keyCode  ,event = $event")
        mFactoryKeyInput.add(keyCode)
        checkFactoryKey()

        if (keyCode == 302) {
//            var intent: Intent = Intent(this, FullScreenActivity::class.java)
//            intent.putExtra("page",Page.HOME)
            startActivity(Intent(this, MainActivity::class.java))
            return true
        }
        val container = mTopFragment.keyAt(0)
        val top = supportFragmentManager.findFragmentById(container)
        if (top is AppBaseView) {
            val view: AppBaseView = top as AppBaseView
            if (view.onFragmentKeyDown(keyCode, event))
                return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter()
        filter.addAction(ACTION_UPDATE_APK)
        registerReceiver(mReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(mReceiver)
    }

    private fun checkFactoryKey() {
        if (mFactoryKeyInput.size >= 8)
            mDisposable = Observable.just(mFactoryKeyInput)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (Arrays.equals(it.subList(it.size - 8, it.size).toIntArray(), resources.getIntArray(R.array.factory_key))) {
                            startActivity(Intent(this, FactoryActivity::class.java))
                            finish()
                        }
                        if (mFactoryKeyInput.size > 100) {
                            mFactoryKeyInput.clear()
                        }
                    }, {
                        Log.d(TAG, "checkFactoryKey factory key mapping error = $it")
                    })
    }
}