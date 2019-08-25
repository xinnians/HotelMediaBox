package com.ufistudio.hotelmediabox.pages.memo

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import com.ufistudio.hotelmediabox.AppInjector
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Cache
import com.ufistudio.hotelmediabox.constants.Cache.Memos
import com.ufistudio.hotelmediabox.interfaces.ViewModelsCallback
import com.ufistudio.hotelmediabox.pages.MainActivity
import com.ufistudio.hotelmediabox.pages.base.PaneViewActivity
import com.ufistudio.hotelmediabox.repository.data.NoteButton
import kotlinx.android.synthetic.main.activity_momo.*
import kotlinx.android.synthetic.main.activity_momo.dateView
import kotlinx.android.synthetic.main.view_bottom_ok.*

class MemoActivity : PaneViewActivity(), ViewModelsCallback {

    private lateinit var mViewModel: MemoViewModel

    private var mMemoIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_momo)

        mViewModel = AppInjector.obtainViewModel(this)

        mViewModel.initNoteButtonProgress.observe(this, Observer { onProgress() })
        mViewModel.initNoteButtonSuccess.observe(this, Observer { onSuccess(it) })
        mViewModel.initNoteButtonError.observe(this, Observer { onError(it) })

        setMemo(mMemoIndex)

    }

    private fun setMemo(index: Int) {
        val content = Memos[index].content
        tv_content.text = content
    }

    override fun onStop() {
        super.onStop()

        dateView.stopRefreshTimer()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when(keyCode){
            KeyEvent.KEYCODE_DPAD_CENTER -> {

                if(mMemoIndex == Memos.size-1){
                    val intent: Intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    mMemoIndex++
                    setMemo(mMemoIndex)
                }

                return true
            }
            else -> {
                return true
            }
        }
    }

    override fun onSuccess(result: Any?) {
        result?.let {
            when (it) {
                is NoteButton -> {
                    textView_ok.text = it.note?.next
                }
                else -> {
                    onError(Throwable("OnSuccess response is null"))
                }
            }
        }
    }

    override fun onError(t: Throwable?) {
    }

    override fun onProgress(b: Boolean) {
    }
}