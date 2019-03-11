package com.ufistudio.hotelmediabox.pages.roomService.template

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import com.ufistudio.hotelmediabox.R


open class TemplateType2PagerAdapter : PagerAdapter {

    private var mListViews: ArrayList<View> = ArrayList<View>()
    private lateinit var mData: ArrayList<Array<String>>

    constructor(context: Context, data: ArrayList<Array<String>>) {
        mData = data
        for (i in 1..data.size) {
            if (i % 3 == 0) {
                Log.d("neo", "QQ = $i")
                val mInflater = LayoutInflater.from(context)
                val v1 = mInflater.inflate(R.layout.item_room_service_type2, null)
                mListViews.add(v1)
            }
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
        val d = ArrayList<Array<String>>()
        Log.d("neo", "instantiateItem")
        d.add(mData[0 + position])
        d.add(mData[1 + position])
        d.add(mData[2 + position])
        val adapter: TemplateType2RecyclerViewAdapter = TemplateType2RecyclerViewAdapter(d)

        val recyclerView: RecyclerView = view.findViewById<RecyclerView>(R.id.recyclerview_content)
        recyclerView.layoutManager = LinearLayoutManager(container.context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }
}