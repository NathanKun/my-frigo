package com.catprogrammer.myfrigo.model

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import com.catprogrammer.myfrigo.`interface`.Crudable
import com.catprogrammer.myfrigo.util.DateTimeUtil
import com.catprogrammer.myfrigo.util.HttpUtil
import com.google.gson.JsonObject
import okhttp3.Callback
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Food() : Crudable, Parcelable {

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

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        count = parcel.readInt()
        countType = CountType.valueOf(parcel.readInt())!!
        isHistory = parcel.readByte() != 0.toByte()
        name = parcel.readString()
        note = parcel.readString()
        barcode = parcel.readString()
        historyId = parcel.readValue(Int::class.java.classLoader) as? Int
        photoBitmap = parcel.readParcelable(Bitmap::class.java.classLoader)
        photoBitmapPath = parcel.readString()
        photoBitmapCompressed = parcel.readParcelable(Bitmap::class.java.classLoader)
        photoUrlSmall = parcel.readString()
        photoUrlLarge = parcel.readString()
        photoUrlOriginal = parcel.readString()
        isBarcodeDetected = parcel.readByte() != 0.toByte()
        isOcr1Detected = parcel.readByte() != 0.toByte()
        isOcr2Detected = parcel.readByte() != 0.toByte()
        isOcr3Detected = parcel.readByte() != 0.toByte()

        var year = parcel.readInt()
        var month = parcel.readInt()
        var day = parcel.readInt()
        var hour = parcel.readInt()
        var minuit = parcel.readInt()
        var second = parcel.readInt()
        var nano = parcel.readInt()
        if (year != -1) {
            createdAt = LocalDateTime.of(year, month, day, hour, minuit, second, nano)
        }

        year = parcel.readInt()
        month = parcel.readInt()
        day = parcel.readInt()
        hour = parcel.readInt()
        minuit = parcel.readInt()
        second = parcel.readInt()
        nano = parcel.readInt()
        if (year != -1) {
            updatedAt = LocalDateTime.of(year, month, day, hour, minuit, second, nano)
        }

        year = parcel.readInt()
        month = parcel.readInt()
        day = parcel.readInt()
        if (year != -1) {
            productionDate = LocalDate.of(year, month, day)
        }

        year = parcel.readInt()
        month = parcel.readInt()
        day = parcel.readInt()
        if (year != -1) {
            expirationDate = LocalDate.of(year, month, day)
        }

    }

    fun productionDateString(): String {
        return if (productionDate != null) productionDate!!.format(dateFormatter) else ""
    }

    fun expirationDateString(): String {
        return if (expirationDate != null) expirationDate!!.format(dateFormatter) else ""
    }

    fun uploadDateString(): String {
        return if (createdAt != null) createdAt!!.format(dateFormatter) else ""
    }

    companion object CREATOR : Parcelable.Creator<Food> {

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

        override fun createFromParcel(parcel: Parcel): Food {
            return Food(parcel)
        }

        override fun newArray(size: Int): Array<Food?> {
            return arrayOfNulls(size)
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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(count)
        parcel.writeInt(countType.value)
        parcel.writeByte(if (isHistory) 1 else 0)
        parcel.writeString(name)
        parcel.writeString(note)
        parcel.writeString(barcode)
        parcel.writeValue(historyId)
        parcel.writeParcelable(photoBitmap, flags)
        parcel.writeString(photoBitmapPath)
        parcel.writeParcelable(photoBitmapCompressed, flags)
        parcel.writeString(photoUrlSmall)
        parcel.writeString(photoUrlLarge)
        parcel.writeString(photoUrlOriginal)
        parcel.writeByte(if (isBarcodeDetected) 1 else 0)
        parcel.writeByte(if (isOcr1Detected) 1 else 0)
        parcel.writeByte(if (isOcr2Detected) 1 else 0)
        parcel.writeByte(if (isOcr3Detected) 1 else 0)
        if (createdAt != null) {
            parcel.writeInt(createdAt!!.year)
            parcel.writeInt(createdAt!!.monthValue)
            parcel.writeInt(createdAt!!.dayOfMonth)
            parcel.writeInt(createdAt!!.hour)
            parcel.writeInt(createdAt!!.minute)
            parcel.writeInt(createdAt!!.second)
            parcel.writeInt(createdAt!!.nano)
        } else {
            repeat(7) {parcel.writeInt(-1)}
        }
        if (updatedAt != null) {
            parcel.writeInt(updatedAt!!.year)
            parcel.writeInt(updatedAt!!.monthValue)
            parcel.writeInt(updatedAt!!.dayOfMonth)
            parcel.writeInt(updatedAt!!.hour)
            parcel.writeInt(updatedAt!!.minute)
            parcel.writeInt(updatedAt!!.second)
            parcel.writeInt(updatedAt!!.nano)
        } else {
            repeat(7) {parcel.writeInt(-1)}
        }
        if (productionDate != null) {
            parcel.writeInt(productionDate!!.year)
            parcel.writeInt(productionDate!!.monthValue)
            parcel.writeInt(productionDate!!.dayOfMonth)
        } else {
            repeat(3) {parcel.writeInt(-1)}
        }
        if (expirationDate != null) {
            parcel.writeInt(expirationDate!!.year)
            parcel.writeInt(expirationDate!!.monthValue)
            parcel.writeInt(expirationDate!!.dayOfMonth)
        } else {
            repeat(3) {parcel.writeInt(-1)}
        }
    }

    override fun describeContents(): Int {
        return 0
    }
}