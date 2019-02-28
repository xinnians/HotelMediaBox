package com.ufistudio.hotelmediabox.pages.channel

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import kotlinx.android.synthetic.main.item_genre_list.view.*

class ChannelGenreAdapter(private val focusChangeListener: (String, Boolean) -> Unit) : RecyclerView.Adapter<ChannelGenreAdapter.ViewHolder>() {

    private var mItems: ArrayList<String>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_genre_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = if (mItems != null) mItems!!.size else 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mItems?.get(position)?.let {
            holder.bind(it, focusChangeListener)
            holder.itemView.tag = it
        }
    }

    fun setItems(items: ArrayList<String>?) {
        this.mItems = items
        notifyDataSetChanged()
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(data: String) {
//            Log.e("ChannelGenreAdapter", "Genre :$data")
            itemView.text_genre_type.text = data
        }

        fun bind(data: String, focusChangeListener: (String, Boolean) -> Unit) {
//            Log.e("ChannelGenreAdapter", "Genre :$data")
            itemView.text_genre_type.text = data
            itemView.setOnFocusChangeListener { v, hasFocus ->
                itemView.text_genre_type.setTextColor(if (itemView.isFocused) Color.YELLOW else Color.WHITE)
                focusChangeListener(data, hasFocus)
            }
        }
    }
}