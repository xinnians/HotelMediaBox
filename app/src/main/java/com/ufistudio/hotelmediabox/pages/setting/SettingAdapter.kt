package com.ufistudio.hotelmediabox.pages.setting

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.interfaces.OnItemClickListener
import com.ufistudio.hotelmediabox.interfaces.OnItemFocusListener
import com.ufistudio.hotelmediabox.repository.data.SettingCategories
import kotlinx.android.synthetic.main.item_room_service.view.*

class SettingAdapter(private var mClickListener: OnItemClickListener, private var mFocusListener: OnItemFocusListener) : RecyclerView.Adapter<SettingAdapter.ViewHolder>() {

    private var mSelectIndex: Int = 0
    private var mSideViewIsShow = false
    private var mData: ArrayList<SettingCategories> = ArrayList()
    private var mClearFocus: Boolean = false

    companion object {
        val TAG_ITEM = "com.ufistudio.hotelmediabox.pages.setting.item".hashCode()
        val TAG_INDEX = "com.ufistudio.hotelmediabox.pages.setting.index".hashCode()
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
        mClearFocus = false
        mSelectIndex = selectIndex
        notifyItemChanged(selectIndex)
    }

    fun setSelectPosition(selectIndex: Int) {
        mSelectIndex = selectIndex
    }

    fun getLastPosition(): Int {
        return mSelectIndex
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

    fun fromSideViewBack(selectIndex: Int) {
        mSideViewIsShow = false
        mSelectIndex = selectIndex
        mClearFocus = false
        notifyDataSetChanged()
    }

    fun clearFocus(selectIndex: Int) {
        mClearFocus = true
        notifyItemChanged(selectIndex)
    }

    fun setData(data: ArrayList<SettingCategories>) {
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
            if (mClearFocus) {
                holder.itemView.text_title.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
                holder.itemView.clearFocus()
            } else {
                holder.itemView.text_title.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
                holder.itemView.setTag(TAG_ITEM, item)
                holder.itemView.setTag(TAG_INDEX, position)

                holder.itemView.setOnClickListener { mClickListener.onClick(holder.itemView) }
                if (mSelectIndex == position) {
                    holder.itemView.text_title.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorYellow))
                    mFocusListener.onFoucsed(holder.itemView)
                } else {
                    holder.itemView.text_title.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
                }
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
        }
    }
}