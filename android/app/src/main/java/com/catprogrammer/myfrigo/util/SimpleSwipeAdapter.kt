package com.catprogrammer.myfrigo.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter

import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.implments.SwipeItemAdapterMangerImpl
import com.daimajia.swipe.interfaces.SwipeAdapterInterface
import com.daimajia.swipe.interfaces.SwipeItemMangerInterface
import com.daimajia.swipe.util.Attributes

abstract class SimpleSwipeAdapter protected constructor(context: Context, data: List<Map<String, *>>, resource: Int, from: Array<String>, to: IntArray, flags: Int) : SimpleAdapter(context, data, resource, from, to), SwipeItemMangerInterface, SwipeAdapterInterface {

    private val mItemManger = SwipeItemAdapterMangerImpl(this)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val convertViewIsNull = convertView == null
        val v = super.getView(position, convertView, parent)
        if (convertViewIsNull) {
            mItemManger.initialize(v, position)
        } else {
            mItemManger.updateConvertView(v, position)
        }
        return v
    }

    override fun openItem(position: Int) {
        mItemManger.openItem(position)
    }

    override fun closeItem(position: Int) {
        mItemManger.closeItem(position)
    }

    override fun closeAllExcept(layout: SwipeLayout) {
        mItemManger.closeAllExcept(layout)
    }

    override fun getOpenItems(): List<Int> {
        return mItemManger.openItems
    }

    override fun getOpenLayouts(): List<SwipeLayout> {
        return mItemManger.openLayouts
    }

    override fun removeShownLayouts(layout: SwipeLayout) {
        mItemManger.removeShownLayouts(layout)
    }

    override fun isOpen(position: Int): Boolean {
        return mItemManger.isOpen(position)
    }

    override fun getMode(): Attributes.Mode {
        return mItemManger.mode
    }

    override fun setMode(mode: Attributes.Mode) {
        mItemManger.mode = mode
    }
}