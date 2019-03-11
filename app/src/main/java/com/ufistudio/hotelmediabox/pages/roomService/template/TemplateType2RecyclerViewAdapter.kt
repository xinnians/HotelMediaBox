package com.ufistudio.hotelmediabox.pages.roomService.template

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import kotlinx.android.synthetic.main.item_room_service_type2_content.view.*

class TemplateType2RecyclerViewAdapter(data: ArrayList<Array<String>>) : RecyclerView.Adapter<TemplateType2RecyclerViewAdapter.ViewHolder>() {
    private var mData: ArrayList<Array<String>> = data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateType2RecyclerViewAdapter.ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_room_service_type2_content, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: TemplateType2RecyclerViewAdapter.ViewHolder, position: Int) {
        holder.bind()
        holder.itemView.text_title.text = mData[position][0]
        holder.itemView.text_type.text = mData[position][1]
        holder.itemView.text_price.text = String.format("$%s", mData[position][2])
        holder.itemView.text_description.text = mData[position][3]
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {

        }
    }
}