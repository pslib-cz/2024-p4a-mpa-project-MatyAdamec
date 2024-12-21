package com.example.fitness.Data.entities

import androidx.room.Entity

@Entity(
    tableName = "recipe_ingredient_cross_ref",
    primaryKeys = ["recipeId", "ingredientId"]
)
data class RecipeIngredientCrossRef(
    val recipeId: Long,
    val ingredientId: Long
)