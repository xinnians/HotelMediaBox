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
import com.ufistudio.hotelmediabox.repository.data.RoomServiceCategories
import com.ufistudio.hotelmediabox.repository.data.RoomServiceContent


open class TemplateType2PagerAdapter : PagerAdapter {

    private var mListViews: ArrayList<View> = ArrayList<View>()
    private lateinit var mData: ArrayList<RoomServiceContent>

    constructor(context: Context, data: RoomServiceCategories) {
        mData = data.contents
        for (i in 1..data.contents.size) {
            if (i % 3 == 0) {
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
        val listData = ArrayList<RoomServiceContent>()
        Log.d("neo", "instantiateItem")
        for (i in 0..2) {
            if (mData.size >= i + position * 3)
                listData.add(mData[i + position * 3])
        }
        val adapter: TemplateType2RecyclerViewAdapter = TemplateType2RecyclerViewAdapter(listData)

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