package com.example.fitness.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness.repository.RecipeRepository
import com.example.fitness.Data.entities.Recipe
import com.example.fitness.Data.relations.RecipeWithIngredients
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes

    private val _selectedRecipe = MutableStateFlow<RecipeWithIngredients?>(null)
    val selectedRecipe: StateFlow<RecipeWithIngredients?> = _selectedRecipe

    private val _searchResults = MutableStateFlow<List<Recipe>>(emptyList())
    val searchResults: StateFlow<List<Recipe>> = _searchResults

    private val _filteredRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val filteredRecipes: StateFlow<List<Recipe>> = _filteredRecipes

    private val _selectedIngredients = MutableStateFlow<List<String>>(emptyList())
    val selectedIngredients: StateFlow<List<String>> = _selectedIngredients

    private val _combinedResults = MutableStateFlow<List<Recipe>>(emptyList())
    val combinedResults: StateFlow<List<Recipe>> = _combinedResults


    fun loadRecipes() {
        viewModelScope.launch {
            val data = repository.getAllRecipes()
            _searchResults.value = data
            _recipes.value = data
            _combinedResults.value = data
        }
    }

    fun getRecipesEmpty(){
        viewModelScope.launch {
            val data = repository.getAllRecipes()
            Log.d("RecipeListFragment", "Start loaded all ${data.size}")

            if (data.isEmpty()) {
                Log.d("RecipeListFragment", "LOADING SAMPLE DATA FROM REPO ${recipes.value.isEmpty()}")
                repository.insertSampleData()
                loadRecipes()
            }
        }
    }

    fun loadRecipeWithIngredients(recipeId: Long) {
        viewModelScope.launch {
            val recipe = repository.getRecipeWithIngredients(recipeId)
            _selectedRecipe.value = recipe
        }
    }

    fun searchRecipes(query: String) {
        viewModelScope.launch {
            Log.d("RecipeListFragment", "query: $query isBlank: , ${query.isNotBlank()}")
            if (query.isNotBlank()) {
                val results = repository.searchRecipes(query)
                _searchResults.value = results
            } else {
                Log.d("RecipeListFragment", "loaded all")
                loadRecipes()
            }
        }
    }

    fun filterRecipesByIngredients(ingredientNames: List<String>) {
        viewModelScope.launch {
            val filtered = repository.filterRecipesByIngredients(ingredientNames)
            Log.d("RecipeViewModel", "Filtered recipes: ${filtered.size} recipes")
            _filteredRecipes.value = filtered
            _selectedIngredients.value = ingredientNames
            _searchResults.value = emptyList()
        }
    }

    fun filterAndSearchRecipes(ingredientNames: List<String>, query: String?) {
        viewModelScope.launch {
            //Log.d("RecipeListFragment", "query: $query, ingredients: $ingredientNames")
            val results = repository.filterAndSearchRecipes(ingredientNames, query)
            //Log.d("RecipeListFragment", "results: $results")
            _combinedResults.value = results
            _selectedIngredients.value = ingredientNames
        }
    }


    fun deleteRecipe(recipeId: Long) {
        viewModelScope.launch {
            repository.deleteRecipe(recipeId)
            Log.d("RecipeViewModel", "Deleted recipe with ID: $recipeId")
            loadRecipes()
        }
    }

    fun addRecipe(recipe: Recipe, ingredients: List<String>) {
        viewModelScope.launch {
            repository.insertRecipe(recipe, ingredients)
            Log.d("RecipeViewModel", "Added recipe: ${recipe.name}")
            loadRecipes()
        }
    }

    fun updateRecipe(recipe: Recipe, ingredients: List<String>) {
        viewModelScope.launch {
            repository.updateRecipe(recipe, ingredients)
            Log.d("RecipeViewModel", "Updated recipe: ${recipe.name}")
            loadRecipes()
            loadRecipeWithIngredients(recipe.recipeId)
        }
    }
}