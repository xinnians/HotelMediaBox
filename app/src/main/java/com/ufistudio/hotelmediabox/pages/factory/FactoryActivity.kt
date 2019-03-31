package com.ufistudio.hotelmediabox.pages.factory

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.interfaces.OnItemClickListener
import com.ufistudio.hotelmediabox.interfaces.OnSimpleListener
import com.ufistudio.hotelmediabox.interfaces.ViewModelsCallback
import com.ufistudio.hotelmediabox.pages.welcome.WelcomeActivity
import com.ufistudio.hotelmediabox.repository.data.Config
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.utils.MiscUtils
import kotlinx.android.synthetic.main.activity_factory.*
import java.lang.StringBuilder

class FactoryActivity : AppCompatActivity(), OnItemClickListener, ViewModelsCallback {
    private val mAdapter: FactoryAdapter = FactoryAdapter()
    private lateinit var mViewModel: FactoryViewModel
    private var mDownloadDialog: android.app.AlertDialog? = null
    private var mData: Config? = null

    private val mInfo1: StringBuilder = StringBuilder()
    private val mInfo2: StringBuilder = StringBuilder()

    companion object {
        private val TAG = FactoryActivity::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_factory)

        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initConfigProgress.observe(this, Observer {
            onProgress(it!!)
        })
        mViewModel.initConfigSuccess.observe(this, Observer {
            onSuccess(it)
        })
        mViewModel.initConfigError.observe(this, Observer { onError(it) })

        mViewModel.fileDownloadProgress.observe(this, Observer {
            downloadFileProgress(it)
        })
        mViewModel.fileDownloadError.observe(this, Observer {
            downloadFileFailed(it)
        })
        mViewModel.fileDownloadSuccess.observe(this, Observer {
            downloadFileSuccess(it!!)
        })
    }

    override fun onStart() {
        super.onStart()
        recyclerview_content.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.HORIZONTAL, false)
        recyclerview_content.adapter = mAdapter
        mAdapter.setOnClickListener(this)

        textView_info1.movementMethod = ScrollingMovementMethod()
        textView_info2.movementMethod = ScrollingMovementMethod()

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onClick(view: View?) {
        when (view?.tag as FactoryFeature) {
            FactoryFeature.CHECK_UPGRADE_FROM_URL -> {
                mInfo1.setLength(0)
                if (TextUtils.isEmpty(mData?.config?.upgradeUrl!!)) {
                    mInfo1.append("找不到url")
                    textView_info1.text = mInfo1
                    return
                }
                mInfo1.append("檢查更新網址：${mData?.config?.upgradeUrl}\n")
                textView_info1.text = mInfo1
                mViewModel.downloadFileWithUrl(mData?.config?.upgradeUrl!!)
            }
            FactoryFeature.CHECK_UPGRADE_FROM_USB -> {
                if (!MiscUtils.installApk(this, "hotelbox.apk", "${FileUtils.getUSBFiles()?.path}")) {
                    mInfo1.setLength(0)
                    mInfo1.append("找不到 hotelbox.apk 安裝檔")

                    Log.d("neo", "找不到裝置")
                }
                textView_info1.text = mInfo1
                Log.d("neo", "找得到裝置")
            }
            FactoryFeature.EXPORT_JSON_FILE -> {
                mInfo1.setLength(0)
                FileUtils.exportFile(object : OnSimpleListener {
                    override fun callback(msg: String?) {
                        mInfo1.append("$msg\n")
                        textView_info1.text = mInfo1
                    }
                })
            }
            FactoryFeature.IMPORT_JSON_FILE -> {
                mInfo1.setLength(0)
                FileUtils.importFile(object : OnSimpleListener {
                    override fun callback(msg: String?) {
                        mInfo1.append("$msg\n")
                        textView_info1.text = mInfo1
                    }
                })
            }
            FactoryFeature.OPEN_SETTING -> {
                MiscUtils.openSetting(this)
            }
            FactoryFeature.OPEN_DEFAULT_LAUNCHER -> {
//                startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER))
                val startMain: Intent = Intent(Intent.ACTION_MAIN)
                startMain.addCategory(Intent.CATEGORY_HOME)
                startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(startMain)
            }
            FactoryFeature.SHOW_INSIDE_HOTEL -> {
                mInfo1.setLength(0)
                val hotelFile = FileUtils.getInsideHotel()
                if (hotelFile != null) {
                    mInfo1.append("${hotelFile.absolutePath} ( ${hotelFile.list().size} )\n")
                    for (file in hotelFile.list()) {
                        mInfo1.append("$file\n")
                    }
                } else {
                    mInfo1.append("Can not read hotel directory of inside storage")
                }

                textView_info1.text = mInfo1

            }
            FactoryFeature.SHOW_OUTSIDE_USB -> {
                mInfo2.setLength(0)
                val usbFile = FileUtils.getUSBFiles()
                if (usbFile != null && usbFile.exists()) {
                    mInfo2.append("${usbFile.absolutePath} ( ${usbFile.list().size} )\n")
                    for (file in usbFile.list()) {
                        mInfo2.append("$file\n")
                    }
                } else {
                    mInfo2.append("Can not read usb storage")
                }

                textView_info2.text = mInfo2
            }
            FactoryFeature.CLEAR_INFO -> {
                textView_info1.text = ""
                textView_info2.text = ""
                mInfo1.setLength(0)
                mInfo2.setLength(0)
            }
        }
    }

    override fun onSuccess(it: Any?) {
        mData = it as Config?
    }

    override fun onError(t: Throwable?) {
        Log.e(TAG, "Error = ${t?.message}")
    }

    override fun onProgress(b: Boolean) {
    }

    private fun downloadFileProgress(it: Boolean?) {
        mInfo1.append("下載中 = $it\n")
        textView_info1.text = mInfo1
        if (it == true) {
            if (mDownloadDialog == null)
                mDownloadDialog = android.app.AlertDialog.Builder(this)
                        .setTitle("下載中")
                        .setView(R.layout.view_progress)
                        .setCancelable(false)
                        .create()

            mDownloadDialog?.show()
        } else {
            Log.d("neo", "cancel")
        }
    }

    private fun downloadFileFailed(it: Throwable?) {
        mDownloadDialog?.cancel()
        mInfo1.append("下載失敗：$it\n")
        textView_info1.text = mInfo1
        if (mDownloadDialog == null)
            mDownloadDialog = android.app.AlertDialog.Builder(this)
                    .setTitle("下載失敗")
                    .setMessage(it?.message)
                    .setView(R.layout.view_progress)
                    .setCancelable(false)
                    .create()

        mDownloadDialog?.show()
    }

    private fun downloadFileSuccess(it: String) {
        mInfo1.append("下載成功：$it\n")
        textView_info1.text = mInfo1
        mDownloadDialog?.cancel()

        if (MiscUtils.installApk(this, it, FileUtils.getInsideHotel()?.path!!)) {
            mInfo1.append("Find apk：$it\n")
        } else {
            mInfo1.append("Can not find apk：$it\n")
        }
        textView_info1.text = mInfo1
    }

    override fun onBackPressed() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }
}