package com.ufistudio.hotelmediabox.pages.roomService

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.interfaces.OnItemClickListener
import com.ufistudio.hotelmediabox.interfaces.OnItemFocusListener
import com.ufistudio.hotelmediabox.repository.data.RoomServiceCategories
import kotlinx.android.synthetic.main.item_room_service.view.*

class RoomServiceAdapter(private var mClickListener: OnItemClickListener, private var mFocusListener: OnItemFocusListener) : RecyclerView.Adapter<RoomServiceAdapter.ViewHolder>() {

    private var mSideViewIsDisplay: Boolean = false
    private var mSelectIndex: Int = 0
    private var mSideViewIsShow = false
    private var mData: ArrayList<RoomServiceCategories> = ArrayList()

    companion object {
        val TAG_ITEM = "com.ufistudio.hotelmediabox.pages.roomService.item".hashCode()
        val TAG_INDEX = "com.ufistudio.hotelmediabox.pages.roomService.index".hashCode()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_room_service, parent, false) as View
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    /**
     * set last select index
     * @selectIndex: is last select index
     */
    fun selectLast(selectIndex: Int = 0) {
        mSelectIndex = selectIndex
        notifyItemChanged(selectIndex)
    }

    /**
     *set side view is show
     * @sideViewShow: true:Side view is show
     *                true:Side view isn't show
     * if sideViewShow is show, text will become 30% opacity
     */
    fun sideViewIsShow(sideViewShow: Boolean) {
        mSideViewIsShow = sideViewShow
        notifyDataSetChanged()
    }

    fun setData(data: ArrayList<RoomServiceCategories>) {
        mData.addAll(data)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
        val item = mData[position]

        holder.itemView.text_title.text = item.title
        if (mSideViewIsShow) {
            holder.itemView.setOnClickListener(null)
            holder.itemView.onFocusChangeListener = null
            holder.itemView.text_title.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white30))
        } else {
            holder.itemView.text_title.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
            holder.itemView.setTag(TAG_ITEM, item)
            holder.itemView.setTag(TAG_INDEX, position)

            holder.itemView.setOnClickListener { mClickListener.onClick(holder.itemView) }
            holder.itemView.setOnFocusChangeListener { v, hasFocus ->
                mFocusListener.onFoucsed(holder.itemView)
                if (hasFocus) {
                    holder.itemView.text_title.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorYellow))
                } else {
                    holder.itemView.text_title.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
                }
            }
            if (mSelectIndex == position) {
                holder.itemView.requestFocus()
            } else {
                holder.itemView.clearFocus()
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            itemView.isFocusable = true
        }
    }
}