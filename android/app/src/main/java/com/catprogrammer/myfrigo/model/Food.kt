package com.catprogrammer.myfrigo.model

import android.graphics.Bitmap
import android.util.Log
import com.catprogrammer.myfrigo.`interface`.Crudable
import com.catprogrammer.myfrigo.util.DateTimeUtil
import okhttp3.Callback
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime

class Food() : Crudable {

    var id: Int? = null
    var count: Int? = null
    var countType: CountType? = null
    var productionDate: LocalDate? = null
    var expirationDate: LocalDate? = null
    var isHistory: Boolean? = null
    var createdAt: LocalDateTime? = null
    var updatedAt: LocalDateTime? = null

    var name: String? = null
    var note: String? = null
    var barcode: String? = null
    var historyId: Int? = null
    var photoBitmap: Bitmap? = null
    var photoUrlSmall: String? = null
    var photoUrlLarge: String? = null
    var photoUrlOriginal: String? = null

    var isBarcodeDetected: Boolean = false
    var isOcrDetected: Boolean = false

    companion object Populator {

        fun populateFood(json: JSONObject): Food {
            val food = Food()

            food.id = json.getInt("id")
            food.count = json.getInt("count")
            food.countType = if (json.getInt("count_type") == 0) CountType.COUNT_TYPE_NUMBER else CountType.COUNT_TYPE_PERCENTAGE
            food.isHistory = json.getInt("is_history") != 0

            if (!json.isNull("name")) food.name = json.getString("name")
            if (!json.isNull("note")) food.note = json.getString("note")
            if (!json.isNull("barcode")) food.barcode = json.getString("barcode")
            if (!json.isNull("production_date")) food.productionDate = DateTimeUtil.parseDateString(json.getString("production_date"))
            if (!json.isNull("expiration_date")) food.expirationDate = DateTimeUtil.parseDateString(json.getString("expiration_date"))
            if (!json.isNull("created_at")) food.createdAt = DateTimeUtil.parseDateTimeString(json.getString("created_at"))
            if (!json.isNull("updated_at")) food.updatedAt = DateTimeUtil.parseDateTimeString(json.getString("updated_at"))
            if (!json.isNull("history_id")) food.historyId = json.getInt("history_id")

            if (!json.isNull("img1")) {
                val img1 = json.getJSONObject("img")
                food.photoUrlSmall = img1.getJSONObject("profile").getString("url")
                food.photoUrlLarge = img1.getJSONObject("large").getString("url")
                food.photoUrlOriginal = img1.getJSONObject("original").getString("url")
            }

            return Food()
        }
    }

    constructor(bitmap: Bitmap) : this() {
        this.photoBitmap = bitmap
    }

    override fun create(cb: Callback) {
        Log.d("detect", "create called")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun push(cb: Callback) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pull(cb: Callback) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(cb: Callback) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    enum class CountType {
        COUNT_TYPE_NUMBER, COUNT_TYPE_PERCENTAGE
    }
}