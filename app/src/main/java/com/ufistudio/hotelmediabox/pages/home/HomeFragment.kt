package com.ufistudio.hotelmediabox.pages.home

import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.TemplateViewModel
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import kotlinx.android.synthetic.main.fragment_home.*
import java.net.URI

class HomeFragment : InteractionView<OnPageInteractionListener.Primary>() {

    private lateinit var mViewModel: TemplateViewModel
    private var mAdapter = FunctionsAdapter()

    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
        private val TAG = HomeFragment::class.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onStart() {
        super.onStart()

        list_functions.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        list_functions.adapter = mAdapter

//        videoView.setMediaController(MediaController(context))
//        videoView.setVideoURI(Uri.parse("http://194.88.107.101:6204/Arnaud/123/9019"))
    }
}