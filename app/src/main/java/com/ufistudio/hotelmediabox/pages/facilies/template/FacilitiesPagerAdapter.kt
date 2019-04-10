package com.ufistudio.hotelmediabox.pages.facilies.template

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.repository.data.HotelFacilitiesCategories
import com.ufistudio.hotelmediabox.repository.data.HotelFacilitiesContent
import com.ufistudio.hotelmediabox.utils.FileUtils

private const val TAG_TYPE_1 = 1
private const val TAG_TYPE_2 = 2
private const val TAG_TYPE_3 = 3

class HotelFacilitiesPagerAdapter(context: Context, data: HotelFacilitiesCategories) : PagerAdapter() {

    private var mListViews: ArrayList<View> = ArrayList<View>()
    private var mData: ArrayList<HotelFacilitiesContent> = data.contents
    private var mViewType = TAG_TYPE_1

    init {
        for (item in mData) {
            val mInflater = LayoutInflater.from(context)
            var layout: Int = 0
            mViewType = data.content_type
            when (mViewType) {
                TAG_TYPE_1 -> {
                    layout = R.layout.item_hotel_facilities_type1
                }
                TAG_TYPE_2 -> {
                    layout = R.layout.item_hotel_facilities_type2
                }
                TAG_TYPE_3 -> {
                    layout = R.layout.item_hotel_facilities_type3
                }

            }
            val v1 = mInflater.inflate(layout, null)
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
        when (mViewType) {
            TAG_TYPE_1 -> {
                view.findViewById<TextView>(R.id.text_total_page).text = String.format("/%d", mData.size)
                view.findViewById<TextView>(R.id.text_current_page).text = (position + 1).toString()
                Glide.with(view.context)
                        .load(FileUtils.getFileFromStorage(item.file_name))
                        .skipMemoryCache(true)
                        .into(view.findViewById<ImageView>(R.id.image_photo))
            }
            TAG_TYPE_2 -> {
                view.findViewById<TextView>(R.id.text_total_page).text = String.format("/%d", mData.size)
                view.findViewById<TextView>(R.id.text_current_page).text = (position + 1).toString()
                view.findViewById<TextView>(R.id.text_title).text = item.title
                view.findViewById<TextView>(R.id.text_description).text = item.content
                Glide.with(view.context)
                        .load(FileUtils.getFileFromStorage(item.file_name))
                        .skipMemoryCache(true)
                        .into(view.findViewById<ImageView>(R.id.image_photo))
            }
            TAG_TYPE_3 -> {
                view.findViewById<TextView>(R.id.text_description).text = item.content
                Glide.with(view.context)
                        .load(FileUtils.getFileFromStorage(item.file_name))
                        .skipMemoryCache(true)
                        .into(view.findViewById<ImageView>(R.id.image_photo))
            }
        }
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }
}