package com.ufistudio.hotelmediabox.pages

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.base.InteractionView
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.pages.roomService.template.TemplateType2PagerAdapter
import kotlinx.android.synthetic.main.fragment_room_service_content.*
import kotlinx.android.synthetic.main.framgnet_room_service.*

class TemplateType2Fragment : InteractionView<OnPageInteractionListener.Primary>() {

    private lateinit var mViewModel: TemplateType2ViewModel
    private val fakeData: ArrayList<Array<String>> = ArrayList<Array<String>>()


    companion object {
        fun newInstance(): TemplateType2Fragment = TemplateType2Fragment()
        private val TAG = TemplateType2Fragment::class.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_room_service_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fakeData.add(arrayOf("AAA", "Cake", "101", "蛋糕好好吃Ａ"))
        fakeData.add(arrayOf("BBB", "CAKE", "102", "蛋糕好好吃b"))
        fakeData.add(arrayOf("CCC", "CAKE", "103", "蛋糕好好吃c"))
        fakeData.add(arrayOf("DDD", "CAKE", "104", "蛋糕好好吃d"))
        fakeData.add(arrayOf("EEE", "CAKE", "105", "蛋糕好好吃e"))
        fakeData.add(arrayOf("FFF", "CAKE", "106", "蛋糕好好吃f"))
        fakeData.add(arrayOf("GGG", "CAKE", "107", "蛋糕好好吃g"))
        fakeData.add(arrayOf("HHH", "CAKE", "108", "蛋糕好好吃h"))
        fakeData.add(arrayOf("III", "CAKE", "109", "蛋糕好好吃j"))

    }

    override fun onStart() {
        super.onStart()
        view_pager_content.adapter = TemplateType2PagerAdapter(context!!, fakeData)
    }

    override fun onFragmentKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (sideView.visibility == View.VISIBLE) {
                }
                return false
            }
            KeyEvent.KEYCODE_BACK -> {
                if (sideView.visibility == View.GONE) {
                    return true
                }
            }
        }
        return super.onFragmentKeyDown(keyCode, event)
    }
}