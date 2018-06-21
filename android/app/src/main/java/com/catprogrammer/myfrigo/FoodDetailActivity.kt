package com.catprogrammer.myfrigo

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.catprogrammer.myfrigo.model.Food
import com.catprogrammer.myfrigo.model.GeneralCallback
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_food_detail.*
import java.time.LocalDate
import android.view.ViewGroup
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.*


class FoodDetailActivity : Activity() {

    private lateinit var food: Food
    private val today: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_detail)

        // get food instance
        food = intent.getParcelableExtra("food") as Food

        // set text
        fooddetail_name_edittext.setText(food.name)
        fooddetail_note_edittext.setText(food.note)
        fooddetail_upload_edittext.setText(food.uploadDateString())
        fooddetail_production_edittext.setText(food.productionDateString())
        fooddetail_expiration_edittext.setText(food.expirationDateString())

        // prd date and exp date EditText on click listener
        // show date picker on click
        val productionOnDateSetListener: DatePickerDialog.OnDateSetListener =
                DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    food.productionDate = LocalDate.of(year, monthOfYear, dayOfMonth)
                    fooddetail_production_edittext.setText(food.productionDateString())
                }
        val expirationOnDateSetListener: DatePickerDialog.OnDateSetListener =
                DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    food.expirationDate = LocalDate.of(year, monthOfYear, dayOfMonth)
                    fooddetail_expiration_edittext.setText(food.expirationDateString())
                }

        fooddetail_production_edittext.setOnClickListener(getEditTextOnClickListener(food.productionDate, productionOnDateSetListener))
        fooddetail_expiration_edittext.setOnClickListener(getEditTextOnClickListener(food.expirationDate, expirationOnDateSetListener))

        // prd date and exp date EditText on long click listener
        // clear text on long press
        val longPressListener: View.OnLongClickListener =
                View.OnLongClickListener { view: View ->
                    (view as EditText).setText("")
                    return@OnLongClickListener true
                }

        fooddetail_production_edittext.setOnLongClickListener(longPressListener)
        fooddetail_expiration_edittext.setOnLongClickListener(longPressListener)

        // photo
        if (food.photoUrlLarge != null && food.photoUrlLarge!!.startsWith("https://")) {
            // load image
            Glide.with(this)
                    .load(food.photoUrlLarge)
                    .into(fooddetail_photo_imageView)

            // add on click listener
            val builder = getImageDialogBuilder(food.photoUrlOriginal!!)
            fooddetail_photo_imageView.setOnClickListener { builder.show() }
        }

        // button
        val cb = object : GeneralCallback() {
            override fun onSuccess(data: JsonObject) {
                runOnUiThread {
                    Toast.makeText(this@FoodDetailActivity, "Saved", Toast.LENGTH_LONG).show()
                    this@FoodDetailActivity.finish()
                }
            }

            override fun onFailure() {
                runOnUiThread { Toast.makeText(this@FoodDetailActivity, "Error", Toast.LENGTH_LONG).show() }
            }
        }
        fooddetail_save_button.setOnClickListener {
            food.name = fooddetail_name_edittext.text.toString()
            food.note = fooddetail_note_edittext.text.toString()
            food.push(cb)
        }
    }

    private fun getEditTextOnClickListener(date: LocalDate?, onDateSetListener: DatePickerDialog.OnDateSetListener): (View) -> Unit {
        return { _: View ->
            val year: Int
            val month: Int
            val day: Int
            if (date != null) {
                year = date.year
                month = date.monthValue
                day = date.dayOfMonth
            } else {
                year = today.year
                month = today.monthValue
                day = today.dayOfMonth
            }
            DatePickerDialog(this, onDateSetListener, year, month, day).show()
        }
    }

    private fun getImageDialogBuilder(url: String): Dialog {
        val builder = Dialog(this, android.R.style.Theme_Light)
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
        builder.window.setBackgroundDrawable(
                ColorDrawable(android.graphics.Color.TRANSPARENT))
        builder.setOnDismissListener { }

        val dialogImageView = ImageView(this)
        Glide.with(this)
                .load(url)
                .into(dialogImageView)

        builder.addContentView(dialogImageView, RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT))
        //builder.show()
        return builder
    }

}
