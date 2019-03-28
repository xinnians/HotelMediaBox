package com.ufistudio.hotelmediabox.pages.channel

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.repository.data.TVChannel
import kotlinx.android.synthetic.main.item_channel_list.view.*

class ChannelListAdapter(private val listener: (TVChannel, Boolean) -> Unit) :
        RecyclerView.Adapter<ChannelListAdapter.ViewHolder>() {

    private var mOriginalItems: ArrayList<TVChannel>? = null
    private var mFilterItems: ArrayList<TVChannel>? = null
    private var mGenreType: String = ""
    private lateinit var mContext: Context
    private var mSelectPosition = 0 //目前被選到的position
    private var mGenreFocus = false //Genre list是否正在focus

    interface OnItemClickListener {
        fun onClick(view: View)
    }

    private var mListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_channel_list, parent, false)
        mContext = view.context
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = if (mFilterItems != null) mFilterItems!!.size else 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mFilterItems?.get(position)?.let { channelData ->
            holder.bind(channelData, listener)
            holder.itemView.tag = channelData
            holder.itemView.setOnClickListener { view ->
                mListener?.onClick(view)
            }
            if (mGenreFocus && position == mSelectPosition) {
                holder.itemView.layout_frame.background = ContextCompat.getDrawable(mContext, R.drawable.home_icon_frame_frame_default)
            } else {
                holder.itemView.layout_frame.setBackgroundResource(0)
            }
            holder.itemView.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    mSelectPosition = position
                    holder.itemView.layout_frame.background = ContextCompat.getDrawable(mContext, R.drawable.home_icon_frame_frame_focused)
                    holder.itemView.text_channelName.setTextColor(ContextCompat.getColor(mContext, R.color.colorYellow))
                } else {
                    holder.itemView.layout_frame.setBackgroundResource(0)
                    holder.itemView.text_channelName.setTextColor(ContextCompat.getColor(mContext, android.R.color.white))
                }
                listener(channelData, hasFocus)
            }
        }
    }

    fun setItems(items: ArrayList<TVChannel>?) {
        this.mOriginalItems = items
        setGenreFilter("")
    }

    fun setItemClickListener(listener: ChannelListAdapter.OnItemClickListener) {
        mListener = listener
    }

    /**
     * 通知Genre list是否被foucs
     * @focus:
     * true:Genre list被focus
     * false:Genre list沒有focus
     */
    fun genreFocus(focus: Boolean) {
        mGenreFocus = focus
    }

    /**
     * 清楚上一個被選擇到的狀態
     */
    fun clearSelectPosition() {
        val int = mSelectPosition
        mSelectPosition = 0
        notifyItemChanged(int)
    }

    fun setGenreFilter(genreType: String) {
        mGenreType = genreType
        mFilterItems = if (genreType == "All" || genreType == "") {
            ArrayList(mOriginalItems)
        } else ArrayList(mOriginalItems?.filter { it.chType == genreType })
        notifyDataSetChanged()
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(data: TVChannel, listener: (TVChannel, Boolean) -> Unit) {
//            Glide.with(itemView.image.context).load(data.images?.first()).into(itemView.image)
//            Log.e("ChannelListAdapter", "TVChannel:$data")
            itemView.text_channelName.text = data.chNum + " " + data.chName
            itemView.isFocusable = true
        }
    }
}