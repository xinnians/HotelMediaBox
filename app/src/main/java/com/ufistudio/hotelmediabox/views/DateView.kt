package com.ufistudio.hotelmediabox.views

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.repository.data.Config
import com.ufistudio.hotelmediabox.repository.data.ConfigContent
import com.ufistudio.hotelmediabox.repository.data.TimeInfo
import com.ufistudio.hotelmediabox.repository.remote.ApiClient
import com.ufistudio.hotelmediabox.utils.MiscUtils
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.view_date.view.*
import java.util.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit


private const val TAG_DEFAULT_FORMAT = "K:mma  EEE, dd MMM yyyy"
private val TAG = DateView::class.java.simpleName

class DateView : ConstraintLayout {
    private var mDf: DateFormat = SimpleDateFormat(TAG_DEFAULT_FORMAT, Locale.getDefault())
    private var mDateDisposable: Disposable? = null

    val mGson = Gson()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_date, this)
        getTimeFormat()
    }

    private fun getTime() {
        mDateDisposable = Observable.interval(1, 20, TimeUnit.SECONDS)
                .flatMapSingle { getTimeFromServer() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    textView_date.text = mDf.format(Calendar.getInstance().time)
                }
    }

    /**
     * 停止刷新時間
     */
    fun stopRefreshTimer() {
        mDateDisposable?.dispose()
    }

    /**
     * 設置新的Time format
     * @format: Android 用的時間格式
     */
    fun setDateFormat(format: String) {
        mDf = SimpleDateFormat(format, Locale.getDefault())
        stopRefreshTimer()
        getTime()
    }

    /**
     * 設置Default Time Format
     */
    fun setDefaultFormat() {
        mDf = SimpleDateFormat(TAG_DEFAULT_FORMAT, Locale.getDefault())
        stopRefreshTimer()
        getTime()
    }

    /**
     * 取得時間格式
     */
    fun getTimeFormat() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val jsonObject = mGson.fromJson(MiscUtils.getJsonFromStorage("box_config.json"), Config::class.java)
            if (jsonObject != null) {
                mDateDisposable = Single.just(jsonObject)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            if (!TextUtils.isEmpty(it.config.timeFormat))
                                setDateFormat(it.config.timeFormat)
                            else {
                                Log.e(TAG, "讀不到format")
                                setDefaultFormat()
                            }
                        }, {
                            Log.e(TAG, "load file error $it")
                            setDefaultFormat()
                        })
            } else {
                setDefaultFormat()
                Log.e(TAG, "jsonObject is null")
            }
        } else {
            setDefaultFormat()
        }
    }

    /**
     * 從Server 讀取時間
     */
    private fun getTimeFromServer(): Single<TimeInfo> {
        var jsonObject = mGson.fromJson(MiscUtils.getJsonFromStorage("box_config.json"), Config::class.java)
        if (jsonObject == null) {
            jsonObject = Config(ConfigContent(defaultServerIp = "10.0.0.1"))
        }

        return Single.just(jsonObject)
                .flatMap {
                    ApiClient.getInstance()!!.getTime("http:${it.config.defaultServerIp}/api/device/time")
                }
    }
}