package com.example.culinarycompanion.database

import androidx.room.TypeConverter
import com.example.culinarycompanion.model.RecipeCategory
import java.util.*

class Converters {

    // String List Conversions
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        return try {
            value?.split("|||")
                ?.filter { it.isNotBlank() }
                ?.map { it.trim() }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        return list?.joinToString("|||") ?: ""
    }

    // Date/Long Conversions
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // RecipeCategory Conversions
    @TypeConverter
    fun fromRecipeCategory(value: RecipeCategory?): String? {
        return value?.name
    }

    @TypeConverter
    fun toRecipeCategory(value: String?): RecipeCategory? {
        return try {
            value?.let { RecipeCategory.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            RecipeCategory.ALL
        }
    }

    // Boolean Conversions (for compatibility)
    @TypeConverter
    fun fromBoolean(value: Boolean?): Int {
        return if (value == true) 1 else 0
    }

    @TypeConverter
    fun toBoolean(value: Int): Boolean {
        return value == 1
    }
}