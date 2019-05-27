package com.ufistudio.hotelmediabox.views

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.interfaces.OnItemClickListener
import com.ufistudio.hotelmediabox.pages.home.HomeFeatureEnum
import com.ufistudio.hotelmediabox.repository.data.HomeIcons
import kotlinx.android.synthetic.main.item_side.view.*

class SideViewAdapter : RecyclerView.Adapter<SideViewAdapter.ViewHolder>() {

    private val TAG_ENABLE: Int = 1
    private val TAG_DISABLE: Int = 0

    private lateinit var mContext: Context
    private var mItem: ArrayList<HomeFeatureEnum> = ArrayList()
    private var mServerItem: ArrayList<HomeIcons> = ArrayList()
    private var mLastIndex = 0
    private var mIsInit = true //若為第一次進來

    private var mListener: OnItemClickListener? = null

    companion object {
        val TAG = SideViewAdapter::class.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_side, parent, false) as View
        mContext = parent.context
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mItem.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
        val item = mItem[position]
        val serverItem = mServerItem[position]


        holder.itemView.image_icon.background = ContextCompat.getDrawable(mContext, item.icon)

        holder.itemView.tag = item.page
        holder.itemView.setOnClickListener { mListener?.onClick(holder.itemView) }

        if (mLastIndex == position) {
            holder.itemView.text_title.visibility = View.VISIBLE
            holder.itemView.image_icon.background = ContextCompat.getDrawable(mContext, item.focusedIcon)
            holder.itemView.text_title.text = serverItem.name
        } else {
            holder.itemView.text_title.visibility = View.GONE
            holder.itemView.image_icon.background = ContextCompat.getDrawable(mContext, item.icon)
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

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    fun setLastPosition(position: Int) {
        mLastIndex = position
        notifyItemChanged(position)
    }

    fun setScrollPosition(position: Int) {
        mLastIndex = position
    }

    fun getSelectPosition(): Int {
        return mLastIndex
    }

    fun getSelectItem(): HomeFeatureEnum {
        return mItem[mLastIndex]
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
        }
    }
}