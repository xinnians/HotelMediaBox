package com.ufistudio.hotelmediabox.pages.home

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.repository.data.HomeIcons
import kotlinx.android.synthetic.main.item_home_functions.view.*

class FunctionsAdapter : RecyclerView.Adapter<FunctionsAdapter.ViewHolder>() {

    private val TAG_ENABLE: Int = 1
    private val TAG_DISABLE: Int = 0

    private lateinit var mContext: Context
    private var mItem: ArrayList<HomeFeatureEnum> = ArrayList()

    interface OnItemClickListener {
        fun onClick(view: View)
    }

    private var mListener: OnItemClickListener? = null

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): FunctionsAdapter.ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_home_functions, p0, false) as View
        mContext = p0.context
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mItem.size
    }

    override fun onBindViewHolder(p0: FunctionsAdapter.ViewHolder, p1: Int) {
        p0.itemView.text_title.setText(mItem[p1].title)

        if (p0.itemView.image_icon.isFocused) {
            p0.itemView.text_title.setTextColor(ContextCompat.getColor(mContext, R.color.homeIconFrameFocused))
            p0.itemView.image_icon.background = ContextCompat.getDrawable(mContext, mItem[p1].focusedIcon)
        } else {
            p0.itemView.text_title.setTextColor(ContextCompat.getColor(mContext, android.R.color.white))
            p0.itemView.image_icon.background = ContextCompat.getDrawable(mContext, mItem[p1].icon)
        }
        p0.itemView.tag = mItem[p1].page
        p0.itemView.setOnClickListener {
            mListener?.onClick(it)
        }
    }

    fun setData(data: ArrayList<HomeIcons>?) {
        if (data != null) {
            for (item in data) {
                if (item.enable == TAG_ENABLE) {
                    val enumItem = HomeFeatureEnum.findItemByTag(item.name)
                    Log.d("neo", "adapter ${enumItem?.name}")
                    if (enumItem != null)
                        mItem.add(enumItem)
                }
            }
            notifyDataSetChanged()
        }
    }

    fun setItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}