package com.catprogrammer.myfrigo

import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SimpleAdapter
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {


    private val KEYS = arrayOf("name", "note", "production_date", "expiration_date", "photo")
    private val IDS = intArrayOf(R.id.foodlist_name_textview, R.id.foodlist_note_textview,
            R.id.foodlist_productiondate_textview, R.id.foodlist_expirationdate_textview,
            R.id.foodlust_photo_imageiew)

    private val dataToShow = ArrayList<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_to_camera.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        // add swipe down refresh listener, call getData() when swipe down
        foodlist_swiperefresh.setOnRefreshListener({ getData() })

        // add adapter to listview, link dataToShow to adapter
        val mAdapter = SimpleAdapter(this, dataToShow, R.layout.listview_food, KEYS, IDS)
        mAdapter.setViewBinder({ view, data, _ ->
            if (view is ImageView && data is Bitmap) {
                Glide.with(this)
                        .load(data)
                        .into(view)
                return@setViewBinder true
            }
            return@setViewBinder false
        })
        foodlist_listview.adapter = mAdapter
    }

    fun getData() {
        val cb : Callback = object : Callback {
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
                        val json = JSONObject(body.string())
                        Log.d("http request", json.toString())

                        if (!json.getBoolean("success")) {
                            Log.d("http request", "success = false")
                        } else { // success = true
                            Log.d("http request", "success = true")
                            runOnUiThread { Toast.makeText(applicationContext, "Uploaded", Toast.LENGTH_LONG).show() }
                            return
                        }
                    }
                }
            }

        }
    }
}
