package com.catprogrammer.myfrigo.model

enum class CountType(val value: Int) {
    COUNT_TYPE_NUMBER(0), COUNT_TYPE_PERCENTAGE(1);

    companion object {
        fun valueOf(value: Int): CountType? {
            return if (value == 0) COUNT_TYPE_NUMBER else if (value == 1) COUNT_TYPE_PERCENTAGE else null
        }
    }


}