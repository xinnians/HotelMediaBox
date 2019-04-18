package com.ufistudio.hotelmediabox.pages.roomService.template

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.repository.data.RoomServiceContent
import com.ufistudio.hotelmediabox.utils.FileUtils
import kotlinx.android.synthetic.main.item_room_service_type2_content.view.*

class TemplateType2RecyclerViewAdapter : RecyclerView.Adapter<TemplateType2RecyclerViewAdapter.ViewHolder>() {
    private var mData: ArrayList<RoomServiceContent>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateType2RecyclerViewAdapter.ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_room_service_type2_content, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        //若是不足三個則補滿，判斷type = -1則隱藏
        while (mData?.size!! < 3) {
            mData?.add(RoomServiceContent("", "", "-1", "", "", "", ""))
        }
        return mData?.size!!
    }

    override fun onBindViewHolder(holder: TemplateType2RecyclerViewAdapter.ViewHolder, position: Int) {
        holder.bind()
        val item: RoomServiceContent? = mData!![position]
        if (item == null || TextUtils.equals(item.type, "-1")) {
            holder.itemView.visibility = View.INVISIBLE
        } else {
            holder.itemView.visibility = View.VISIBLE
            holder.itemView.text_title.text = item.title
            holder.itemView.text_type.text = item.type
            holder.itemView.text_price.text = String.format("$%s", item.price)
            holder.itemView.text_description.text = item.content
            Glide.with(holder.itemView.context)
                    .load(FileUtils.getFileFromStorage(item.image))
                    .skipMemoryCache(true)
                    .into(holder.itemView.image_top)
        }
    }

    fun setData(data: ArrayList<RoomServiceContent>) {
        mData = data
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {

        }
    }
}