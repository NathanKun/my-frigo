package com.catprogrammer.myfrigo.model

import com.google.gson.JsonObject

interface CallbackActionInterface {
    fun onSuccess(data: JsonObject)
    fun onFailure()
}