package com.ufistudio.hotelmediabox.pages.nearby.template

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.repository.data.NearbyMeContent
import com.ufistudio.hotelmediabox.utils.FileUtils


open class FoodPagerAdapter(context: Context, data: ArrayList<NearbyMeContent>) : PagerAdapter() {

    private var mListViews: ArrayList<View> = ArrayList<View>()
    private lateinit var mData: ArrayList<NearbyMeContent>
    private var mTextViewContent: TextView? = null

    init {
        for (item in data) {
            mData = data
            val mInflater = LayoutInflater.from(context)
            val v1 = mInflater.inflate(R.layout.item_nearby_me_food, null)
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
        mTextViewContent = view.findViewById<TextView>(R.id.text_content)
        mTextViewContent?.movementMethod = ScrollingMovementMethod()
        mTextViewContent?.text = item.content
        view.findViewById<TextView>(R.id.text_current_page).text = (position + 1).toString()
        view.findViewById<TextView>(R.id.text_total_page).text = String.format("/%d", mListViews.size)
        Glide.with(view.context)
                .load(FileUtils.getFileFromStorage(item.file_name))
                .into(view.findViewById<ImageView>(R.id.image_content))


        container.addView(view)
        Log.d("neo","position => $position : ${mTextViewContent?.isFocused} ")
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

    fun clearFocus() {
        mTextViewContent?.clearFocus()
    }
}