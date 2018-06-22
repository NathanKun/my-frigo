package com.catprogrammer.myfrigo.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Parcel
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.catprogrammer.myfrigo.FoodDetailActivity
import com.catprogrammer.myfrigo.R
import com.catprogrammer.myfrigo.model.CountType
import com.catprogrammer.myfrigo.model.Food
import com.catprogrammer.myfrigo.model.GeneralCallback
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter
import com.google.gson.JsonObject


class RecyclerViewAdapter(private val mContext: Context, var foods: ArrayList<Food>, private val handler: Handler)
    : RecyclerSwipeAdapter<RecyclerViewAdapter.SimpleViewHolder>() {

    // call when item on click
    interface OnViewClickListener {
        fun onViewClick(position: Int)
    }


    abstract class SimpleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), OnViewClickListener {
        internal var swipeLayout: SwipeLayout = itemView.findViewById(R.id.foodlist_swipelayout)
        internal val foodlistNameTextview: TextView = itemView.findViewById(R.id.foodlist_name_textview)
        internal val foodlistNoteTextview: TextView = itemView.findViewById(R.id.foodlist_note_textview)
        internal val foodlistUploadDateTextview: TextView = itemView.findViewById(R.id.foodlist_uploaddate_textview)
        internal val foodlistProductionDateTextview: TextView = itemView.findViewById(R.id.foodlist_productiondate_textview)
        internal val foodlistExpirationDateTextview: TextView = itemView.findViewById(R.id.foodlist_expirationdate_textview)
        internal val foodlistCountTextview: TextView = itemView.findViewById(R.id.foodlist_count_textview)
        internal val foodlistCountSeekBar: SeekBar = itemView.findViewById(R.id.foodlist_count_seekBar)
        internal val foodlistPhotoImageview: ImageView = itemView.findViewById(R.id.foodlist_photo_imageview)
        internal val foodlistDeleteImageview: ImageView = itemView.findViewById(R.id.foodlist_delete_imageview)

        init {
            itemView.setOnClickListener { onViewClick(adapterPosition) }
        }
    }


    /* class RecyclerViewAdapter */

    private val callback: GeneralCallback = object : GeneralCallback() {
        override fun onSuccess(data: JsonObject) {
            (mContext as Activity).runOnUiThread { Toast.makeText(mContext, "Food Updated", Toast.LENGTH_LONG).show() }
        }

        override fun onFailure() {
            (mContext as Activity).runOnUiThread { Toast.makeText(mContext, "Update Food Error", Toast.LENGTH_LONG).show() }
        }
    }

    private var openingPosition: Int = -1

    override fun openItem(position: Int) {
        super.openItem(position)
        openingPosition = position
    }

    override fun closeItem(position: Int) {
        super.closeItem(position)
        // prevent closing swift layout trigger click event
        handler.postDelayed({ openingPosition = -1 }, 100)
    }

    override fun closeAllItems() {
        super.closeAllItems()
        // prevent closing swift layout trigger click event
        handler.postDelayed({ openingPosition = -1 }, 100)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listview_food, parent, false)
        return object : SimpleViewHolder(view) {
            // call when item on click
            override fun onViewClick(position: Int) {
                // prevent issue that closing swipe layout trigger click event
                if (!mItemManger.isOpen(position) && swipeLayout.openStatus == SwipeLayout.Status.Close
                        && position != openingPosition) {
                    val food = foods[position]
                    val intent = Intent(mContext, FoodDetailActivity::class.java)
                    intent.putExtra("food", food)
                    mContext.startActivity(intent)
                } // else ignore event
            }
        }
    }

    override fun onBindViewHolder(viewHolder: SimpleViewHolder, position: Int) {
        val food = foods[viewHolder.adapterPosition]

        viewHolder.swipeLayout.addSwipeListener(object : SimpleSwipeListener() {
            override fun onOpen(layout: SwipeLayout?) {
                super.onOpen(layout)
                // closeAllExcept(layout) not working ?
                closeItem(openingPosition)
                handler.postDelayed({ openingPosition = viewHolder.adapterPosition }, 200)
            }

            override fun onClose(layout: SwipeLayout?) {
                super.onClose(layout)
                handler.postDelayed({ openingPosition = -1 }, 200)
            }
        })

        // text
        viewHolder.foodlistNameTextview.text = food.name
        viewHolder.foodlistNoteTextview.text = food.note
        viewHolder.foodlistUploadDateTextview.text = food.uploadDateString()
        viewHolder.foodlistProductionDateTextview.text = food.productionDateString()
        viewHolder.foodlistExpirationDateTextview.text = food.expirationDateString()

        val countTextView = viewHolder.foodlistCountTextview
        countTextView.text = food.count.toString()

        // seek bar
        val seekBar = viewHolder.foodlistCountSeekBar
        seekBar.min = 0
        if (food.countType == CountType.COUNT_TYPE_PERCENTAGE) {
            seekBar.max = 100
        } else {
            seekBar.max = food.count
        }
        seekBar.progress = food.count
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                countTextView.text = progress.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(sb: SeekBar?) {
                food.count = seekBar.progress
                food.push(callback)

                if (food.count == 0) {
                    foods.removeAt(position)
                    notifyItemRemoved(position)
                } else {
                    notifyItemChanged(position)
                }
            }

        })

        // photo
        val imageView = viewHolder.foodlistPhotoImageview
        Glide.with(mContext)
                .setDefaultRequestOptions(
                        RequestOptions()
                                .placeholder(R.drawable.ic_launcher_background)
                                .fallback(R.drawable.ic_launcher_background)
                )
                .load(food.photoUrlSmall)
                .into(imageView)

        // delete button
        val deleteBtn = viewHolder.foodlistDeleteImageview
        deleteBtn.setOnClickListener {
            // clone food
            val undoFood = food.copy()

            // update food
            food.count = 0
            food.push(callback)

            // animation
            foods.removeAt(position)
            notifyItemRemoved(position)

            // snackbar for undo
            Snackbar.make(
                    (mContext as Activity).findViewById(R.id.activity_main) as ConstraintLayout,
                    "deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", UndoListener(mContext, this, undoFood, position))
                    .show()
        }

        mItemManger.bindView(viewHolder.itemView, position)
    }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.foodlist_swipelayout
    }

    override fun getItemCount(): Int {
        return foods.size
    }


    fun updateDataSet(newFoods: ArrayList<Food>) {
        foods = newFoods
        //notifyDataSetChanged()
        if (foods.size != 0)
            notifyItemRangeChanged(0, foods.size)
    }
}
