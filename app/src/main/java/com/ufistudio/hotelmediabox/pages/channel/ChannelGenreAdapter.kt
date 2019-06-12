package com.ufistudio.hotelmediabox.pages.channel

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.repository.data.GenreType
import kotlinx.android.synthetic.main.item_genre_list.view.*

class ChannelGenreAdapter : RecyclerView.Adapter<ChannelGenreAdapter.ViewHolder>() {

    private var mIsFocus: Boolean = false
    private var mSelectPosition: Int = 0
    private var mGenreList: ArrayList<GenreType>? = arrayListOf()

    interface OnItemClickListener {
        fun onClick(view: View)
    }

    private var mListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_genre_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mGenreList?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mGenreList?.get(position)
        holder.bind()
        holder.itemView.text_genre_type.text = item?.display ?: ""
        holder.itemView.text_genre_type.tag = item
        holder.itemView.text_genre_type.setTextColor(
            if(mIsFocus){
                if (position == mSelectPosition)
                    ContextCompat.getColor(holder.itemView.context, R.color.colorYellow)
                else
                    ContextCompat.getColor(holder.itemView.context, R.color.colorWhite)
            }else{
                ContextCompat.getColor(holder.itemView.context, R.color.white30)
            }
        )
        holder.itemView.setOnClickListener { view ->
            mListener?.onClick(view)
        }
    }

    fun setItemClickListener(listener: ChannelGenreAdapter.OnItemClickListener) {
        mListener = listener
    }

    fun selectUp():String {
        mGenreList?.let { list ->
            if(list.size == 0){
                return ""
            }else{
                if (mSelectPosition == list.size -1) return list.last().key
                mSelectPosition++
                notifyDataSetChanged()
                return list[mSelectPosition].key
            }
        }
        return ""
    }

    fun selectDown():String {
        mGenreList?.let { list ->
            if(list.size == 0){
                return ""
            }else{
                if (mSelectPosition == 0) return list.first().key
                mSelectPosition--
                notifyDataSetChanged()
                return list[mSelectPosition].key
            }
        }
        return ""
    }

    fun getSelectType(): String{
        mGenreList?.let { list ->
            return list[mSelectPosition].key
        }
        return ""
    }

    fun setFocus(isFocus: Boolean){
        mIsFocus = isFocus
        notifyDataSetChanged()
    }

    fun setItems(items: ArrayList<GenreType>?) {
        this.mGenreList = items
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
        }
    }
}