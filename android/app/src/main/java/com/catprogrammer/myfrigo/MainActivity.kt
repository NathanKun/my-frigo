package com.catprogrammer.myfrigo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.catprogrammer.myfrigo.model.Food
import com.catprogrammer.myfrigo.util.HttpUtil
import com.catprogrammer.myfrigo.util.ListViewAdapter
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private var foods = ArrayList<Food>()
    private val jsonParser = JsonParser()
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
        foodlist_swiperefresh.isRefreshing = true

        // add adapter to listview, link dataToShow to adapter
        mAdapter = ListViewAdapter(this, foods)
        foodlist_listview.adapter = mAdapter

        getData()
    }

    private fun getData() {
        val cb: Callback = object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Request failure", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (response == null) {
                    Log.d("http request", "response == null")
                } else if (!response.isSuccessful) {
                    Log.d("http request", "response not successful")
                    Log.d("http request", "response code: ${response.code()}")
                } else {
                    val body = response.body()
                    if (body == null) {
                        Log.d("http request", "body == null")
                    } else {
                        val bodyStr = body.string()
                        Log.d("http request", bodyStr)
                        val json: JsonObject = jsonParser.parse(bodyStr).asJsonObject

                        if (!json.get("success").asBoolean) {
                            Log.d("http request", "success = false")
                        } else { // success = true
                            Log.d("http request", "success = true")
                            val data = json.get("data").asJsonObject
                            val entries = data.entrySet() // foods

                            foods = ArrayList()
                            for (map in entries) { // one food
                                val foodJson = map.value.asJsonObject
                                foods.add(Food.populateFood(foodJson))

                            }

                            runOnUiThread {
                                mAdapter.updateDataSet(foods)
                                foodlist_swiperefresh.isRefreshing = false
                            }
                            return
                        } // if json.success is true end
                    } // if body not null end
                } // if response isSuccessful end
            } // fun onResponse end
        }// val cb end

        HttpUtil.getInstance().getAllFoods(cb)
    }
}


