package com.example.fitness.repository


import android.util.Log
import com.example.fitness.Data.RecipeDao
import com.example.fitness.Data.entities.Ingredient
import com.example.fitness.Data.entities.Recipe
import com.example.fitness.Data.entities.RecipeIngredientCrossRef
import com.example.fitness.Data.relations.RecipeWithIngredients

class RecipeRepository(private val recipeDao: RecipeDao) {

    suspend fun getAllRecipes(): List<Recipe> {
        val allRecipesWithIng = recipeDao.getAllRecipesWithIngredients()
        return allRecipesWithIng.map { it.recipe }
    }

    suspend fun getRecipeWithIngredients(id: Long): RecipeWithIngredients? {
        return recipeDao.getRecipeWithIngredients(id)
    }

    suspend fun getAllIngredients(): List<Ingredient> {
        val allIngredients = recipeDao.getAllIngredients()
        return allIngredients.distinctBy { it.name }
    }

    suspend fun deleteRecipe(recipeId: Long) {
        recipeDao.deleteRecipeIngredientCrossRefs(recipeId)
        recipeDao.deleteRecipeById(recipeId)
    }

    suspend fun filterRecipesByIngredients(ingredientNames: List<String>): List<Recipe> {
        if (ingredientNames.isEmpty()) {
            return getAllRecipes()
        }
        val recipesWithIngredients = recipeDao.filterRecipesByIngredients(ingredientNames, ingredientNames.size)
        return recipesWithIngredients.map { it.recipe }
    }

    suspend fun searchRecipes(query: String): List<Recipe> {
        val searchQuery = "%$query%"
        val recipesWithIngredients = recipeDao.searchRecipesWithIngredients(searchQuery)
        return recipesWithIngredients.map { it.recipe }
    }

    suspend fun filterAndSearchRecipes(ingredientNames: List<String>, query: String?): List<Recipe> {
        if (ingredientNames.isEmpty() && query.isNullOrEmpty()) {
            return getAllRecipes()
        }
        if (!query.isNullOrEmpty() && ingredientNames.isEmpty()) {
            return searchRecipes(query)
        }

        if (query.isNullOrEmpty() && ingredientNames.isNotEmpty()) {
            return filterRecipesByIngredients(ingredientNames)
        }

        if (!query.isNullOrEmpty() && ingredientNames.isNotEmpty()) {
            val ingredientFilteredRecipes = filterRecipesByIngredients(ingredientNames)
            val searchFilteredRecipes = searchRecipes(query)

            val combinedRecipes = ingredientFilteredRecipes.intersect(searchFilteredRecipes).toList()

            return combinedRecipes
        }

        return emptyList()
    }

    suspend fun insertRecipe(recipe: Recipe, ingredients: List<String>) {
        val recipeId = recipeDao.insertRecipe(recipe)

        for (ingredientName in ingredients) {
            val existingIngredients = recipeDao.getAllIngredients().filter { it.name.equals(ingredientName, ignoreCase = true) }
            val ingredientId = if (existingIngredients.isNotEmpty()) {
                existingIngredients.first().ingredientId
            } else {
                recipeDao.insertIngredient(Ingredient(name = ingredientName))
            }

            recipeDao.insertRecipeIngredientCrossRef(RecipeIngredientCrossRef(recipeId, ingredientId))
        }
    }
    suspend fun updateRecipe(recipe: Recipe, ingredients: List<String>) {
        recipeDao.updateRecipe(recipe)

        recipeDao.deleteRecipeIngredientCrossRefs(recipe.recipeId)

        for (ingredientName in ingredients) {
            val existingIngredients = recipeDao.getAllIngredients().filter { it.name.equals(ingredientName, ignoreCase = true) }
            val ingredientId = if (existingIngredients.isNotEmpty()) {
                existingIngredients.first().ingredientId
            } else {
                recipeDao.insertIngredient(Ingredient(name = ingredientName))
            }

            recipeDao.insertRecipeIngredientCrossRef(RecipeIngredientCrossRef(recipe.recipeId, ingredientId))
        }
    }

