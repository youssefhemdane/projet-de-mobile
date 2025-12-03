package tn.isilan.projet.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val ingredients: String,
    val instructions: String,
    val preparationTime: Int,
    val difficulty: String,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)