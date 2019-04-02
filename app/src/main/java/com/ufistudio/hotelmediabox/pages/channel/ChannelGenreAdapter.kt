package com.ufistudio.hotelmediabox.pages.channel

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import kotlinx.android.synthetic.main.item_genre_list.view.*

class ChannelGenreAdapter : RecyclerView.Adapter<ChannelGenreAdapter.ViewHolder>() {

    private var mIsFocus: Boolean = false
    private var mSelectPosition: Int = 0

    interface OnItemClickListener {
        fun onClick(view: View)
    }

    private var mListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_genre_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = GenreType.values().size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = GenreType.values()[position]
        holder.bind()
        holder.itemView.text_genre_type.text = holder.itemView.context.getString(item.stringRes)
        holder.itemView.text_genre_type.tag = item
        holder.itemView.text_genre_type.setTextColor(
            if (position == mSelectPosition && mIsFocus)
                ContextCompat.getColor(holder.itemView.context, R.color.colorYellow)
            else
                ContextCompat.getColor(holder.itemView.context, R.color.colorWhite)
        )
        holder.itemView.setOnClickListener { view ->
            mListener?.onClick(view)
        }
    }

    fun setItemClickListener(listener: ChannelGenreAdapter.OnItemClickListener) {
        mListener = listener
    }

    fun selectUp():GenreType {
        if (mSelectPosition == GenreType.values().size-1) return GenreType.values().last()
        mSelectPosition++
        notifyDataSetChanged()
        return GenreType.values()[mSelectPosition]
    }

    fun selectDown():GenreType {
        if (mSelectPosition == 0) return GenreType.values().first()
        mSelectPosition--
        notifyDataSetChanged()
        return GenreType.values()[mSelectPosition]
    }

    fun getSelectType(): String{
        return GenreType.values()[mSelectPosition].name
    }

    fun setFocus(isFocus: Boolean){
        mIsFocus = isFocus
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
        }
    }
}