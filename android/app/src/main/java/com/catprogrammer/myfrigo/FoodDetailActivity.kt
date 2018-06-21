package com.catprogrammer.myfrigo

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import com.bumptech.glide.Glide
import com.catprogrammer.myfrigo.model.CountType
import com.catprogrammer.myfrigo.model.Food
import com.catprogrammer.myfrigo.model.GeneralCallback
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_food_detail.*
import java.time.LocalDate


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
        val productionDatePickerDialog = getDatePickerDialog(food.productionDate, productionOnDateSetListener)
        val expirationDatePickerDialog = getDatePickerDialog(food.expirationDate, expirationOnDateSetListener)
        fooddetail_production_edittext.setOnClickListener { productionDatePickerDialog.show() }
        fooddetail_expiration_edittext.setOnClickListener { expirationDatePickerDialog.show() }

        // prd date and exp date EditText on long click listener
        // clear text on long press
        fooddetail_production_edittext.setOnLongClickListener { _: View ->
            fooddetail_production_edittext.setText("")
            food.productionDate = null
            return@setOnLongClickListener true
        }
        fooddetail_expiration_edittext.setOnLongClickListener { _: View ->
            fooddetail_expiration_edittext.setText("")
            food.expirationDate = null
            return@setOnLongClickListener true
        }

        // count type switch
        fooddetail_counttype_switch.isChecked = food.countType == CountType.COUNT_TYPE_PERCENTAGE
        fooddetail_counttype_switch.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            food.countType = if (b) CountType.COUNT_TYPE_PERCENTAGE else CountType.COUNT_TYPE_NUMBER
            onCountTypeUpdate()
        }
        onCountTypeUpdate()

        // count by % - seek bar
        fooddetail_count_percentage_seekBar.max = 100
        fooddetail_count_percentage_seekBar.min = 0
        fooddetail_count_percentage_seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                fooddetail_count_number_editText.setText(progress.toString())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(sb: SeekBar?) {
                food.count = fooddetail_count_percentage_seekBar.progress
            }
        })

        // count by number - edit text
        fooddetail_count_number_editText.setText(food.count.toString())

        if(food.countType == CountType.COUNT_TYPE_PERCENTAGE) {
            fooddetail_count_percentage_seekBar.progress = food.count
        } else {
            fooddetail_count_percentage_seekBar.visibility = View.INVISIBLE
        }

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
            if(food.countType == CountType.COUNT_TYPE_NUMBER) food.count = fooddetail_count_number_editText.text.toString().toInt()
            food.push(cb)
        }
    }

    private fun getDatePickerDialog(date: LocalDate?, onDateSetListener: DatePickerDialog.OnDateSetListener): DatePickerDialog {
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
        return DatePickerDialog(this, onDateSetListener, year, month, day)

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

    private fun onCountTypeUpdate() {
        if (food.countType == CountType.COUNT_TYPE_PERCENTAGE) {
            fooddetail_counttype_label.text = resources.getText(R.string.switch_counttype_percentage)
            fooddetail_count_percentage_seekBar.visibility = View.VISIBLE
            fooddetail_count_number_editText.isEnabled = false
        } else {
            fooddetail_counttype_label.text = resources.getText(R.string.switch_counttype_number)
            fooddetail_count_percentage_seekBar.visibility = View.INVISIBLE
            fooddetail_count_number_editText.isEnabled = true
        }
    }

}
