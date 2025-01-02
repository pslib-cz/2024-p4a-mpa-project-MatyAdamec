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

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Transaction
    @Query("SELECT * FROM recipes WHERE recipeId = :id")
    suspend fun getRecipeWithIngredients(id: Long): RecipeWithIngredients?

    @Transaction
    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipesWithIngredients(): List<RecipeWithIngredients>

    @Transaction
    @Query("SELECT * FROM ingredients")
    suspend fun getAllIngredients(): List<Ingredient>

    @Transaction
    @Query("SELECT * FROM recipes WHERE name LIKE :searchQuery")
    suspend fun searchRecipesWithIngredients(searchQuery: String): List<RecipeWithIngredients>

    @Transaction
    @Query("DELETE FROM recipes WHERE recipeId = :id")
    suspend fun deleteRecipeById(id: Long)

    @Query("DELETE FROM recipe_ingredient_cross_ref WHERE recipeId = :id")
    suspend fun deleteRecipeIngredientCrossRefs(id: Long)

    @Query("SELECT * FROM ingredients WHERE name = :name LIMIT 1")
    suspend fun getIngredientByName(name: String): Ingredient?

    @Transaction
    @Query("""
        SELECT DISTINCT recipes.* FROM recipes
        INNER JOIN recipe_ingredient_cross_ref ON recipes.recipeId = recipe_ingredient_cross_ref.recipeId
        INNER JOIN ingredients ON recipe_ingredient_cross_ref.ingredientId = ingredients.ingredientId
        WHERE ingredients.name IN (:ingredientNames)
        GROUP BY recipes.recipeId
        HAVING COUNT(DISTINCT ingredients.name) = :ingredientCount
    """)
    suspend fun filterRecipesByIngredients(ingredientNames: List<String>, ingredientCount: Int): List<RecipeWithIngredients>

}