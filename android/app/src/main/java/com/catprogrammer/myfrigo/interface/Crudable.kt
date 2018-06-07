package com.catprogrammer.myfrigo.`interface`

import okhttp3.Callback

interface Crudable {
    fun create(cb: Callback)
    fun push(cb: Callback)
    fun pull(cb: Callback)
    fun delete(cb: Callback)
}