package com.ufistudio.hotelmediabox.pages.home

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ufistudio.hotelmediabox.R
import kotlinx.android.synthetic.main.item_home_functions.view.*

class FunctionsAdapter : RecyclerView.Adapter<FunctionsAdapter.ViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): FunctionsAdapter.ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_home_functions, p0, false) as View
        context = p0.context
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 10
    }

    override fun onBindViewHolder(p0: FunctionsAdapter.ViewHolder, p1: Int) {
        p0.itemView.image_icon.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}