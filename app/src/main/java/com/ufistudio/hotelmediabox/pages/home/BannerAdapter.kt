package com.ufistudio.hotelmediabox.pages.home

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class BannerAdapter : PagerAdapter {

    private var mList: List<ImageView>? = null

    constructor(list: List<ImageView>) {
        mList = list
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(mList?.get(position))
        return mList?.get(position) ?: 0
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(mList?.get(position))
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return mList?.size?:0
    }
}