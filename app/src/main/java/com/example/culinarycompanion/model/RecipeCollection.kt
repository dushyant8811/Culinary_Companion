package com.example.culinarycompanion.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.culinarycompanion.database.Converters
import com.google.firebase.firestore.Exclude

@Entity(
    tableName = "recipecollection",
    indices = [
        Index(value = ["name"], unique = false),
        Index(value = ["createdAt"], unique = false)
    ]
)
@TypeConverters(Converters::class)
data class RecipeCollection(
    @PrimaryKey
    val id: String = "",

    val name: String = "",

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    val recipeIds: List<String> = emptyList(),

    @ColumnInfo(defaultValue = "")
    val ownerId: String = ""
) {
    @Exclude
    var documentId: String = ""

    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "createdAt" to createdAt,
            "recipeIds" to recipeIds,
            "ownerId" to ownerId
        )
    }

    companion object {
        fun fromFirestore(
            documentId: String,
            data: Map<String, Any>
        ): RecipeCollection {
            return RecipeCollection(
                id = documentId,
                name = data["name"] as? String ?: "",
                createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
                recipeIds = data["recipeIds"] as? List<String> ?: emptyList(),
                ownerId = data["ownerId"] as? String ?: ""
            ).apply {
                this.documentId = documentId
            }
        }
    }

    fun containsRecipe(recipeId: String): Boolean = recipeIds.contains(recipeId)
}
