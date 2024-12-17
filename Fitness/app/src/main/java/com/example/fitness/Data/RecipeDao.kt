package com.example.fitness.Data

import androidx.room.*
import com.example.fitness.Data.entities.Ingredient
import com.example.fitness.Data.entities.Recipe
import com.example.fitness.Data.entities.RecipeIngredientCrossRef
import com.example.fitness.Data.relations.RecipeWithIngredients

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredientCrossRef(ref: RecipeIngredientCrossRef)

    @Transaction
    @Query("SELECT * FROM recipes WHERE recipeId = :id")
    suspend fun getRecipeWithIngredients(id: Long): RecipeWithIngredients?

    @Transaction
    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipesWithIngredients(): List<RecipeWithIngredients>

    @Transaction
    @Query("SELECT * FROM ingredients")
    suspend fun getAllIngredients(): List<Ingredient>
}