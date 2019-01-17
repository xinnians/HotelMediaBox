package com.ufistudio.hotelmediabox.utils

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.Log
import com.ufistudio.hotelmediabox.R

object ActivityUtils {

    private val TAG = ActivityUtils::class.simpleName

    /**
     * Replace fragment.
     * Note that when the passing fragment is the root fragment, the current transaction
     * won't be added into back stack EVEN IF {@code addToBackStack} is true.
     *
     * @param manager
     * @param fragment
     * @param containerId
     * @param tag
     * @param addToBackStack
     */
    fun replcaeFragment(manager: FragmentManager, fragment: Fragment, containerId: Int, tag: String, addToBackStack: Boolean, wothAnimation: Boolean) {
        var transaction = manager.beginTransaction()
        if (wothAnimation) {
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
        }
        transaction.replace(containerId, fragment, tag)

        // DO NOT add the first transaction in back stack,
        // otherwise the very first page would be blank.
        if (manager.backStackEntryCount == 0 || addToBackStack)
            transaction.addToBackStack(tag)
        try {
            transaction.commit()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "[replcaeFragment] ${e.message}")
        }
    }

    /**
     * Add fragment.
     * Note that when the passing fragment is the root fragment, the current transaction
     * won't be added into back stack EVEN IF {@code addToBackStack} is true.
     *
     * @param manager
     * @param fragment
     * @param containerId
     * @param tag
     * @param addToBackStack
     */
    fun addFragment(manager: FragmentManager, fragment: Fragment, containerId: Int, tag: String, addToBackStack: Boolean, wothAnimation: Boolean) {
        var transaction = manager.beginTransaction()
        if (wothAnimation) {
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
        }
        transaction.add(containerId, fragment, tag)

        // DO NOT add the first transaction in back stack,
        // otherwise the very first page would be blank.
        if (manager.backStackEntryCount == 0 || addToBackStack)
            transaction.addToBackStack(tag)
        try {
            transaction.commit()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "[replcaeFragment] ${e.message}")
        }
    }

    fun clearFragmentBackStack(manager: FragmentManager) = manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

    fun popBackByPageTag(manager: FragmentManager, tag: String?) = tag?.let { manager.popBackStackImmediate(tag, 0) }

}