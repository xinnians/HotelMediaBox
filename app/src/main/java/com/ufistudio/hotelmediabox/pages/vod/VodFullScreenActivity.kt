package com.ufistudio.hotelmediabox.pages.vod

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.repository.data.NoteButton

class VodFullScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vod_full_screen)

        val b: NoteButton = intent.extras.get("bottom_note") as NoteButton
        Log.d("neo", "${b.note?.home}")
    }

    override fun onStart() {
        super.onStart()
    }
}
