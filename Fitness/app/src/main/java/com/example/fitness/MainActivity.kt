package com.example.fitness

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.fitness.Data.AppDatabase
import com.example.fitness.Data.entities.Ingredient
import com.example.fitness.Data.entities.Recipe
import com.example.fitness.Data.entities.RecipeIngredientCrossRef
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Nastavíme layout (musí být vytvořen v res/layout/)
        setContentView(R.layout.activity_main)

        // Inicializace databáze
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "recipes-db"
        ).build()

        // Ukázka použití v coroutine
        lifecycleScope.launch {
            val recipeId = db.recipeDao()
                .insertRecipe(Recipe(name = "Bábovka", description = "Tradiční bábovka"))
            val flourId = db.recipeDao().insertIngredient(Ingredient(name = "Mouka"))
            val sugarId = db.recipeDao().insertIngredient(Ingredient(name = "Cukr"))
            val eggsId = db.recipeDao().insertIngredient(Ingredient(name = "Vejce"))

            // Propojíme recept s ingrediencemi (N:M)
            db.recipeDao()
                .insertRecipeIngredientCrossRef(RecipeIngredientCrossRef(recipeId, flourId))
            db.recipeDao()
                .insertRecipeIngredientCrossRef(RecipeIngredientCrossRef(recipeId, sugarId))
            db.recipeDao()
                .insertRecipeIngredientCrossRef(RecipeIngredientCrossRef(recipeId, eggsId))

            // Nyní můžeme načíst recept s ingrediencemi
            val recipeWithIngredients = db.recipeDao().getRecipeWithIngredients(recipeId)
            // Např.:
            // Log.d("MainActivity", "Recept: ${recipeWithIngredients?.recipe?.name}")
            // Log.d("MainActivity", "Ingredience: ${recipeWithIngredients?.ingredients?.joinToString { it.name }}")
        }
    }
}