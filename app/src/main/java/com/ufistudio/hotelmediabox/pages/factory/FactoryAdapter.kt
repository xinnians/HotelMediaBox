package com.ufistudio.hotelmediabox.pages.factory

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.interfaces.OnItemClickListener
import kotlinx.android.synthetic.main.item_factory.view.*

class FactoryAdapter : RecyclerView.Adapter<FactoryAdapter.ViewHolder>() {
    private lateinit var mListener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FactoryAdapter.ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_factory, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return FactoryFeature.values().size
    }

    override fun onBindViewHolder(holder: FactoryAdapter.ViewHolder, position: Int) {
        val item = FactoryFeature.values()[position]
        holder.bind()
        holder.itemView.button_item.text = holder.itemView.context.getString(item.stringRes)
        holder.itemView.button_item.tag = item
        holder.itemView.button_item.setOnClickListener { mListener.onClick(holder.itemView.button_item) }
    }

    fun setOnClickListener(listener: OnItemClickListener) {
        mListener = listener
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {

        }
    }
}