package com.example.fitness.Data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val recipeId: Long = 0,
    val name: String,
    val description: String
)