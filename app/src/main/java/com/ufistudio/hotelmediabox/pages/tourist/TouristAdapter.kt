package com.ufistudio.hotelmediabox.pages.tourist

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.interfaces.OnItemClickListener
import com.ufistudio.hotelmediabox.interfaces.OnItemFocusListener
import com.ufistudio.hotelmediabox.repository.data.TouristLocation
import kotlinx.android.synthetic.main.item_nearby_me.view.*

class TouristAdapter(private var mClickListener: OnItemClickListener, private var mFocusListener: OnItemFocusListener) : RecyclerView.Adapter<TouristAdapter.ViewHolder>() {

    private var mSideViewIsDisplay: Boolean = false
    private var mSelectIndex: Int = 0
    private var mSideViewIsShow = false
    private var mClearFocus: Boolean = false
    private var mData: ArrayList<TouristLocation> = ArrayList()

    companion object {
        val TAG_ITEM = "com.ufistudio.hotelmediabox.pages.tourist.item".hashCode()
        val TAG_INDEX = "com.ufistudio.hotelmediabox.pages.tourist.index".hashCode()
        val TAG: String = TouristAdapter::class.java.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_nearby_me, parent, false) as View
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
        mClearFocus = false
        notifyItemChanged(selectIndex)
    }

    fun fromSideViewBack(selectIndex: Int) {
        mSideViewIsShow = false
        mSelectIndex = selectIndex
        mClearFocus = false
        notifyDataSetChanged()
    }

    fun clearFocus(selectIndex: Int) {
        mClearFocus = true
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
        val item = mData[position]

        holder.itemView.text_title.text = item.placeName
        Log.e(TAG,"[onBindViewHolder] position : $position, mClearFocus : $mClearFocus, titleText : ${holder.itemView.text_title.text}")
        if (mSideViewIsShow) {
            holder.itemView.setOnClickListener(null)
            holder.itemView.text_title.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white30))
        } else {
            if (mClearFocus && mSelectIndex == position) {
                Log.e(TAG,"[onBindViewHolder] 進 mClearFocus == true 判斷")
                holder.itemView.text_title.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
            } else {
                Log.e(TAG,"[onBindViewHolder] 進 mClearFocus == false 判斷")
                holder.itemView.text_title.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
                holder.itemView.setTag(TAG_ITEM, item)
                holder.itemView.setTag(TAG_INDEX, position)

                holder.itemView.setOnClickListener { mClickListener.onClick(holder.itemView) }

                if (mSelectIndex == position) {
                    mFocusListener.onFoucsed(holder.itemView)
                    holder.itemView.text_title.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorYellow))

                } else {
                    holder.itemView.text_title.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
                }
            }
        }

        if(mSelectIndex == position){
            if(mSideViewIsShow || mClearFocus){
                holder.itemView.layout_nearby_me.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.home_icon_frame_frame_default)
            }else{
                holder.itemView.layout_nearby_me.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.home_icon_frame_frame_focused)
            }
        }else{
            holder.itemView.layout_nearby_me.setBackgroundResource(0)
        }
    }

    fun setData(data: ArrayList<TouristLocation>) {
        mData.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
        }
    }

    fun setSelectPosition(selectIndex: Int) {
        mSelectIndex = selectIndex
    }

    fun getLastPosition(): Int {
        return mSelectIndex
    }

}