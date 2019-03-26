package com.ufistudio.hotelmediabox.pages.roomService.template

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.repository.data.RoomServiceCategories
import com.ufistudio.hotelmediabox.repository.data.RoomServiceContent
import com.ufistudio.hotelmediabox.utils.MiscUtils
import kotlinx.android.synthetic.main.item_room_service_type2_content.view.*

class TemplateType2RecyclerViewAdapter(data: ArrayList<RoomServiceContent>) : RecyclerView.Adapter<TemplateType2RecyclerViewAdapter.ViewHolder>() {
    private var mData: ArrayList<RoomServiceContent> = data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateType2RecyclerViewAdapter.ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_room_service_type2_content, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: TemplateType2RecyclerViewAdapter.ViewHolder, position: Int) {
        holder.bind()
        val item: RoomServiceContent? = mData[position]
        if (item == null) {
            holder.itemView.visibility = View.INVISIBLE
        } else {
            holder.itemView.visibility = View.VISIBLE
            holder.itemView.text_title.text = item.title
            holder.itemView.text_type.text = item.type
            holder.itemView.text_price.text = String.format("$%s", item.price)
            holder.itemView.text_description.text = item.content
            Glide.with(holder.itemView.context)
                    .load(MiscUtils.getFileFromStorage("/image", item.image))
                    .into(holder.itemView.image_top)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {

        }
    }
}