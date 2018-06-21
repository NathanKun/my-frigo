package com.catprogrammer.myfrigo.model

import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

abstract class GeneralCallback : Callback, CallbackActionInterface {
    override fun onFailure(call: Call?, e: IOException?) {
        Log.d("HttpUtil", "onFailure: ${e?.message}")
        e?.printStackTrace()
        onFailure()
    }

    override fun onResponse(call: Call?, response: Response?) {
        if (response == null) {
            Log.d("HttpUtil", "response == null")
            onFailure()
        } else if (!response.isSuccessful) {
            Log.d("HttpUtil", "response not successful")
            Log.d("HttpUtil", "response code: ${response.code()}")
            onFailure()
        } else {
            val body = response.body()
            if (body == null) {
                Log.d("HttpUtil", "body == null")
                onFailure()
            } else {
                val bodyStr = body.string()
                Log.d("HttpUtil", bodyStr)
                val json: JsonObject = JsonParser().parse(bodyStr).asJsonObject

                if (!json.get("success").asBoolean) {
                    Log.d("HttpUtil", "success = false")
                    onFailure()
                } else { // success = true
                    Log.d("HttpUtil", "success = true")
                    val data = json.get("data").asJsonObject
                    onSuccess(data)
                }
                return
            } // if json.success is true end
        } // if body not null end
    } // if response isSuccessful end

}