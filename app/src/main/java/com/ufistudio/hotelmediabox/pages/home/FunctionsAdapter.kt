package com.ufistudio.hotelmediabox.pages.home

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import kotlinx.android.synthetic.main.item_home_functions.view.*

class FunctionsAdapter : RecyclerView.Adapter<FunctionsAdapter.ViewHolder>() {

    private lateinit var mContext: Context

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
        return HomeFeatureEnum.values().size
    }

    override fun onBindViewHolder(p0: FunctionsAdapter.ViewHolder, p1: Int) {
        p0.itemView.text_title.setText(HomeFeatureEnum.values()[p1].title)

        if (p0.itemView.image_icon.isFocused) {
            p0.itemView.text_title.setTextColor(ContextCompat.getColor(mContext, R.color.homeIconFrameFocused))
            p0.itemView.image_icon.background =
                ContextCompat.getDrawable(mContext, HomeFeatureEnum.values()[p1].focusedIcon)
        } else {
            p0.itemView.text_title.setTextColor(ContextCompat.getColor(mContext, android.R.color.white))
            p0.itemView.image_icon.background = ContextCompat.getDrawable(mContext, HomeFeatureEnum.values()[p1].icon)
        }
        p0.itemView.tag = HomeFeatureEnum.values()[p1].page
        p0.itemView.setOnClickListener {
            mListener?.onClick(it)
        }
    }

    fun setItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}