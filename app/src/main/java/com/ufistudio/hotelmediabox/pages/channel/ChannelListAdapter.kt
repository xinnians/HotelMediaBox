package com.ufistudio.hotelmediabox.pages.channel

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.repository.data.TVChannel
import com.ufistudio.hotelmediabox.utils.FileUtils
import kotlinx.android.synthetic.main.item_channel_list.view.*

class ChannelListAdapter : RecyclerView.Adapter<ChannelListAdapter.ViewHolder>() {

    private var mOriginalItems: ArrayList<TVChannel>? = null
    private var mFilterItems: ArrayList<TVChannel>? = null
    private var mGenreType: String = ""
    private lateinit var mContext: Context
    private var mSelectPosition = 0 //目前被選到的position
    private var mCurrentTVChannel: TVChannel? = null
    private var mIsFocus = false

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
            holder.bind(channelData)
            holder.itemView.tag = channelData
            holder.itemView.setOnClickListener { view ->
                mListener?.onClick(view)
            }

            if (position == mSelectPosition) {
                if (mIsFocus) {
                    holder.itemView.layout_frame.background =
                        ContextCompat.getDrawable(mContext, R.drawable.home_icon_frame_frame_focused)
                    holder.itemView.text_channelName.setTextColor(ContextCompat.getColor(mContext, R.color.colorYellow))
                } else {
                    holder.itemView.layout_frame.background =
                        ContextCompat.getDrawable(mContext, R.drawable.home_icon_frame_frame_default)
                    holder.itemView.text_channelName.setTextColor(
                        ContextCompat.getColor(
                            mContext,
                            android.R.color.white
                        )
                    )
                }
            } else {
                holder.itemView.layout_frame.setBackgroundResource(0)
                holder.itemView.text_channelName.setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        android.R.color.white
                    )
                )
            }

//            holder.itemView.setOnFocusChangeListener { v, hasFocus ->
//                if (hasFocus) {
//                    mSelectPosition = position
//                    holder.itemView.layout_frame.background =
//                        ContextCompat.getDrawable(mContext, R.drawable.home_icon_frame_frame_focused)
//                    holder.itemView.text_channelName.setTextColor(ContextCompat.getColor(mContext, R.color.colorYellow))
//                } else {
//                    holder.itemView.layout_frame.setBackgroundResource(0)
//                    holder.itemView.text_channelName.setTextColor(
//                        ContextCompat.getColor(
//                            mContext,
//                            android.R.color.white
//                        )
//                    )
//                }
//                listener(channelData, hasFocus)
//            }
        }
    }

    fun setItems(items: ArrayList<TVChannel>?) {
        this.mOriginalItems = items
        setGenreFilter("")
    }

    fun setItemClickListener(listener: ChannelListAdapter.OnItemClickListener) {
        mListener = listener
    }

    fun selectDownItem(): TVChannel? {
        if (mFilterItems?.size ?: 0 == 0) {
            return null
        }
        if (mSelectPosition == 0 && mFilterItems?.size ?: 0 > 0) {
            mSelectPosition = mFilterItems?.size?.minus(1) ?: 0
            notifyDataSetChanged()
            mCurrentTVChannel = mFilterItems?.get(mSelectPosition)
            return mCurrentTVChannel
        }
        mSelectPosition -= 1
        notifyDataSetChanged()
        mCurrentTVChannel = mFilterItems?.get(mSelectPosition)
        return mCurrentTVChannel
    }

    fun selectUPItem(): TVChannel? {
        if (mFilterItems?.size ?: 0 == 0) {
            return null
        }
        if ((mFilterItems?.size ?: 0) - 1 == mSelectPosition) {
            mSelectPosition = 0
            notifyDataSetChanged()
            mCurrentTVChannel = mFilterItems?.get(mSelectPosition)
            return mCurrentTVChannel
        }
        mSelectPosition++
        notifyDataSetChanged()
        mCurrentTVChannel = mFilterItems?.get(mSelectPosition)
        return mCurrentTVChannel
    }

    fun getCurrentTVChannel(): TVChannel? {
        if (mFilterItems?.size ?: 0 == 0) {
            return null
        }
        if ((mFilterItems?.size ?: 0) - 1 >= mSelectPosition && mSelectPosition >= 0) {
            return mFilterItems?.get(mSelectPosition)
        } else {
            return null
        }
    }

    fun setCurrentTVChannel(channel: TVChannel) {
        if (mFilterItems?.contains(channel) == true) {
            mCurrentTVChannel = channel
            mSelectPosition = mFilterItems?.indexOf(mCurrentTVChannel!!) ?: 0
        } else {
            mCurrentTVChannel = null
            mSelectPosition = 0
        }

    }

    fun getSelectPosition(): Int {
        return mSelectPosition
    }

    /**
     * 通知是否被foucs
     * @focus:
     * true:list被focus
     * false:list沒有focus
     */
    fun setFocus(isFocus: Boolean) {
        mIsFocus = isFocus
        notifyDataSetChanged()
    }

    /**
     * 清楚上一個被選擇到的狀態
     */
    fun clearSelectPosition() {
        if (mFilterItems?.contains(mCurrentTVChannel) == true) {
            mSelectPosition = mFilterItems?.indexOf(mCurrentTVChannel) ?: 0
        } else {
            mSelectPosition = 0
        }
    }

    fun setGenreFilter(genreType: String) {
        if (mOriginalItems == null) {
            return
        }

        mGenreType = genreType
        mFilterItems = if (genreType == "All" || genreType == "") {
            ArrayList(mOriginalItems)
        } else ArrayList(mOriginalItems?.filter { it.chGenre == genreType })
        clearSelectPosition()
        notifyDataSetChanged()
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(data: TVChannel) {
//            Glide.with(itemView.image.context).load(data.images?.first()).into(itemView.image)
//            Log.e("ChannelListAdapter", "TVChannel:$data")
            itemView.text_channelName.text = data.chNum + " " + data.chName
            Glide.with(itemView.context)
                .load(FileUtils.getFileFromStorage(data.chLogo.normalIconName))
//                    .skipMemoryCache(true)
                .into(itemView.view_icon)
        }
    }
}