package com.catprogrammer.myfrigo.util

import android.graphics.Bitmap
import android.util.Log
import com.catprogrammer.myfrigo.model.Food
import okhttp3.*
import java.io.ByteArrayOutputStream

class HttpUtil {
    private val client = OkHttpClient()

    companion object {
        private var singleton: HttpUtil? = null
        private const val baseUrl = "https://myfrigo.catprogrammer.com/api/foods"

        fun getInstance(): HttpUtil {
            if (singleton == null)
                singleton = HttpUtil()
            return singleton!!
        }
    }

    fun getAllFoods(cb: Callback) {
        asynGet(cb, baseUrl)
    }

    fun create(food: Food, cb: Callback) {
        try {

            //Compress Image
            val bos = ByteArrayOutputStream()
            food.photoBitmapCompressed!!.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            food.photoBitmapCompressed!!.recycle()
            food.photoBitmap!!.recycle()

            Log.d("upload img size", bos.size().toString())

            val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("count", "100")
                    .addFormDataPart("count_type", "1")
                    .addFormDataPart("img1", "photo.jpeg",
                            RequestBody.create(MediaType.parse("image/jpeg"), bos.toByteArray()))

            if (food.barcode != null) {
                requestBody.addFormDataPart("barcode", food.barcode!!)
            }
            if (food.productionDate != null) {
                requestBody.addFormDataPart("production_date", food.productionDate!!.toString())
            }
            if (food.expirationDate != null) {
                requestBody.addFormDataPart("expiration_date", food.expirationDate!!.toString())
            }


            val request = Request.Builder()
                    .url(baseUrl)
                    .addHeader("Authorization", Const.AUTH)
                    .post(requestBody.build())
                    .build()

            client.newCall(request).enqueue(cb)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun push(food: Food, cb: Callback) {
        val params = HashMap<String, String>()
        params["name"] = food.name
        params["note"] = food.note
        params["count"] = food.count.toString()
        params["count_type"] = food.countType.value.toString()
        if (food.productionDate != null) params["production_date"] = food.productionDate.toString()
        if (food.expirationDate != null) params["expiration_date"] = food.expirationDate.toString()

        asynPatch(cb, "$baseUrl/${food.id}", params)
    }

    fun pull(food: Food, cb: Callback) {
        asynGet(cb, "$baseUrl/${food.id}")
    }

    fun delete(food: Food, cb: Callback) {
        asynDelete(cb, "$baseUrl/${food.id}")
    }


    /**
     * general method for make a asynchronous POST
     * @param callback  callback
     * @param url       url for adding to the base url
     */
    private fun asynGet(callback: Callback, url: String) {
        try {

            val request = Request.Builder()
                    .url(baseUrl + url)
                    .addHeader("Authorization", Const.AUTH)
                    .build()

            client.newCall(request).enqueue(callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * general method for make a asynchronous POST
     * @param callback  callback
     * @param url       url for adding to the base url
     * @param params    parameters for POST
     */
    private fun asynPost(callback: Callback, url: String, params: Map<String, String>) {
        try {
            val it = params.entries.iterator()

            val builder = FormBody.Builder()
            while (it.hasNext()) {
                val entry = it.next() as Map.Entry<*, *>
                builder.add(entry.key as String, entry.value as String)
            }

            val request = Request.Builder()
                    .url(baseUrl + url)
                    .addHeader("Authorization", Const.AUTH)
                    .post(builder.build())
                    .build()

            client.newCall(request).enqueue(callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * general method for make a asynchronous PATCH
     * @param callback  callback
     * @param url       url for adding to the base url
     * @param params    parameters for PATCH
     */
    private fun asynPatch(callback: Callback, url: String, params: Map<String, String>) {
        params as HashMap
        params["_method"] = "patch"
        asynPost(callback, url, params)
    }


    /**
     * general method for make a asynchronous DELETE
     * @param callback  callback
     * @param url       url for adding to the base url
     */
    private fun asynDelete(callback: Callback, url: String) {
        val params = HashMap<String, String>()
        params["_method"] = "delete"
        asynPost(callback, url, params)
    }
}