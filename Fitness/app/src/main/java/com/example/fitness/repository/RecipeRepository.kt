package com.example.fitness.repository


import com.example.fitness.Data.RecipeDao
import com.example.fitness.Data.entities.Ingredient
import com.example.fitness.Data.entities.Recipe
import com.example.fitness.Data.entities.RecipeIngredientCrossRef
import com.example.fitness.Data.relations.RecipeWithIngredients

class RecipeRepository(private val recipeDao: RecipeDao) {

    suspend fun getAllRecipes(): List<Recipe> {
        // V reálné aplikaci lze přidat caching, filtraci atd.
        // Zde zjednodušeně pouze načteme všechno.
        val allRecipesWithIng = recipeDao.getAllRecipesWithIngredients()
        return allRecipesWithIng.map { it.recipe }
    }

    suspend fun getRecipeWithIngredients(id: Long): RecipeWithIngredients? {
        return recipeDao.getRecipeWithIngredients(id)
    }

    suspend fun getAllIngredients(): List<Ingredient> {
        // Zde byste museli mít metodu v DAO pro získání všech ingrediencí.
        // Přidejme si ji do RecipeDao:
        // @Query("SELECT * FROM ingredients")
        // suspend fun getAllIngredients(): List<Ingredient>
        return recipeDao.getAllIngredients()
    }

    suspend fun insertSampleData() {
        // Vložení ukázkových dat (10 receptů)

        // Recept 1: Bábovka
        val recipe1Id = recipeDao.insertRecipe(Recipe(name = "Bábovka", description = "Tradiční česká bábovka."))
        val recipe1Ingredients = listOf("Mouka", "Cukr", "Vejce", "Máslo", "Mléko", "Kypřící prášek")
        insertIngredientsForRecipe(recipe1Id, recipe1Ingredients)

        // Recept 2: Rajská omáčka
        val recipe2Id = recipeDao.insertRecipe(Recipe(name = "Rajská omáčka", description = "Tradiční česká rajská omáčka."))
        val recipe2Ingredients = listOf("Rajčata", "Cibule", "Česnek", "Olej", "Sůl", "Pepř", "Cukr")
        insertIngredientsForRecipe(recipe2Id, recipe2Ingredients)

        // Recept 3: Svíčková na smetaně
        val recipe3Id = recipeDao.insertRecipe(Recipe(name = "Svíčková na smetaně", description = "Klasický český pokrm se smetanovou omáčkou."))
        val recipe3Ingredients = listOf("Hovězí maso", "Cibule", "Mrkev", "Celer", "Řapíkatý celer", "Smetana", "Koření")
        insertIngredientsForRecipe(recipe3Id, recipe3Ingredients)

        // Recept 4: Knedlíky
        val recipe4Id = recipeDao.insertRecipe(Recipe(name = "Knedlíky", description = "Tradiční české houskové knedlíky."))
        val recipe4Ingredients = listOf("Housky", "Vejce", "Mouka", "Voda", "Sůl", "Droždí")
        insertIngredientsForRecipe(recipe4Id, recipe4Ingredients)

        // Recept 5: Guláš
        val recipe5Id = recipeDao.insertRecipe(Recipe(name = "Guláš", description = "Vydatný hovězí guláš s cibulí a paprikou."))
        val recipe5Ingredients = listOf("Hovězí maso", "Cibule", "Paprika", "Česnek", "Rajčata", "Sůl", "Pepř", "Kmín")
        insertIngredientsForRecipe(recipe5Id, recipe5Ingredients)

        // Recept 6: Bramborový salát
        val recipe6Id = recipeDao.insertRecipe(Recipe(name = "Bramborový salát", description = "Klasický český bramborový salát s majonézou."))
        val recipe6Ingredients = listOf("Brambory", "Cibule", "Majonéza", "Hořčice", "Sůl", "Pepř", "Ocet")
        insertIngredientsForRecipe(recipe6Id, recipe6Ingredients)

        // Recept 7: Palačinky
        val recipe7Id = recipeDao.insertRecipe(Recipe(name = "Palačinky", description = "Lehké a tenké palačinky s ovocem nebo marmeládou."))
        val recipe7Ingredients = listOf("Mouka", "Vejce", "Mléko", "Sůl", "Cukr", "Olej")
        insertIngredientsForRecipe(recipe7Id, recipe7Ingredients)

        // Recept 8: Smažený sýr
        val recipe8Id = recipeDao.insertRecipe(Recipe(name = "Smažený sýr", description = "Oblíbený pokrm smaženého sýra s bramborovou přílohou."))
        val recipe8Ingredients = listOf("Sýr eidam", "Mouka", "Vejce", "Strouhanka", "Olej", "Sůl", "Pepř")
        insertIngredientsForRecipe(recipe8Id, recipe8Ingredients)

        // Recept 9: Chléb
        val recipe9Id = recipeDao.insertRecipe(Recipe(name = "Chléb", description = "Čerstvý domácí chléb s droždím a solí."))
        val recipe9Ingredients = listOf("Mouka", "Voda", "Droždí", "Sůl", "Cukr")
        insertIngredientsForRecipe(recipe9Id, recipe9Ingredients)

        // Recept 10: Polévka svíčková
        val recipe10Id = recipeDao.insertRecipe(Recipe(name = "Polévka svíčková", description = "Lehká polévka inspirovaná omáčkou svíčková."))
        val recipe10Ingredients = listOf("Cibule", "Mrkev", "Celer", "Petržel", "Hovězí vývar", "Smetana", "Sůl", "Pepř")
        insertIngredientsForRecipe(recipe10Id, recipe10Ingredients)
    }

    private suspend fun insertIngredientsForRecipe(recipeId: Long, ingredients: List<String>) {
        for (ingredientName in ingredients) {
            val ingredientId = recipeDao.insertIngredient(Ingredient(name = ingredientName))
            recipeDao.insertRecipeIngredientCrossRef(RecipeIngredientCrossRef(recipeId, ingredientId))
        }
    }
}