package com.ufistudio.hotelmediabox.views

import android.app.DialogFragment
import android.app.FragmentManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import com.ufistudio.hotelmediabox.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class MuteView : DialogFragment() {

    private val WAIT_TIME: Long = 2000L
    private var mDisposableInfoView: Disposable? = null

    companion object {
        private val TAG = MuteView::class.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setOnKeyListener { dialog, keyCode, event ->
            if(keyCode == KeyEvent.KEYCODE_VOLUME_MUTE){
                return@setOnKeyListener true
            }
            false }
        return inflater.inflate(R.layout.view_mute, container)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

        hide()
    }

    fun showDialogAllowingStateLoss(fragmentManager: FragmentManager, tag: String) {
        var ft = fragmentManager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }

    fun hide(){
        if (mDisposableInfoView != null && !mDisposableInfoView!!.isDisposed) {
            mDisposableInfoView?.dispose()
        }

        mDisposableInfoView = Observable.timer(1000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { Log.e(TAG, " hide continus : $it") },
                { onError -> Log.e(TAG, "error:$onError") },
                {
                    dialog?.dismiss()
                    Log.e(TAG, " hide finish")
                })
    }


}