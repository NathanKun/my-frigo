package com.catprogrammer.myfrigo.util

import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class HttpUtil {
    private val client = OkHttpClient()

    companion object {
        private var singleton: HttpUtil? = null
        private val baseUrl = "https://bingbin.io/index.php/"

        fun getInstance(): HttpUtil? {
            if (singleton == null)
                singleton = HttpUtil()
            return singleton
        }
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
     * @param params    parameters for POST
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
     * @param params    parameters for POST
     */
    private fun asynPatch(callback: Callback, url: String) {
        val params = HashMap<String, String>()
        params["_method"] = "delete"
        asynPost(callback, url, params)
    }
}