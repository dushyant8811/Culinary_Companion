// database/AppDatabase.kt
package com.example.culinarycompanion.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import com.example.culinarycompanion.model.RecipeCollection

@Database(
    entities = [RecipeCollection::class],
    version = 1,
    exportSchema = false
)

@TypeConverters(Converters::class) // Add this

abstract class AppDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "culinary_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}