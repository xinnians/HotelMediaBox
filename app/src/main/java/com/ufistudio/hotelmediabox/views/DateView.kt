package com.ufistudio.hotelmediabox.views

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.SystemClock
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Cache.IsMessageHintShow
import com.ufistudio.hotelmediabox.constants.Cache.IsMessageUpdate
import com.ufistudio.hotelmediabox.constants.Key.IS_TIME_SET_SUCCESS
import com.ufistudio.hotelmediabox.repository.data.Config
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
    private var mMessageDisposable: Disposable? = null
    private var mCheckMessageDisposable: Disposable? = null
    private var mIsTimeSet: Boolean = false
    private var mTextView: TextView? = null
    private var mTvMessage: TextView? = null
    private var mLayoutMessage: ConstraintLayout? = null

    val mGson = Gson()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_date, this)
        getTimeFormat()

        mTvMessage = this.findViewById(R.id.tv_message) as TextView
        mLayoutMessage = this.findViewById(R.id.layout_message) as ConstraintLayout
    }

    private fun getTime() {
        mDateDisposable = Observable.interval(1, 30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.d(TAG,"Update Time = ${System.currentTimeMillis()}")
//                    if(this.visibility == View.VISIBLE)
                    textView_date.text = mDf.format(System.currentTimeMillis())
                    checkTimeSet()
                }
        mCheckMessageDisposable = Observable.interval(5, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if(IsMessageUpdate){
                    showMessageHint()
                    IsMessageUpdate = false
                }
            }
    }

    /**
     * 停止刷新時間
     */
    fun stopRefreshTimer() {
        mDateDisposable?.dispose()
        mMessageDisposable?.dispose()
        mCheckMessageDisposable?.dispose()
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
            mDateDisposable = Single.fromCallable { mGson.fromJson(MiscUtils.getJsonFromStorage("box_config.json"), Config::class.java) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (!TextUtils.isEmpty(it.config.timeFormat))
                            setDateFormat(it.config.timeFormat)
                        else {
                            Log.e(TAG, "讀不到format")
                            setDefaultFormat()
                        }
                        mTextView = this.findViewById(R.id.textView_date) as TextView
                        checkTimeSet()

                    }, {
                        Log.e(TAG, "load file error $it")
                        setDefaultFormat()
                    })
        } else {
            setDefaultFormat()
        }
    }

    fun checkTimeSet(){
        if(!mIsTimeSet){
            val isTimeSet = context.getSharedPreferences("HotelBoxData", Context.MODE_PRIVATE).getBoolean(IS_TIME_SET_SUCCESS,false)
            Log.e(TAG,"[getTimeFormat] is time set:$isTimeSet")
            mIsTimeSet = isTimeSet
        }

        mTextView?.visibility = if(mIsTimeSet) View.VISIBLE else View.INVISIBLE
    }

    fun showMessageHint(){
        IsMessageHintShow = true
        mLayoutMessage?.visibility = View.VISIBLE

        mMessageDisposable = Observable.timer(10,TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Log.e(TAG,"Update Time for message = $it")
//                if(it == 5L){
                    mLayoutMessage?.visibility = View.INVISIBLE
                IsMessageHintShow = false
                    mDateDisposable?.dispose()
//                }
            }
    }

    fun setVisiable(isVisiable: Boolean){
        this.visibility = if(isVisiable) View.VISIBLE else View.INVISIBLE

        textView_date.text = textView_date.text
    }
}