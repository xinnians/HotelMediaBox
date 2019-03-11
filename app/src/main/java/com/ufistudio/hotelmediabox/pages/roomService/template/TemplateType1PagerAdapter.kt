package com.ufistudio.hotelmediabox.pages.roomService.template

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.ufistudio.hotelmediabox.R


open class TemplateType1PagerAdapter : PagerAdapter {

    private var mListViews: ArrayList<View> = ArrayList<View>()
    private lateinit var mData: ArrayList<Array<String>>

    constructor(context: Context, data: ArrayList<Array<String>>) {
        for (item in data) {
            mData = data
            val mInflater = LayoutInflater.from(context)
            val v1 = mInflater.inflate(R.layout.item_room_service_type1, null)
            mListViews.add(v1)
        }
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return mListViews.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View = mListViews[position]
        view.findViewById<TextView>(R.id.text_title).text = mData[position][0]
        view.findViewById<TextView>(R.id.text_content).text = mData[position][1]
        view.findViewById<TextView>(R.id.text_current_page).text = (position + 1).toString()
        view.findViewById<TextView>(R.id.text_total_page).text = String.format("/%d", mListViews.size)

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }
}