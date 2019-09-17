package com.ufistudio.hotelmediabox.pages.setting.content

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import android.view.KeyEvent
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.interfaces.OnFragmentKeyListener
import com.ufistudio.hotelmediabox.interfaces.ViewModelsCallback
import com.ufistudio.hotelmediabox.repository.data.*
import com.ufistudio.hotelmediabox.utils.FileUtils
import com.ufistudio.hotelmediabox.utils.MiscUtils
import com.ufistudio.hotelmediabox.utils.TAG_DEFAULT_LOCAL_PATH
import kotlinx.android.synthetic.main.view_langauge_setting.*
import java.io.File

class LanguageFragment : InteractionView<OnPageInteractionListener.Primary>(), OnFragmentKeyListener, ViewModelsCallback {
    private lateinit var mViewModel: LanguageViewModel

    private var mData: SettingContent? = null
    private var mConfigData: Config? = null
    private var mIsRendered: Boolean = false //判斷塞資料了沒
    private var mCurrentLanguageCode: String = "en"
    private var mCurrentIndex: Int = -1
    private val mGson: Gson = GsonBuilder().disableHtmlEscaping().create()
    private var mSwitchRange: Int = 5


    companion object {
        fun newInstance(): LanguageFragment = LanguageFragment()
        private val TAG = LanguageFragment::class.simpleName
        private const val TAG_CURRENT_INDEX = "com.ufistudio.hotelmediabox.pages.setting.content.current_index"
        private const val TAG_CONFIG = "com.ufistudio.hotelmediabox.pages.setting.content.config"
        private const val TAG_SETTING = "com.ufistudio.hotelmediabox.pages.setting.content.setting"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("neo ", "onCreate ")
        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initConfigServiceProgress.observe(this, Observer {
            onProgress(it!!)
        })
        mViewModel.initConfigServiceSuccess.observe(this, Observer {
            onSuccess(it)
        })
        mViewModel.initConfigServiceError.observe(this, Observer {
            onError(it)
        })

        mData = arguments?.getParcelable<SettingContent>(Page.ARG_BUNDLE)!!
        if (savedInstanceState != null) {
            mData = savedInstanceState.getParcelable<SettingContent>(TAG_SETTING)
            mConfigData = savedInstanceState.getParcelable<Config>(TAG_CONFIG)
            mCurrentIndex = savedInstanceState.getInt(TAG_CURRENT_INDEX)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.view_langauge_setting, container, false)
    }

