package com.ufistudio.hotelmediabox.receivers

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.text.TextUtils
import android.util.Log

const val ACTION_UPDATE_APK = "UpdateApk"
const val TAG_FORCE = "force"

class MyReceiver : BroadcastReceiver() {
    private val TAG = MyReceiver::class.java.simpleName

    private var mBuild: AlertDialog.Builder? = null
    private var mDialog: AlertDialog? = null
    override fun onReceive(context: Context?, intent: Intent?) {

        Log.d(TAG, "broadcast receiver")

        if (intent?.action == ACTION_UPDATE_APK) {
            Log.d(TAG,"intent.extras.getString(TAG_FORCE):${intent.extras.getString(TAG_FORCE)}")
            if (TextUtils.equals(intent.extras.getString(TAG_FORCE),"1")) {
                forceRestart(context)
                return
            }
            if (mBuild == null)
                mBuild = AlertDialog.Builder(context)
                        .setTitle("Notification")
                        .setMessage("Would you want to install new version app and restart?")
                        .setPositiveButton(android.R.string.ok, object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                forceRestart(context)
                            }
                        })
                        .setNegativeButton(android.R.string.cancel) { dialog, which -> }

            if (mDialog == null) {
                mDialog = mBuild?.create()!!
                mDialog?.setCanceledOnTouchOutside(false)
            }
            if (!mDialog?.isShowing!!)
                mDialog?.show()
        }
    }

    /**
     * 強制導到com.fdi.customlauncher重啟
     */
    fun forceRestart(context: Context?) {
        val i: Intent? = context?.packageManager?.getLaunchIntentForPackage("com.fdi.customlauncher")
        if (i != null)
            context.startActivity(i)
    }
}