    suspend fun insertSampleData() {
        // Recipe 1: Bábovka
        val recipe1Id = recipeDao.insertRecipe(Recipe(name = "Bábovka", description = "Traditional Czech bábovka."))
        val recipe1Ingredients = listOf("Flour", "Sugar", "Eggs", "Butter", "Milk", "Baking powder")
        insertIngredientsForRecipe(recipe1Id, recipe1Ingredients)

        // Recipe 2: Tomato Sauce
        val recipe2Id = recipeDao.insertRecipe(Recipe(name = "Tomato Sauce", description = "Traditional Czech tomato sauce."))
        val recipe2Ingredients = listOf("Tomatoes", "Onion", "Garlic", "Oil", "Salt", "Pepper", "Sugar")
        insertIngredientsForRecipe(recipe2Id, recipe2Ingredients)

        // Recipe 3: Svíčková na smetaně
        val recipe3Id = recipeDao.insertRecipe(Recipe(name = "Svíčková na smetaně", description = "Classic Czech dish with creamy sauce."))
        val recipe3Ingredients = listOf("Beef", "Onion", "Carrot", "Celery", "Celery stalk", "Cream", "Spices")
        insertIngredientsForRecipe(recipe3Id, recipe3Ingredients)

        // Recipe 4: Dumplings
        val recipe4Id = recipeDao.insertRecipe(Recipe(name = "Dumplings", description = "Traditional Czech bread dumplings."))
        val recipe4Ingredients = listOf("Bread rolls", "Eggs", "Flour", "Water", "Salt", "Yeast")
        insertIngredientsForRecipe(recipe4Id, recipe4Ingredients)

        // Recipe 5: Goulash
        val recipe5Id = recipeDao.insertRecipe(Recipe(name = "Goulash", description = "Hearty beef goulash with onions and paprika."))
        val recipe5Ingredients = listOf("Beef", "Onion", "Paprika", "Garlic", "Tomatoes", "Salt", "Pepper", "Caraway")
        insertIngredientsForRecipe(recipe5Id, recipe5Ingredients)

        // Recipe 6: Potato Salad
        val recipe6Id = recipeDao.insertRecipe(Recipe(name = "Potato Salad", description = "Classic Czech potato salad with mayonnaise."))
        val recipe6Ingredients = listOf("Potatoes", "Onion", "Mayonnaise", "Mustard", "Salt", "Pepper", "Vinegar")
        insertIngredientsForRecipe(recipe6Id, recipe6Ingredients)

        // Recipe 7: Pancakes
        val recipe7Id = recipeDao.insertRecipe(Recipe(name = "Pancakes", description = "Light and thin pancakes with fruit or jam."))
        val recipe7Ingredients = listOf("Flour", "Eggs", "Milk", "Salt", "Sugar", "Oil")
        insertIngredientsForRecipe(recipe7Id, recipe7Ingredients)

        // Recipe 8: Fried Cheese
        val recipe8Id = recipeDao.insertRecipe(Recipe(name = "Fried Cheese", description = "Popular dish of fried cheese with potato side."))
        val recipe8Ingredients = listOf("Edam cheese", "Flour", "Eggs", "Breadcrumbs", "Oil", "Salt", "Pepper")
        insertIngredientsForRecipe(recipe8Id, recipe8Ingredients)

        // Recipe 9: Bread
        val recipe9Id = recipeDao.insertRecipe(Recipe(name = "Bread", description = "Fresh homemade bread with yeast and salt."))
        val recipe9Ingredients = listOf("Flour", "Water", "Yeast", "Salt", "Sugar")
        insertIngredientsForRecipe(recipe9Id, recipe9Ingredients)

        // Recipe 10: Svíčková Soup
        val recipe10Id = recipeDao.insertRecipe(Recipe(name = "Svíčková Soup", description = "Light soup inspired by svíčková sauce."))
        val recipe10Ingredients = listOf("Onion", "Carrot", "Celery", "Parsley", "Beef broth", "Cream", "Salt", "Pepper")
        insertIngredientsForRecipe(recipe10Id, recipe10Ingredients)

        // Recipe 11: Schnitzel with Potato Salad
        val recipe11Id = recipeDao.insertRecipe(Recipe(name = "Schnitzel with Potato Salad", description = "Classic Czech schnitzel served with potato salad."))
        val recipe11Ingredients = listOf("Pork", "Eggs", "Flour", "Breadcrumbs", "Oil", "Salt", "Pepper", "Potatoes", "Mayonnaise", "Onion")
        insertIngredientsForRecipe(recipe11Id, recipe11Ingredients)

        // Recipe 12: Knedlo zelo vepřo
        val recipe12Id = recipeDao.insertRecipe(Recipe(name = "Knedlo zelo vepřo", description = "Traditional Czech dish of dumplings, cabbage, and pork."))
        val recipe12Ingredients = listOf("Pork", "Dumplings", "Sauerkraut", "Onion", "Oil", "Salt", "Pepper", "Spices")
        insertIngredientsForRecipe(recipe12Id, recipe12Ingredients)

        // Recipe 13: Potato Pancakes
        val recipe13Id = recipeDao.insertRecipe(Recipe(name = "Potato Pancakes", description = "Delicious pancakes made from grated potatoes."))
        val recipe13Ingredients = listOf("Potatoes", "Eggs", "Flour", "Onion", "Garlic", "Salt", "Pepper", "Oil")
        insertIngredientsForRecipe(recipe13Id, recipe13Ingredients)

        // Recipe 14: Strudel
        val recipe14Id = recipeDao.insertRecipe(Recipe(name = "Strudel", description = "Traditional Czech fruit strudel with apples and cinnamon."))
        val recipe14Ingredients = listOf("Puff pastry", "Apples", "Sugar", "Cinnamon", "Raisins", "Oil", "Yeast")
        insertIngredientsForRecipe(recipe14Id, recipe14Ingredients)

        // Recipe 15: Delicate Cake with Quark
        val recipe15Id = recipeDao.insertRecipe(Recipe(name = "Delicate Cake with Quark", description = "Delicious cake with a delicate quark filling."))
        val recipe15Ingredients = listOf("Flour", "Butter", "Sugar", "Eggs", "Quark", "Vanilla", "Lemon zest")
        insertIngredientsForRecipe(recipe15Id, recipe15Ingredients)

        // Recipe 16: Pork with Mushrooms
        val recipe16Id = recipeDao.insertRecipe(Recipe(name = "Pork with Mushrooms", description = "Pork stewed with mushrooms and onions."))
        val recipe16Ingredients = listOf("Pork", "Mushrooms", "Onion", "Garlic", "Cream", "Salt", "Pepper", "Oil")
        insertIngredientsForRecipe(recipe16Id, recipe16Ingredients)

        // Recipe 17: Fried Mushrooms
        val recipe17Id = recipeDao.insertRecipe(Recipe(name = "Fried Mushrooms", description = "Crispy fried mushrooms with garlic and herbs."))
        val recipe17Ingredients = listOf("Mushrooms", "Eggs", "Breadcrumbs", "Garlic", "Parsley", "Salt", "Pepper", "Oil")
        insertIngredientsForRecipe(recipe17Id, recipe17Ingredients)

        // Recipe 18: Halušky with Bryndza
        val recipe18Id = recipeDao.insertRecipe(Recipe(name = "Halušky with Bryndza", description = "Traditional Slovak halušky served with bryndza."))
        val recipe18Ingredients = listOf("Potatoes", "Flour", "Eggs", "Salt", "Bryndza", "Bacon")
        insertIngredientsForRecipe(recipe18Id, recipe18Ingredients)

        // Recipe 19: Potato Dumplings with Smoked Meat
        val recipe19Id = recipeDao.insertRecipe(Recipe(name = "Potato Dumplings with Smoked Meat", description = "Traditional potato dumplings served with smoked meat and sauerkraut."))
        val recipe19Ingredients = listOf("Potatoes", "Flour", "Eggs", "Salt", "Smoked meat", "Sauerkraut")
        insertIngredientsForRecipe(recipe19Id, recipe19Ingredients)

        // Recipe 20: Pork with Paprika
        val recipe20Id = recipeDao.insertRecipe(Recipe(name = "Pork with Paprika", description = "Delicious pork cooked in a spicy paprika sauce."))
        val recipe20Ingredients = listOf("Pork", "Paprika", "Onion", "Garlic", "Tomatoes", "Cream", "Salt", "Pepper", "Oil")
        insertIngredientsForRecipe(recipe20Id, recipe20Ingredients)

        // Recipe 21: Sauerkraut with Sausage
        val recipe21Id = recipeDao.insertRecipe(Recipe(name = "Sauerkraut with Sausage", description = "Simple dish of sauerkraut and sausage."))
        val recipe21Ingredients = listOf("Sauerkraut", "Sausage", "Onion", "Garlic", "Salt", "Pepper", "Oil")
        insertIngredientsForRecipe(recipe21Id, recipe21Ingredients)

        // Recipe 22: Garlic Soup
        val recipe22Id = recipeDao.insertRecipe(Recipe(name = "Garlic Soup", description = "Warm garlic soup with bread and cheese."))
        val recipe22Ingredients = listOf("Garlic", "Water", "Beef broth", "Onion", "Salt", "Pepper", "Bread", "Cheese")
        insertIngredientsForRecipe(recipe22Id, recipe22Ingredients)

        // Recipe 23: Moravian Sparrow
        val recipe23Id = recipeDao.insertRecipe(Recipe(name = "Moravian Sparrow", description = "Pork roasted with dumplings and sauerkraut."))
        val recipe23Ingredients = listOf("Pork", "Dumplings", "Sauerkraut", "Onion", "Oil", "Salt", "Pepper")
        insertIngredientsForRecipe(recipe23Id, recipe23Ingredients)

        // Recipe 24: Plum Dumplings
        val recipe24Id = recipeDao.insertRecipe(Recipe(name = "Plum Dumplings", description = "Sweet dumplings filled with plums, sprinkled with sugar and cinnamon."))
        val recipe24Ingredients = listOf("Flour", "Eggs", "Water", "Salt", "Plums", "Sugar", "Cinnamon", "Oil")
        insertIngredientsForRecipe(recipe24Id, recipe24Ingredients)

        // Recipe 25: Tomato Soup
        val recipe25Id = recipeDao.insertRecipe(Recipe(name = "Tomato Soup", description = "Light tomato soup with meat and herbs."))
        val recipe25Ingredients = listOf("Tomatoes", "Onion", "Garlic", "Beef", "Cream", "Salt", "Pepper", "Oil", "Herbs")
        insertIngredientsForRecipe(recipe25Id, recipe25Ingredients)

        // Recipe 26: Onion Soup
        val recipe26Id = recipeDao.insertRecipe(Recipe(name = "Onion Soup", description = "Traditional Czech onion soup with croutons and cheese."))
        val recipe26Ingredients = listOf("Onion", "Butter", "Water", "Beef broth", "Salt", "Pepper", "Bread", "Cheese")
        insertIngredientsForRecipe(recipe26Id, recipe26Ingredients)

        // Recipe 27: Roast Duck with Red Cabbage
        val recipe27Id = recipeDao.insertRecipe(Recipe(name = "Roast Duck with Red Cabbage", description = "Delicious roast duck served with red cabbage and dumplings."))
        val recipe27Ingredients = listOf("Duck", "Red cabbage", "Onion", "Apples", "Salt", "Pepper", "Caraway", "Oil")
        insertIngredientsForRecipe(recipe27Id, recipe27Ingredients)

        // Recipe 28: Beer Goulash
        val recipe28Id = recipeDao.insertRecipe(Recipe(name = "Beer Goulash", description = "Goulash prepared with beer instead of water, for a richer flavor."))
        val recipe28Ingredients = listOf("Beef", "Onion", "Paprika", "Garlic", "Beer", "Tomatoes", "Salt", "Pepper", "Caraway", "Oil")
        insertIngredientsForRecipe(recipe28Id, recipe28Ingredients)

        // Recipe 29: Grilled Lamb Chops
        val recipe29Id = recipeDao.insertRecipe(Recipe(name = "Grilled Lamb Chops", description = "Tender lamb chops marinated and grilled to perfection."))
        val recipe29Ingredients = listOf("Lamb chops", "Garlic", "Rosemary", "Lemon juice", "Oil", "Salt", "Pepper")
        insertIngredientsForRecipe(recipe29Id, recipe29Ingredients)

        // Recipe 30: Quark Cake
        val recipe30Id = recipeDao.insertRecipe(Recipe(name = "Quark Cake", description = "Sweet cake with quark filling and fruit."))
        val recipe30Ingredients = listOf("Flour", "Butter", "Eggs", "Sugar", "Quark", "Vanilla", "Lemon juice", "Fruit")
        insertIngredientsForRecipe(recipe30Id, recipe30Ingredients)
    }


    private suspend fun insertIngredientsForRecipe(recipeId: Long, ingredients: List<String>) {

        for (ingredientName in ingredients) {
            val ingredientId = recipeDao.getIngredientByName(ingredientName)?.ingredientId ?: recipeDao.insertIngredient(Ingredient(name = ingredientName))
            recipeDao.insertRecipeIngredientCrossRef(RecipeIngredientCrossRef(recipeId, ingredientId))
        }

    }
}