    override fun onStart() {
        super.onStart()
        renderView()
        getInteractionListener().setOnKeyListener(this)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        if (arguments != null)
            saveState(arguments!!)
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onKeyPress(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                mConfigData?.config?.language = mCurrentLanguageCode
                FileUtils.writeToFile(File("/data/$TAG_DEFAULT_LOCAL_PATH", "box_config.json"), mGson.toJson(mConfigData))
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (mCurrentIndex - 1 < 0) {
                    mCurrentIndex = mData?.content?.size!! - 1
                } else {
                    mCurrentIndex -= 1
                }
                scrollLanguage()
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (mCurrentIndex + 1 >= mData?.content?.size!!) {
                    mCurrentIndex = 0
                } else {
                    mCurrentIndex += 1
                }
                scrollLanguage()
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (getInteractionListener().getFragmentCacheData() as Boolean) {
                    textView3.setTextColor(ContextCompat.getColor(context!!, R.color.homeIconFrameFocused))
                    textView3.background = ContextCompat.getDrawable(context!!, R.drawable.language_frame_focus)
                }
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                mIsRendered = false
                textView3.setTextColor(ContextCompat.getColor(context!!, R.color.homeIconFrame))
                textView3.background = ContextCompat.getDrawable(context!!, R.drawable.language_frame_pass)
                return true
            }
        }
        return false
    }

    override fun onSuccess(it: Any?) {
        mConfigData = it as Config?
        renderView()
    }

    override fun onError(t: Throwable?) {
        Log.d(TAG, "error = $t")
    }

    override fun onProgress(b: Boolean) {
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveState(outState)
    }

    private fun saveState(bundle: Bundle) {
        bundle.putInt(TAG_CURRENT_INDEX, mCurrentIndex)
        bundle.putParcelable(TAG_CONFIG, mConfigData)
        bundle.putParcelable(TAG_SETTING, mData)
        mIsRendered = false
    }

    /**
     * 塞資料
     */
    private fun renderView() {
        Log.e(TAG,"[renderView]")
        if (!mIsRendered) {
            if (mData?.content != null && mConfigData != null) {
                mIsRendered = true
                if (mData?.content != null) {
                    text_content_title.text = mData?.content_title
                    if (mCurrentIndex == -1)
                        renderLanguage(mConfigData?.config?.language!!)
                    else
                        scrollLanguage()
                }
            }
        }
    }

    /**
     * First render language list
     * 比對config的當前語言做第一次的設置
     * @languageCode: language code，第一次進入應該會放Config.json內的language code
     */
    private fun renderLanguage(languageCode: String) {
        Log.e(TAG,"[renderLanguage] languageCode:$languageCode")
        if (mData?.content != null) {
            for (i in 0 until mData?.content!!.size) {
                if (TextUtils.equals(languageCode, mData?.content!![i].code)) {
                    mCurrentLanguageCode = mData?.content!![i].code
                    mCurrentIndex = i

                    if(mData?.content?.size ?: 0 in 3..4){
                        mSwitchRange = 3
                    }else if(mData?.content?.size ?: 0 < 3){
                        mSwitchRange = 1
                    }

                    Log.e(TAG,"[renderLanguage] mSwitchRange:$mSwitchRange")

                    when(mSwitchRange){
                        5 -> {
                            textView1.text = mData?.content!![if (mCurrentIndex - 2 >= 0) mCurrentIndex - 2 else mData?.content!!.size + mCurrentIndex - 2].title
                            textView2.text = mData?.content!![if (mCurrentIndex - 1 >= 0) mCurrentIndex - 1 else mData?.content!!.size + mCurrentIndex - 1].title
                            textView3.text = mData?.content!![mCurrentIndex].title
                            textView4.text = mData?.content!![if (mCurrentIndex + 1 < mData?.content!!.size) mCurrentIndex + 1 else (mCurrentIndex + 1 - mData?.content!!.size)].title
                            textView5.text = mData?.content!![if (mCurrentIndex + 2 < mData?.content!!.size) mCurrentIndex + 2 else (mCurrentIndex + 2 - mData?.content!!.size)].title
                        }
                        3 -> {
                            textView1.visibility = View.GONE
                            textView5.visibility = View.GONE

                            textView2.text = mData?.content!![if (mCurrentIndex - 1 >= 0) mCurrentIndex - 1 else mData?.content!!.size + mCurrentIndex - 1].title
                            textView3.text = mData?.content!![mCurrentIndex].title
                            textView4.text = mData?.content!![if (mCurrentIndex + 1 < mData?.content!!.size) mCurrentIndex + 1 else (mCurrentIndex + 1 - mData?.content!!.size)].title
                        }
                        1 -> {
                            textView1.visibility = View.GONE
                            textView2.visibility = View.GONE
                            textView5.visibility = View.GONE
                            textView4.visibility = View.GONE

                            textView3.text = mData?.content!![mCurrentIndex].title
                        }
                    }

                    break
                }
            }
        }
    }


    /**
     * 滑動Language List
     */
    private fun scrollLanguage() {
        Log.e(TAG,"[scrollLanguage] mSwitchRange:$mSwitchRange")
        if (mData?.content != null) {
            when(mSwitchRange){
                5 -> {
                    textView1.text = mData?.content!![if (mCurrentIndex - 2 >= 0) mCurrentIndex - 2 else mData?.content!!.size + mCurrentIndex - 2].title
                    textView2.text = mData?.content!![if (mCurrentIndex - 1 >= 0) mCurrentIndex - 1 else mData?.content!!.size + mCurrentIndex - 1].title
                    textView3.text = mData?.content!![mCurrentIndex].title
                    mCurrentLanguageCode = mData?.content!![mCurrentIndex].code
                    textView4.text = mData?.content!![if (mCurrentIndex + 1 < mData?.content!!.size) mCurrentIndex + 1 else (mCurrentIndex + 1 - mData?.content!!.size)].title
                    textView5.text = mData?.content!![if (mCurrentIndex + 2 < mData?.content!!.size) mCurrentIndex + 2 else (mCurrentIndex + 2 - mData?.content!!.size)].title
                }
                3 -> {
                    textView1.visibility = View.GONE
                    textView5.visibility = View.GONE

                    textView2.text = mData?.content!![if (mCurrentIndex - 1 >= 0) mCurrentIndex - 1 else mData?.content!!.size + mCurrentIndex - 1].title
                    textView3.text = mData?.content!![mCurrentIndex].title
                    mCurrentLanguageCode = mData?.content!![mCurrentIndex].code
                    textView4.text = mData?.content!![if (mCurrentIndex + 1 < mData?.content!!.size) mCurrentIndex + 1 else (mCurrentIndex + 1 - mData?.content!!.size)].title
                }
                1 -> {
                    textView1.visibility = View.GONE
                    textView2.visibility = View.GONE
                    textView5.visibility = View.GONE
                    textView4.visibility = View.GONE

                    textView3.text = mData?.content!![mCurrentIndex].title
                    mCurrentLanguageCode = mData?.content!![mCurrentIndex].code
                }
            }
        }
    }
}