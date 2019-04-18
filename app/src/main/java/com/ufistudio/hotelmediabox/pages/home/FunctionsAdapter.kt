package com.ufistudio.hotelmediabox.pages.home

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
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
    private var mServerItem: ArrayList<HomeIcons> = ArrayList()
    private var mListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onClick(view: View)
    }

    companion object {
        val TAG = FunctionsAdapter::class.simpleName
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): FunctionsAdapter.ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_home_functions, p0, false) as View
        mContext = p0.context
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mItem.size
    }

    override fun onBindViewHolder(p0: FunctionsAdapter.ViewHolder, position: Int) {
        val serverItem = mServerItem[position]
        p0.itemView.text_title.text = serverItem.name


        if (p0.itemView.image_icon.isFocused) {
            p0.itemView.text_title.setTextColor(ContextCompat.getColor(mContext, R.color.homeIconFrameFocused))
            p0.itemView.image_icon.background = ContextCompat.getDrawable(mContext, mItem[position].focusedIcon)
        } else {
            p0.itemView.text_title.setTextColor(ContextCompat.getColor(mContext, android.R.color.white))
            p0.itemView.image_icon.background = ContextCompat.getDrawable(mContext, mItem[position].icon)
        }
        p0.itemView.tag = mItem[position].page
        p0.itemView.setOnClickListener {
            mListener?.onClick(it)
        }
    }

    fun setData(data: ArrayList<HomeIcons>?) {
        mItem.clear()
        if (data != null) {
            for (item in data) {
                if (item.enable == TAG_ENABLE) {
                    val enumItem = HomeFeatureEnum.findItemById(item.id)
                    if (enumItem != null) {
                        mItem.add(enumItem)
                        mServerItem.add(item)
                    }
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