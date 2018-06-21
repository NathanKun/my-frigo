package com.catprogrammer.myfrigo.util

import android.app.Activity
import android.content.Context
import android.support.v7.widget.AppCompatSeekBar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.catprogrammer.myfrigo.R
import com.catprogrammer.myfrigo.model.CountType
import com.catprogrammer.myfrigo.model.Food
import com.catprogrammer.myfrigo.model.GeneralCallback
import com.daimajia.swipe.adapters.BaseSwipeAdapter
import com.google.gson.JsonObject

class ListViewAdapter(private val mContext: Context, private var foods: List<Food>) : BaseSwipeAdapter() {

    val callback: GeneralCallback = object : GeneralCallback() {
        override fun onSuccess(data: JsonObject) {
            (mContext as Activity).runOnUiThread { Toast.makeText(mContext, "Food Updated", Toast.LENGTH_LONG).show() }
        }

        override fun onFailure() {
            (mContext as Activity).runOnUiThread { Toast.makeText(mContext, "Update Food Error", Toast.LENGTH_LONG).show() }
        }

    }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.foodlist_swipelayout
    }

    override fun generateView(position: Int, parent: ViewGroup): View {
        return LayoutInflater.from(mContext).inflate(R.layout.listview_food, parent, false)
    }

    override fun fillValues(position: Int, convertView: View) {
        val food = foods[position]

        // text
        convertView.findViewById<TextView>(R.id.foodlist_name_textview).text = food.name
        convertView.findViewById<TextView>(R.id.foodlist_note_textview).text = food.note
        convertView.findViewById<TextView>(R.id.foodlist_uploaddate_textview).text = food.uploadDateString()
        convertView.findViewById<TextView>(R.id.foodlist_productiondate_textview).text = food.productionDateString()
        convertView.findViewById<TextView>(R.id.foodlist_expirationdate_textview).text = food.expirationDateString()

        val countTextView = convertView.findViewById<TextView>(R.id.foodlist_count_textview)
        countTextView.text = food.count.toString()

        // seek bar
        val seekBar = convertView.findViewById<SeekBar>(R.id.foodlist_count_seekBar)
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
            }

        })

        // photo
        val imageView = convertView.findViewById<ImageView>(R.id.foodlist_photo_imageview)
        Glide.with(mContext)
                .setDefaultRequestOptions(RequestOptions().placeholder(R.drawable.ic_launcher_background))
                .load(food.photoUrlSmall)
                .into(imageView)

        // button
        val deleteBtn = convertView.findViewById<ImageButton>(R.id.foodlist_delete_imageButton)
        deleteBtn.setOnClickListener {
            food.count = 0
            food.push(callback)
        }
    }

    override fun getCount(): Int {
        return foods.size
    }

    override fun getItem(i: Int): Food {
        return foods[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    fun updateDataSet(newFoods: List<Food>) {
        foods = newFoods
        notifyDataSetChanged()
    }
}
