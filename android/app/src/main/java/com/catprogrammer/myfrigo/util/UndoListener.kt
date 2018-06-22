package com.catprogrammer.myfrigo.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast
import com.catprogrammer.myfrigo.model.Food
import com.catprogrammer.myfrigo.model.GeneralCallback
import com.google.gson.JsonObject

class UndoListener(private val context: Context, private val adapter: RecyclerViewAdapter,
                   private val food: Food, private val position: Int) : View.OnClickListener {

    val cb = object : GeneralCallback() {
        override fun onSuccess(data: JsonObject) {
            (context as Activity).runOnUiThread {
                adapter.foods.add(position, food)
                adapter.notifyItemInserted(position)
            }
        }

        override fun onFailure() {
            Toast.makeText(context, "Undo error", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onClick(v: View) {
        food.push(cb)
    }
}