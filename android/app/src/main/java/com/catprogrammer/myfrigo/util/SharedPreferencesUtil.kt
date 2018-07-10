package com.catprogrammer.myfrigo.util

import android.content.Context
import android.content.SharedPreferences
import com.catprogrammer.myfrigo.model.Food
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesUtil {
    companion object {
        fun writeFoods(context: Context, foods: List<Food>) {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences("myfrigo", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            editor.remove("foods")
            editor.putString("foods", Gson().toJson(foods))
            editor.apply()
        }

        fun readFoods(context: Context): List<Food> {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences("myfrigo", Context.MODE_PRIVATE)
            val json = sharedPreferences.getString("foods", "")

            if (json == "") {
                return listOf()
            }

            return Gson().fromJson<List<Food>>(json, object : TypeToken<List<Food>>() { }.type)
        }
    }
}