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

    fun loadRecipes() {
        viewModelScope.launch {
            val data = repository.getAllRecipes()
            _searchResults.value = data
            _recipes.value = data
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
}