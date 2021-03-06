package com.catprogrammer.myfrigo.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.catprogrammer.myfrigo.FoodDetailActivity
import com.catprogrammer.myfrigo.MainActivity
import com.catprogrammer.myfrigo.R
import com.catprogrammer.myfrigo.model.CountType
import com.catprogrammer.myfrigo.model.Food
import com.catprogrammer.myfrigo.model.GeneralCallback
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter
import com.google.gson.JsonObject
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil


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

    var openingPosition: Int = -1

    override fun openItem(position: Int) {
        super.openItem(position)
        openingPosition = position
        (mContext as MainActivity).collapseFab()
    }

    override fun closeItem(position: Int) {
        super.closeItem(position)
        // prevent closing swift layout trigger click event
        handler.postDelayed({ openingPosition = -1 }, 300)
    }

    override fun closeAllItems() {
        super.closeAllItems()
        // prevent closing swift layout trigger click event
        handler.postDelayed({ openingPosition = -1 }, 300)
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
                    (mContext as MainActivity).collapseFab()
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
                //closeItem(openingPosition)
                handler.postDelayed({ openingPosition = viewHolder.adapterPosition }, 200)
            }

            override fun onClose(layout: SwipeLayout?) {
                super.onClose(layout)
                handler.postDelayed({ openingPosition = -1 }, 200)
            }
        })

        // text
        viewHolder.foodlistNameTextview.text = concatFoodNameAndCount(food)
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
            seekBar.max = ceil(food.count * 1.5).toInt()
        }
        seekBar.progress = food.count
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                countTextView.text = progress.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(sb: SeekBar?) {
                // clone food
                val undoFood = food.copy()

                food.count = seekBar.progress
                food.push(getUpdateFoodCallback(viewHolder.adapterPosition, undoFood))
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
            food.push(getUpdateFoodCallback(viewHolder.adapterPosition, undoFood))
            closeItem(viewHolder.adapterPosition)
        }

        // set text color base on expiration date
        viewHolder.foodlistNameTextview.setTextColor(when {
            food.expirationDate?.isEqual(LocalDate.now()) == true -> // expire today
                mContext.getColor(R.color.expirationYellow)
            food.expirationDate?.isBefore(LocalDate.now()) == true -> // expired
                mContext.getColor(R.color.expirationRed)
            (food.productionDate != null && food.expirationDate != null &&
                    ((ChronoUnit.DAYS.between(food.productionDate, food.expirationDate) / 10) >
                            (ChronoUnit.DAYS.between(LocalDate.now(), food.expirationDate)) ||
                            (ChronoUnit.DAYS.between(food.createdAt!!.toLocalDate(), food.expirationDate) / 10) >
                            (ChronoUnit.DAYS.between(LocalDate.now(), food.expirationDate)))
                    ) -> // 90% days passed
                mContext.getColor(R.color.expirationYellow)
            else -> mContext.getColor(android.R.color.primary_text_light)
        })

        mItemManger.bindView(viewHolder.itemView, position)
    }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.foodlist_swipelayout
    }

    override fun getItemCount(): Int {
        return foods.size
    }


    fun notifySort() {
        if (foods.size != 0)
            notifyItemRangeChanged(0, foods.size)
    }

    private fun getUpdateFoodCallback(position: Int, undoFood: Food): GeneralCallback {
        return object : GeneralCallback() {
            override fun onSuccess(data: JsonObject) {
                val food = Food.populateFood(data)

                (mContext as Activity).runOnUiThread {
                    if (food.count == 0) {
                        foods.removeAt(position)
                        notifyItemRemoved(position)
                    } else {
                        notifyItemChanged(position)
                    }

                    // snackbar for undo
                    val text = if (food.count == 0) "已删除" else "已更新"
                    val isUndoDelete = food.count == 0
                    Snackbar.make(
                            mContext.findViewById(R.id.activity_main) as ConstraintLayout,
                            text, Snackbar.LENGTH_LONG)
                            .setAction("撤销", UndoListener(mContext, this@RecyclerViewAdapter, undoFood, position, isUndoDelete))
                            .show()
                }
            }

            override fun onFailure() {
                (mContext as Activity).runOnUiThread {
                    Snackbar.make(
                            mContext.findViewById(R.id.activity_main) as ConstraintLayout,
                            "更新失败", Snackbar.LENGTH_LONG)
                            .show()
                }
            }
        }
    }

    private fun concatFoodNameAndCount(food: Food): String {
        return if(food.countType == CountType.COUNT_TYPE_NUMBER) {
            "${food.name} x${food.count}"
        } else {
            "${food.name} (${food.count}%)"
        }
    }
}
