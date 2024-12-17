package com.example.fitness.ui.viewmodels

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
    private val _recipes = MutableStateFlow<List<com.example.fitness.Data.entities.Recipe>>(emptyList())
    val recipes: StateFlow<List<com.example.fitness.Data.entities.Recipe>> = _recipes

    private val _selectedRecipe = MutableStateFlow<RecipeWithIngredients?>(null)
    val selectedRecipe: StateFlow<RecipeWithIngredients?> = _selectedRecipe

    fun loadRecipes() {
        viewModelScope.launch {
            val data = repository.getAllRecipes()
            _recipes.value = data
        }
    }

    fun loadRecipeWithIngredients(recipeId: Long) {
        viewModelScope.launch {
            val recipe = repository.getRecipeWithIngredients(recipeId)
            _selectedRecipe.value = recipe
        }
    }
}