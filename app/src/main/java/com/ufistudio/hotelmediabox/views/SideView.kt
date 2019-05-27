package com.ufistudio.hotelmediabox.views

import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.ufistudio.hotelmediabox.R
import com.ufistudio.hotelmediabox.constants.Page
import com.ufistudio.hotelmediabox.interfaces.OnItemClickListener
import com.ufistudio.hotelmediabox.pages.base.OnPageInteractionListener
import com.ufistudio.hotelmediabox.pages.home.HomeFeatureEnum
import com.ufistudio.hotelmediabox.repository.data.HomeIcons
import kotlinx.android.synthetic.main.view_left_side.view.*

/**
 * Guild
 *
 * Step 1. Add SideView on your xml file
 * Step 2. setAdapterList from your icon list data
 * Step 3. You have to set InteractionListener by "setInteractionListener()" to switch fragment
 * Now the SideView can switchPage automatically
 * Step 4. You can set a index for you show sideView by "setLastPosition()"
 * Step 5. If you want to listener click item of SideView, you can call setOnItemClickListener() to listen.
 *
 */
const val ARG_CURRENT_INDEX: Int = 0
const val ARG_CURRENT_BACK_TITLE: Int = 1

class SideView : ConstraintLayout, OnItemClickListener {

    private var mAdapter: SideViewAdapter? = null
    private var mOutsideListener: OnItemClickListener? = null
    private var mInteractionListener: OnPageInteractionListener.Pane? = null
    private var mFeatureIcons: ArrayList<HomeIcons>? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_left_side, this)

        mAdapter = SideViewAdapter()
        icon_list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        icon_list.adapter = mAdapter

        mAdapter?.setOnItemClickListener(this)
    }

    fun setAdapterList(data: ArrayList<HomeIcons>?) {
        mFeatureIcons = data
        mAdapter?.setData(mFeatureIcons)
    }

    fun setInteractionListener(listener: Any) {
        mInteractionListener = listener as OnPageInteractionListener.Pane
    }

    /**
     * If your fragment want listen the click of side view.
     * you have call this function
     * @listener: Item click listener
     */
    fun setOnItemClickListener(listener: OnItemClickListener) {
        mOutsideListener = listener
    }

    /**
     * Set you need to focus's position
     * @position: the position which you want to focus
     */
    fun setLastPosition(position: Int) {
        mAdapter?.setLastPosition(position)
    }

    fun scrollToPosition(position: Int) {
        mAdapter?.setScrollPosition(position)
        icon_list.scrollToPosition(position)
        mAdapter?.notifyDataSetChanged()
    }

    fun getSelectPosition(): Int {
        return mAdapter?.getSelectPosition() ?: 0
    }

    fun getItemSize(): Int {
        return mAdapter?.itemCount ?: 0
    }

    fun intoPage() {
        if (mAdapter?.getSelectItem()?.page == -100) {
            Toast.makeText(context, "尚未實作", Toast.LENGTH_SHORT).show()
            return
        }
        val b = Bundle()
        b.putParcelableArrayList(Page.ARG_BUNDLE, mFeatureIcons)
        mAdapter?.getSelectItem()?.let { mInteractionListener?.switchPage(R.id.fragment_container, it.page, b, false, false, true) }
    }

    /**
     * Set Side view default Focus
     */
    fun setFocus(homeIcons: ArrayList<HomeIcons>?, feature: HomeFeatureEnum): HashMap<Int, String> {
        var currentIndex: Int = -1
        var currentTitle: String = ""
        if (homeIcons != null) {
            for (i in 0 until homeIcons.size) {
                currentIndex++
                if (homeIcons[i].id == feature.id) {
                    currentTitle = homeIcons[i].backTitle
                    break
                }
                if (homeIcons[i].enable == 0) {
                    currentIndex--
                }
            }
        }
        val result: HashMap<Int, String> = HashMap<Int, String>()
        result[ARG_CURRENT_BACK_TITLE] = currentTitle
        result[ARG_CURRENT_INDEX] = currentIndex.toString()
        return result
    }

    override fun onClick(view: View?) {
        mOutsideListener?.onClick(view)

        if (view?.tag as Int == -100) {
            Toast.makeText(context, "尚未實作", Toast.LENGTH_SHORT).show()
            return
        }
        val b = Bundle()
        b.putParcelableArrayList(Page.ARG_BUNDLE, mFeatureIcons)
        mInteractionListener?.switchPage(R.id.fragment_container, view.tag as Int, b, false, false, true)
    }
}