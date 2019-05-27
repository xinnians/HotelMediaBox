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
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.factory.FactoryActivity
import com.ufistudio.hotelmediabox.receivers.ACTION_UPDATE_APK
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList

const val TAG_KEY_AUDIO: Int = 300
const val TAG_KEY_HOME: Int = 302

open class PaneViewActivity : BaseActivity(), OnPageInteractionListener.Pane {

    private val TAG = PaneViewActivity::class.simpleName
    private var mReceiver: MyReceiver = MyReceiver()

    private var mTopFragment: SparseArray<String> = SparseArray(2)
    private val mFactoryKeyInput: ArrayList<Int> = ArrayList()
    private var mDisposable: Disposable? = null
    private var mFactoryDialog: AlertDialog? = null

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

        initFactoryEntry()
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

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.repeatCount!=0){
            return true
        }
        Log.d(TAG, "keycode = $keyCode  ,event = $event")

        when (keyCode) {
            TAG_KEY_AUDIO -> {
                mFactoryKeyInput.add(keyCode)
                checkFactoryKey()
            }
            TAG_KEY_HOME -> {
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }
        }

        if (keyCode != TAG_KEY_AUDIO)
            mFactoryKeyInput.clear()

        //將keycode 傳入當前Fragment
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

    /**
     * Init factory entry dialog
     */
    private fun initFactoryEntry() {
        mFactoryDialog = AlertDialog.Builder(this)
                .setTitle("Factory password")
                .setView(R.layout.view_edittext)
                .setNegativeButton(android.R.string.cancel, null)
                .setOnDismissListener { mFactoryDialog?.findViewById<EditText>(R.id.editText_password)?.setText("") }
                .create()
    }

    /**
     * 檢查進入Factory page
     */
    private fun checkFactoryKey() {
        val correctCode = resources.getIntArray(R.array.factory_key)
        if (mFactoryKeyInput.size >= correctCode.size)
            mDisposable = Observable.fromCallable{mFactoryKeyInput}
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (Arrays.equals(it.toIntArray(), correctCode)) {
                            mFactoryDialog?.show()
                            mFactoryDialog?.findViewById<EditText>(R.id.editText_password)?.let {
                                Log.d("neo", "find view")
                                it.addTextChangedListener(object : TextWatcher {
                                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                                    }

                                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                    }

                                    override fun afterTextChanged(s: Editable?) {
                                        if (s?.length == FactoryActivity.mFactoryCode.length) {
                                            if (TextUtils.equals(it.text, FactoryActivity.mFactoryCode)) {
                                                mFactoryDialog?.dismiss()
                                                startActivity(Intent(applicationContext, FactoryActivity::class.java))
                                                finish()
                                            }
                                        }
                                    }
                                })
                            }
                        }
                        mFactoryKeyInput.clear()
                    }, {
                        Log.d(TAG, "checkFactoryKey factory key mapping error = $it")
                    })
    }
}