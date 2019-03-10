package com.ufistudio.hotelmediabox.pages.roomService

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import kotlinx.android.synthetic.main.item_room_service.view.*

class RoomServiceAdapter : RecyclerView.Adapter<RoomServiceAdapter.ViewHolder>() {
    private val values: ArrayList<String> = ArrayList()

    init {
        values.add("House Keeping")
        values.add("Food & Beverage")
    }

    private var  view:View?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        view=LayoutInflater.from(parent.context).inflate(R.layout.item_room_service, parent, false) as View
        return ViewHolder(view!!)
    }

    override fun getItemCount(): Int {
        return values.size
    }

    fun requestFocus(){
        view?.requestFocus()
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.text_title.text = values[position]
        holder.itemView.text_title.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))


        holder.itemView.setOnFocusChangeListener { v, hasFocus ->
            Log.d("neo","focus")
            if (hasFocus) {
//                mSelectPosition = position
                holder.itemView.text_title.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorYellow))
            } else {
                holder.itemView.text_title.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
            }
//            listener(channelData, hasFocus)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(view: View) {
            itemView.isFocusable = true
        }
    }
}