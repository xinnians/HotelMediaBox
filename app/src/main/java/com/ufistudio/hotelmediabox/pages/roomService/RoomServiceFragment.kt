package com.ufistudio.hotelmediabox.pages

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.pages.roomService.RoomServiceAdapter
import kotlinx.android.synthetic.main.framgnet_room_service.*

class RoomServiceFragment : InteractionView<OnPageInteractionListener.Primary>() {

    private lateinit var mViewModel: RoomServiceViewModel
    private  var mAdapter: RoomServiceAdapter=RoomServiceAdapter()

    companion object {
        fun newInstance(): RoomServiceFragment = RoomServiceFragment()
        private val TAG = RoomServiceFragment::class.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.framgnet_room_service, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displaySideView(false)
    }

    override fun onStart() {
        super.onStart()
        recyclerView_service.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView_service.adapter = mAdapter

    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (sideView.visibility == View.VISIBLE) {
                    displaySideView(false)
                }
                return false
            }
            KeyEvent.KEYCODE_BACK -> {
                if (sideView.visibility == View.GONE) {
                    displaySideView(true)
                    return true
                }
            }
        }
        return super.onFragmentKeyDown(keyCode, event)
    }

    private fun displaySideView(show:Boolean){
        if(show){
            sideView.visibility = View.VISIBLE
            layout_back.visibility = View.GONE
            view_line.visibility = View.VISIBLE
            recyclerView_service.clearFocus()
        }else{
            sideView.visibility = View.GONE
            layout_back.visibility = View.VISIBLE
            view_line.visibility = View.GONE
            recyclerView_service.requestFocus()
        }
    }

}