package com.ufistudio.hotelmediabox.pages.roomService.template

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.repository.data.RoomServiceCategories
import com.ufistudio.hotelmediabox.repository.data.RoomServiceContent
import com.ufistudio.hotelmediabox.utils.FileUtils


open class TemplateType1PagerAdapter(context: Context, data: RoomServiceCategories) : PagerAdapter() {

    private var mListViews: ArrayList<View> = ArrayList<View>()
    private lateinit var mData: ArrayList<RoomServiceContent>

    init {
        for (item in data.contents) {
            mData = data.contents
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
        val item = mData[position]
        view.findViewById<TextView>(R.id.text_title).text = item.title
        view.findViewById<TextView>(R.id.text_content).text = item.content
        view.findViewById<TextView>(R.id.text_current_page).text = (position + 1).toString()
        view.findViewById<TextView>(R.id.text_total_page).text = String.format("/%d", mListViews.size)
        Glide.with(view.context)
                .load(FileUtils.getFileFromStorage(item.file_name))
                .skipMemoryCache(true)
                .into(view.findViewById<ImageView>(R.id.image_content))


        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }
}