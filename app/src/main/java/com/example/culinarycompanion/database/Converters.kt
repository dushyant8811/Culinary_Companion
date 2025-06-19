package com.example.culinarycompanion.database

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<Int> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",").mapNotNull { it.toIntOrNull() }
    }

    @TypeConverter
    fun fromList(list: List<Int>?): String {
        return list?.joinToString(",") ?: ""
    }

    // Add these new converters for List<String>
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",")
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }
}