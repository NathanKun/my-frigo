package com.catprogrammer.myfrigo.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimeUtil {
    companion object {
        fun parseDateString(string: String): LocalDate {
            val df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            return LocalDate.parse(string, df)
        }

        fun parseDateTimeString(string: String): LocalDateTime {
            val df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            return LocalDateTime.parse(string, df)
        }
    }
}
