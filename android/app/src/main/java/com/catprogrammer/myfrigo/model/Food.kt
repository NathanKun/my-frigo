package com.catprogrammer.myfrigo.model

import android.graphics.Bitmap
import com.catprogrammer.myfrigo.`interface`.Crudable
import com.catprogrammer.myfrigo.util.DateTimeUtil
import com.catprogrammer.myfrigo.util.HttpUtil
import com.google.gson.JsonObject
import okhttp3.Callback
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Food() : Crudable {

    var id: Int = 0
    var count: Int = 100
    var countType: CountType = CountType.COUNT_TYPE_PERCENTAGE
    var productionDate: LocalDate? = null
    var expirationDate: LocalDate? = null
    var isHistory: Boolean = false
    var createdAt: LocalDateTime? = null
    var updatedAt: LocalDateTime? = null

    var name: String = ""
    var note: String = ""
    var barcode: String? = null
    var historyId: Int? = null
    var photoBitmap: Bitmap? = null
    var photoBitmapPath: String? = null
    var photoBitmapCompressed: Bitmap? = null
    var photoUrlSmall: String? = null
    var photoUrlLarge: String? = null
    var photoUrlOriginal: String? = null

    var isBarcodeDetected: Boolean = false
    var isOcr1Detected: Boolean = false
    var isOcr2Detected: Boolean = false
    var isOcr3Detected: Boolean = false

    private val httpUtil = HttpUtil.getInstance()

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    fun productionDateString(): String {
        return if (productionDate != null) productionDate!!.format(dateFormatter) else ""
    }

    fun expirationDateString(): String {
        return if (expirationDate != null) expirationDate!!.format(dateFormatter) else ""
    }

    fun uploadDateString(): String {
        return if (createdAt != null) createdAt!!.format(dateFormatter) else ""
    }

    companion object Populator {

        fun populateFood(json: JsonObject): Food {
            val food = Food()

            food.id = json.get("id").asInt
            food.count = json.get("count").asInt
            food.countType = if (json.get("count_type").asInt == 0) CountType.COUNT_TYPE_NUMBER else CountType.COUNT_TYPE_PERCENTAGE
            food.isHistory = json.get("is_history").asInt != 0

            if (!json.get("name").isJsonNull) food.name = json.get("name").asString
            if (!json.get("note").isJsonNull) food.note = json.get("note").asString
            if (!json.get("barcode").isJsonNull) food.barcode = json.get("barcode").asString
            if (!json.get("production_date").isJsonNull) food.productionDate = DateTimeUtil.parseDateString(json.get("production_date").asString)
            if (!json.get("expiration_date").isJsonNull) food.expirationDate = DateTimeUtil.parseDateString(json.get("expiration_date").asString)
            if (!json.get("created_at").isJsonNull) food.createdAt = DateTimeUtil.parseDateTimeString(json.get("created_at").asString)
            if (!json.get("updated_at").isJsonNull) food.updatedAt = DateTimeUtil.parseDateTimeString(json.get("updated_at").asString)
            if (!json.get("history_id").isJsonNull) food.historyId = json.get("history_id").asInt

            if (!json.get("img1").isJsonNull) {
                val img1 = json.get("img1").asJsonObject
                food.photoUrlSmall = HttpUtil.imgBaseUrl + img1.get("profile").asJsonObject.get("url").asString
                food.photoUrlLarge = HttpUtil.imgBaseUrl + img1.get("large").asJsonObject.get("url").asString
                food.photoUrlOriginal = HttpUtil.imgBaseUrl + img1.get("original").asJsonObject.get("url").asString
            }

            return food
        }
    }

    constructor(bitmap: Bitmap, compressedBitmap: Bitmap, path: String) : this() {
        this.photoBitmap = bitmap
        this.photoBitmapCompressed = compressedBitmap
        this.photoBitmapPath = path
    }

    override fun create(cb: Callback) {
        httpUtil.create(this, cb)
    }

    override fun push(cb: Callback) {
        httpUtil.push(this, cb)
    }

    override fun pull(cb: Callback) {
        httpUtil.pull(this, cb)
    }

    override fun delete(cb: Callback) {
        httpUtil.delete(this, cb)
    }

    override fun toString(): String {
        return "Food(id=$id, count=$count, countType=$countType, productionDate=$productionDate, expirationDate=$expirationDate, isHistory=$isHistory, createdAt=$createdAt, updatedAt=$updatedAt, name='$name', note='$note', barcode=$barcode, historyId=$historyId, photoBitmap=$photoBitmap, photoBitmapPath=$photoBitmapPath, photoBitmapCompressed=$photoBitmapCompressed, photoUrlSmall=$photoUrlSmall, photoUrlLarge=$photoUrlLarge, photoUrlOriginal=$photoUrlOriginal, isBarcodeDetected=$isBarcodeDetected, isOcr1Detected=$isOcr1Detected, isOcr2Detected=$isOcr2Detected, isOcr3Detected=$isOcr3Detected, productionDateString='${productionDateString()}', expirationDateString='${expirationDateString()}')"
    }
}