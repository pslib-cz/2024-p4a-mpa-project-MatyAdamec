package com.example.fitness.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness.repository.RecipeRepository
import com.example.fitness.Data.entities.Ingredient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IngredientViewModel(private val repository: RecipeRepository) : ViewModel() {
    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredients = _ingredients.asStateFlow()

    fun loadIngredients() {
        viewModelScope.launch {
            val data = repository.getAllIngredients()
            _ingredients.value = data
        }
    }
}