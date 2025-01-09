package com.example.hrdepartment.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromRespondedPeopleList(list: List<String>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toRespondedPeopleList(data: String?): List<String>? {
        if (data == null) {
            return emptyList()
        }
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(data, listType)
    }
}
