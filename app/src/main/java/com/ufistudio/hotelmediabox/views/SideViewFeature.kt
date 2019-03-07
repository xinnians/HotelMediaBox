package com.ufistudio.hotelmediabox.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import com.ufistudio.hotelmediabox.R
import kotlinx.android.synthetic.main.view_left_side.view.*

class SideViewFeature : ConstraintLayout {
    private var mAdapter: SideViewAdapter? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_left_side, this)

        mAdapter = SideViewAdapter()
        icon_list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        icon_list.adapter = mAdapter
    }

    fun setOnItemClickListener(listener: SideViewAdapter.OnItemCLickListener) {
        mAdapter?.setOnItemClickListener(listener)
    }
}