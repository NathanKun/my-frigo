package com.catprogrammer.myfrigo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.catprogrammer.myfrigo.model.Food
import com.catprogrammer.myfrigo.model.GeneralCallback
import com.catprogrammer.myfrigo.util.HttpUtil
import com.catprogrammer.myfrigo.util.ListViewAdapter
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity() {

    private var foods = ArrayList<Food>()
    private lateinit var mAdapter: ListViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_to_camera.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        // add swipe down refresh listener, call getData() when swipe down
        foodlist_swiperefresh.setOnRefreshListener { getData() }

        // add adapter to listview, link dataToShow to adapter
        mAdapter = ListViewAdapter(this, emptyList())
        foodlist_listview.adapter = mAdapter
        foodlist_listview.setOnItemClickListener { _: AdapterView<*>, _: View, i: Int, _: Long ->
            val food = foods[i]
            val intent = Intent(this, FoodDetailActivity::class.java)
            intent.putExtra("food", food)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    private fun getData() {
        val cb: GeneralCallback = object : GeneralCallback() {
            override fun onSuccess(data: JsonObject) {
                val entries = data.entrySet() // foods

                foods = ArrayList()
                for (map in entries) { // one food
                    val foodJson = map.value.asJsonObject
                    foods.add(Food.populateFood(foodJson))
                }
                foods.reverse()

                runOnUiThread {
                    mAdapter.updateDataSet(foods)
                    foodlist_swiperefresh.isRefreshing = false
                }
                return
            }

            override fun onFailure() {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Request failure", Toast.LENGTH_LONG).show()
                }
            }
        }// val cb end

        foodlist_swiperefresh.isRefreshing = true
        HttpUtil.getInstance().getAllFoods(cb)
    }
}


