package com.ufistudio.hotelmediabox.views

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.pages.home.HomeFeatureEnum
import kotlinx.android.synthetic.main.item_side.view.*

class SideViewAdapter : RecyclerView.Adapter<SideViewAdapter.ViewHolder>() {

    private lateinit var mContext: Context
    private var mLastIndex = 0
    private var mIsInit = true //若為第一次進來

    private var mListener: OnItemCLickListener? = null

    interface OnItemCLickListener {
        fun OnClicklistener(view: View)
        fun OnClicklistener()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_side, parent, false) as View
        mContext = parent.context
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return HomeFeatureEnum.values().size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()


        holder.itemView.image_icon.background =
            ContextCompat.getDrawable(mContext, HomeFeatureEnum.values()[position].icon)

        holder.itemView.setOnClickListener { mListener?.OnClicklistener() }

        holder.itemView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            mIsInit = false
            if (hasFocus) {
                holder.itemView.text_title.visibility = View.VISIBLE
                holder.itemView.image_icon.background =
                    ContextCompat.getDrawable(mContext, HomeFeatureEnum.values()[position].focusedIcon)
                holder.itemView.text_title.text = mContext.getString(HomeFeatureEnum.values()[position].title)
            } else {
                holder.itemView.text_title.visibility = View.GONE
                holder.itemView.image_icon.background =
                    ContextCompat.getDrawable(mContext, HomeFeatureEnum.values()[position].icon)
            }
        }
        if (mIsInit && position == 0)
            holder.itemView.requestFocus()
        else {
            holder.itemView.clearFocus()
        }
    }

    fun setOnItemClickListener(listener: OnItemCLickListener) {
        mListener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            itemView.isFocusable = true
        }
    }
}