package com.catprogrammer.myfrigo.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast
import com.catprogrammer.myfrigo.model.Food
import com.catprogrammer.myfrigo.model.GeneralCallback
import com.google.gson.JsonObject

class UndoListener(private val context: Context, private val adapter: RecyclerViewAdapter,
                   private val undoFood: Food, private val position: Int, private val isUndoDelete: Boolean)
    : View.OnClickListener {

    val cb = object : GeneralCallback() {
        override fun onSuccess(data: JsonObject) {
            (context as Activity).runOnUiThread {
                if (isUndoDelete) {
                    adapter.foods.add(position, undoFood)
                    adapter.notifyItemInserted(position)
                } else {
                    adapter.foods[position] = undoFood
                    adapter.notifyItemChanged(position)
                }
            }
        }

        override fun onFailure() {
            Toast.makeText(context, "撤销失败", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onClick(v: View) {
        undoFood.push(cb)
    }
}