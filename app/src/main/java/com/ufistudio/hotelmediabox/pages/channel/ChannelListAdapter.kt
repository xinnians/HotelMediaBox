package com.ufistudio.hotelmediabox.pages.channel

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.repository.data.IPTVChannel
import kotlinx.android.synthetic.main.item_channel_list.view.*

class ChannelListAdapter(private val listener: (IPTVChannel, Boolean) -> Unit) : RecyclerView.Adapter<ChannelListAdapter.ViewHolder>() {

    private var mOriginalItems: ArrayList<IPTVChannel>? = null
    private var mFilterItems: ArrayList<IPTVChannel>? = null
    private var mGenreType: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_channel_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = if (mFilterItems != null) mFilterItems!!.size else 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mFilterItems?.get(position)?.let { channelData ->
            holder.bind(channelData, listener)
            holder.itemView.tag = channelData

        }
    }

    fun setItems(items: ArrayList<IPTVChannel>?) {
        this.mOriginalItems = items
        setGenreFilter("")
    }

    fun setGenreFilter(genreType: String) {
        mGenreType = genreType
        mFilterItems = if (genreType == "All" || genreType == "") {
            ArrayList(mOriginalItems)
        } else ArrayList(mOriginalItems?.filter { it.genre == genreType })
        notifyDataSetChanged()
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(data: IPTVChannel, listener: (IPTVChannel, Boolean) -> Unit) {
//            Glide.with(itemView.image.context).load(data.images?.first()).into(itemView.image)
//            Log.e("ChannelListAdapter", "IPTVChannel:$data")
            itemView.text_channelName.text = data.number + " " + data.name
            itemView.setOnFocusChangeListener { v, hasFocus ->
                itemView.text_channelName.setTextColor(if (itemView.isFocused) Color.YELLOW else Color.WHITE)
                listener(data, hasFocus)
            }
        }
    }
}