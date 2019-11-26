package com.agileburo.anytype.persistence.util

import androidx.room.TypeConverter
import com.agileburo.anytype.persistence.model.AccountTable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object Converters {

    @TypeConverter
    @JvmStatic
    fun fromImageSizeList(sizes: List<AccountTable.Size>): String {
        return Gson().toJson(sizes)
    }

    @TypeConverter
    @JvmStatic
    fun toImageSizeList(string: String): List<AccountTable.Size> {
        val type = object : TypeToken<List<AccountTable.Size>>() {}.type
        return Gson().fromJson(string, type)
    }

}