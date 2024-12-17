package com.example.fitness.ui.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fitness.Data.AppDatabase
import com.example.fitness.R
import com.example.fitness.databinding.FragmentRecipeDetailBinding
import com.example.fitness.repository.RecipeRepository
import com.example.fitness.ui.viewmodels.RecipeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy {
        val db = AppDatabase.getDatabase(requireContext())
        RecipeRepository(db.recipeDao())
    }

    private val viewModel by viewModels<RecipeViewModel> {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RecipeViewModel(repository) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Získání recipeId z Bundle
        val recipeId = arguments?.getLong("recipeId") ?: -1L

        if (recipeId != -1L) {
            viewModel.loadRecipeWithIngredients(recipeId)
        } else {
            Toast.makeText(requireContext(), "Recept nenalezen", Toast.LENGTH_SHORT).show()
        }

        // Pozorování vybraného receptu
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedRecipe.collectLatest { recipeWithIngredients ->
                if (recipeWithIngredients != null) {
                    binding.recipeName.text = recipeWithIngredients.recipe.name
                    binding.recipeDescription.text = recipeWithIngredients.recipe.description
                    binding.ingredientsList.text = recipeWithIngredients.ingredients.joinToString(", ") { it.name }
                    Log.d("RecipeDetailFragment", "Loaded recipe: ${recipeWithIngredients.recipe.name}")
                } else {
                    Toast.makeText(requireContext(), "Recept nenalezen", Toast.LENGTH_SHORT).show()
                    Log.e("RecipeDetailFragment", "Recept nenalezen")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
