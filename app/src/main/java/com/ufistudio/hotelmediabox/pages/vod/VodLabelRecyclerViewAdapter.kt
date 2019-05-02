package com.ufistudio.hotelmediabox.pages.vod

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import kotlinx.android.synthetic.main.item_vod_label.view.*

class VodLabelRecyclerViewAdapter : RecyclerView.Adapter<VodLabelRecyclerViewAdapter.ViewHolder>() {
    private val mData: ArrayList<String> = ArrayList<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_vod_label, parent, false) as View
        view.isFocusable = false
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: VodLabelRecyclerViewAdapter.ViewHolder, position: Int) {
        val item = mData[position]
        holder.itemView.text_title.text = item
    }

    fun setData(data: List<String>) {
        mData.clear()
        mData.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {

        }
    }
}