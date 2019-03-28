package com.ufistudio.hotelmediabox.pages.factory

import android.arch.lifecycle.Observer
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.interfaces.OnItemClickListener
import com.ufistudio.hotelmediabox.interfaces.OnSaveFileStatusListener
import com.ufistudio.hotelmediabox.interfaces.ViewModelsCallback
import com.ufistudio.hotelmediabox.pages.home.HomeFragment
import com.ufistudio.hotelmediabox.pages.welcome.WelcomeActivity
import com.ufistudio.hotelmediabox.repository.data.Config
import com.ufistudio.hotelmediabox.repository.data.Welcome
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.utils.MiscUtils
import kotlinx.android.synthetic.main.activity_factory.*
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException

class FactoryActivity : AppCompatActivity(), OnItemClickListener, OnSaveFileStatusListener, ViewModelsCallback {
    private val mAdapter: FactoryAdapter = FactoryAdapter()
    private lateinit var mViewModel: FactoryViewModel
    private var mDownloadDialog: android.app.AlertDialog? = null
    private var mData: Config? = null

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
        recyclerview_content.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerview_content.adapter = mAdapter
        mAdapter.setOnClickListener(this)
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
                Toast.makeText(this, "暫未完成", Toast.LENGTH_SHORT).show()
//                if (TextUtils.isEmpty(mData?.config?.upgradeUrl!!)) {
//                    Toast.makeText(this, "無法解析url", Toast.LENGTH_SHORT).show()
//                    return
//                }
//                AlertDialog.Builder(this)
//                        .setTitle("是否下載更新欓")
//                        .setPositiveButton(android.R.string.ok, object : DialogInterface.OnClickListener {
//                            override fun onClick(dialog: DialogInterface?, which: Int) {
//                                mViewModel.downloadFileWithUrl(mData?.config?.upgradeUrl!!)
//                            }
//                        })
//                        .setNegativeButton(android.R.string.cancel, null)
//                        .setCancelable(false)
//                        .create()
//                        .show()

            }
            FactoryFeature.CHECK_UPGRADE_FROM_USB -> {
                if (!MiscUtils.installApk(this, "hotelbox.apk")) {
                    Toast.makeText(this, "找不到檔案", Toast.LENGTH_SHORT).show()
                }
            }
            FactoryFeature.EXPORT_JSON_FILE -> {
                exportFile()
            }
            FactoryFeature.IMPORT_JSON_FILE -> {
                importFile()
            }
            FactoryFeature.OPEN_SETTING -> {
                MiscUtils.openSetting(this)
            }
            FactoryFeature.OPEN_DEFAULT_LAUNCHER -> {
//                startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER))
                val startMain: Intent = Intent(Intent.ACTION_MAIN)
                startMain.addCategory(Intent.CATEGORY_HOME)
                startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(startMain);
            }
        }
    }

    override fun savingFileStart() {
        Log.d(TAG, "savingFileStart")
    }

    override fun savingFile(progress: Long) {
        Log.d(TAG, "progress =  $progress")
    }

    override fun savingFileEnd() {
    }

    override fun savingFileError(e: IOException) {
        Log.d(TAG, "savingFileError + $e")
        mDownloadDialog?.cancel()
    }

    override fun savingFileSuccess(path: String, name: String) {
        mDownloadDialog?.cancel()
        MiscUtils.installApk(this, name)
    }

    override fun onSuccess(it: Any?) {
        mData = it as Config?
    }

    override fun onError(t: Throwable?) {
        Log.e(TAG, "Error = ${t?.message}")
    }

    override fun onProgress(b: Boolean) {
    }

    /**
     * Import hotel資料
     */
    private fun importFile() {
        var usbDir = ""
        for (dir in File("/storage/").list()) {
            if (!TextUtils.equals(dir, "self") && !TextUtils.equals(dir, "emulated")) {
                usbDir = dir
                break
            }
        }

        try {
            for (item in File("/mnt/media_rw/$usbDir").list()) {
                Log.d("importFile", "item = $item")
                FileUtils.copyFile(
                        File("/mnt/media_rw/$usbDir/$item"),
                        File("${Environment.getExternalStorageDirectory().path}/hotel/$item")
                )
            }
        } catch (e: IOException) {
            Log.e("export", "Error = $e")
        } catch (e: NullPointerException) {
            Log.e("export", "Error = $e")
        }

        Toast.makeText(this, "import Finish", Toast.LENGTH_SHORT).show()
    }

    /**
     * 輸出hotel資料
     */
    private fun exportFile() {
        var usbDir = ""
        for (dir in File("/storage/").list()) {
            if (!TextUtils.equals(dir, "self") && !TextUtils.equals(dir, "emulated")) {
                usbDir = dir
                break
            }
        }

        try {

            for (item in File("${Environment.getExternalStorageDirectory().path}/hotel").list()) {
                Log.d("exportFile", "item = $item")

                FileUtils.copyFile(
                        File("${Environment.getExternalStorageDirectory().path}/hotel/$item"),
                        File("/mnt/media_rw/$usbDir/$item")
                )
            }
        } catch (e: IOException) {
            Log.e("export", "Error = $e")
        } catch (e: NullPointerException) {
            Log.e("export", "Error = $e")
        }


        Toast.makeText(this, "export Finish", Toast.LENGTH_SHORT).show()
    }

    private fun downloadFileProgress(it: Boolean?) {
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
        if (mDownloadDialog == null)
            mDownloadDialog = android.app.AlertDialog.Builder(this)
                    .setTitle("下載失敗")
                    .setMessage(it?.message)
                    .setView(R.layout.view_progress)
                    .setCancelable(false)
                    .create()

        mDownloadDialog?.show()
    }

    private fun downloadFileSuccess(it: ResponseBody) {
        FileUtils.writeResponseBodyToDisk(it, "hotelbox.apk", listener = this)
    }

    override fun onBackPressed() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }
}