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

        // Recept 11: Řízek s bramborovým salátem
        val recipe11Id = recipeDao.insertRecipe(Recipe(name = "Řízek s bramborovým salátem", description = "Klasický český řízek podávaný s bramborovým salátem."))
        val recipe11Ingredients = listOf("Vepřové maso", "Vejce", "Mouka", "Strouhanka", "Olej", "Sůl", "Pepř", "Brambory", "Majonéza", "Cibule")
        insertIngredientsForRecipe(recipe11Id, recipe11Ingredients)

        // Recept 12: Knedlo zelo vepřo
        val recipe12Id = recipeDao.insertRecipe(Recipe(name = "Knedlo zelo vepřo", description = "Tradiční české jídlo z knedlíků, zelí a vepřového masa."))
        val recipe12Ingredients = listOf("Vepřové maso", "Knedlíky", "Kysané zelí", "Cibule", "Olej", "Sůl", "Pepř", "Koření")
        insertIngredientsForRecipe(recipe12Id, recipe12Ingredients)

        // Recept 13: Bramborové placky
        val recipe13Id = recipeDao.insertRecipe(Recipe(name = "Bramborové placky", description = "Lahodné placky z nastrouhaných brambor."))
        val recipe13Ingredients = listOf("Brambory", "Vejce", "Mouka", "Cibule", "Česnek", "Sůl", "Pepř", "Olej")
        insertIngredientsForRecipe(recipe13Id, recipe13Ingredients)

        // Recept 14: Štrůdl
        val recipe14Id = recipeDao.insertRecipe(Recipe(name = "Štrůdl", description = "Tradiční český ovocný štrůdl s jablky a skořicí."))
        val recipe14Ingredients = listOf("Listové těsto", "Jablka", "Cukr", "Skořice", "Rozinky", "Olej", "Droždí")
        insertIngredientsForRecipe(recipe14Id, recipe14Ingredients)

        // Recept 15: Křehký koláč s tvarohem
        val recipe15Id = recipeDao.insertRecipe(Recipe(name = "Křehký koláč s tvarohem", description = "Delikátní koláč s jemným tvarohovým náplní."))
        val recipe15Ingredients = listOf("Mouka", "Máslo", "Cukr", "Vejce", "Tvaroh", "Vanilka", "Citronová kůra")
        insertIngredientsForRecipe(recipe15Id, recipe15Ingredients)

        // Recept 16: Vepřové na houbách
        val recipe16Id = recipeDao.insertRecipe(Recipe(name = "Vepřové na houbách", description = "Vepřové maso dušené s houbami a cibulí."))
        val recipe16Ingredients = listOf("Vepřové maso", "Houby", "Cibule", "Česnek", "Smetana", "Sůl", "Pepř", "Olej")
        insertIngredientsForRecipe(recipe16Id, recipe16Ingredients)

        // Recept 17: Smažené žampiony
        val recipe17Id = recipeDao.insertRecipe(Recipe(name = "Smažené žampiony", description = "Křupavé smažené žampiony s česnekem a bylinkami."))
        val recipe17Ingredients = listOf("Žampiony", "Vejce", "Strouhanka", "Česnek", "Petržel", "Sůl", "Pepř", "Olej")
        insertIngredientsForRecipe(recipe17Id, recipe17Ingredients)

        // Recept 18: Halušky s brynzou
        val recipe18Id = recipeDao.insertRecipe(Recipe(name = "Halušky s brynzou", description = "Tradiční slovenské halušky podávané s brynzou."))
        val recipe18Ingredients = listOf("Brambory", "Mouka", "Vejce", "Sůl", "Brynza", "Slanina")
        insertIngredientsForRecipe(recipe18Id, recipe18Ingredients)

        // Recept 19: Bramborové knedlíky s uzeným masem
        val recipe19Id = recipeDao.insertRecipe(Recipe(name = "Bramborové knedlíky s uzeným masem", description = "Tradiční bramborové knedlíky podávané s uzeným masem a zelím."))
        val recipe19Ingredients = listOf("Brambory", "Mouka", "Vejce", "Sůl", "Uzené maso", "Kysané zelí")
        insertIngredientsForRecipe(recipe19Id, recipe19Ingredients)

        // Recept 20: Vepřové na paprice
        val recipe20Id = recipeDao.insertRecipe(Recipe(name = "Vepřové na paprice", description = "Delikátní vepřové maso vařené v pikantní paprikové omáčce."))
        val recipe20Ingredients = listOf("Vepřové maso", "Paprika", "Cibule", "Česnek", "Rajčata", "Smetana", "Sůl", "Pepř", "Olej")
        insertIngredientsForRecipe(recipe20Id, recipe20Ingredients)

        // Recept 21: Kysané zelí s klobásou
        val recipe21Id = recipeDao.insertRecipe(Recipe(name = "Kysané zelí s klobásou", description = "Jednoduchý pokrm z kysaného zelí a klobásy."))
        val recipe21Ingredients = listOf("Kysané zelí", "Klobása", "Cibule", "Česnek", "Sůl", "Pepř", "Olej")
        insertIngredientsForRecipe(recipe21Id, recipe21Ingredients)

        // Recept 22: Česnečka
        val recipe22Id = recipeDao.insertRecipe(Recipe(name = "Česnečka", description = "Vřelá česnečná polévka s chlebem a sýrem."))
        val recipe22Ingredients = listOf("Česnek", "Voda", "Hovězí vývar", "Cibule", "Sůl", "Pepř", "Chléb", "Sýr")
        insertIngredientsForRecipe(recipe22Id, recipe22Ingredients)

        // Recept 23: Moravský vrabec
        val recipe23Id = recipeDao.insertRecipe(Recipe(name = "Moravský vrabec", description = "Vepřové maso pečené s knedlíky a zelím."))
        val recipe23Ingredients = listOf("Vepřové maso", "Knedlíky", "Kysané zelí", "Cibule", "Olej", "Sůl", "Pepř")
        insertIngredientsForRecipe(recipe23Id, recipe23Ingredients)

        // Recept 24: Knedlíčky se švestkami
        val recipe24Id = recipeDao.insertRecipe(Recipe(name = "Knedlíčky se švestkami", description = "Sladké knedlíčky plněné švestkami posypané cukrem a skořicí."))
        val recipe24Ingredients = listOf("Mouka", "Vejce", "Voda", "Sůl", "Švestky", "Cukr", "Skořice", "Olej")
        insertIngredientsForRecipe(recipe24Id, recipe24Ingredients)

        // Recept 25: Rajská polévka
        val recipe25Id = recipeDao.insertRecipe(Recipe(name = "Rajská polévka", description = "Lehká rajská polévka s masem a bylinkami."))
        val recipe25Ingredients = listOf("Rajčata", "Cibule", "Česnek", "Hovězí maso", "Smetana", "Sůl", "Pepř", "Olej", "Bylinky")
        insertIngredientsForRecipe(recipe25Id, recipe25Ingredients)

        // Recept 26: Cibulová polévka
        val recipe26Id = recipeDao.insertRecipe(Recipe(name = "Cibulová polévka", description = "Tradiční česká cibulová polévka s krutony a sýrem."))
        val recipe26Ingredients = listOf("Cibule", "Máslo", "Voda", "Hovězí vývar", "Sůl", "Pepř", "Chléb", "Sýr")
        insertIngredientsForRecipe(recipe26Id, recipe26Ingredients)

        // Recept 27: Pečená kachna s červeným zelím
        val recipe27Id = recipeDao.insertRecipe(Recipe(name = "Pečená kachna s červeným zelím", description = "Delikátní pečená kachna podávaná s červeným zelím a knedlíky."))
        val recipe27Ingredients = listOf("Kachna", "Červené zelí", "Cibule", "Jablka", "Sůl", "Pepř", "Kmín", "Olej")
        insertIngredientsForRecipe(recipe27Id, recipe27Ingredients)

        // Recept 28: Pivní guláš
        val recipe28Id = recipeDao.insertRecipe(Recipe(name = "Pivní guláš", description = "Guláš připravený s pivem místo vody, pro bohatší chuť."))
        val recipe28Ingredients = listOf("Hovězí maso", "Cibule", "Paprika", "Česnek", "Pivo", "Rajčata", "Sůl", "Pepř", "Kmín", "Olej")
        insertIngredientsForRecipe(recipe28Id, recipe28Ingredients)

        // Recept 29: Grilované jehněčí kotlety
        val recipe29Id = recipeDao.insertRecipe(Recipe(name = "Grilované jehněčí kotlety", description = "Jemné jehněčí kotlety marinované a grilované do dokonalosti."))
        val recipe29Ingredients = listOf("Jehněčí kotlety", "Česnek", "Rozmarýn", "Citronová šťáva", "Olej", "Sůl", "Pepř")
        insertIngredientsForRecipe(recipe29Id, recipe29Ingredients)

        // Recept 30: Tvarohový koláč
        val recipe30Id = recipeDao.insertRecipe(Recipe(name = "Tvarohový koláč", description = "Sladký koláč s tvarohovou náplní a ovocem."))
        val recipe30Ingredients = listOf("Mouka", "Máslo", "Vejce", "Cukr", "Tvaroh", "Vanilka", "Citrónová šťáva", "Ovoce")
        insertIngredientsForRecipe(recipe30Id, recipe30Ingredients)
    }


    private suspend fun insertIngredientsForRecipe(recipeId: Long, ingredients: List<String>) {

        for (ingredientName in ingredients) {
            val ingredientId = recipeDao.getIngredientByName(ingredientName)?.ingredientId ?: recipeDao.insertIngredient(Ingredient(name = ingredientName))
            recipeDao.insertRecipeIngredientCrossRef(RecipeIngredientCrossRef(recipeId, ingredientId))
        }

    }
}