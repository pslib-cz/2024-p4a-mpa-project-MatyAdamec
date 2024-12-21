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
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.fitness.Data.AppDatabase
import com.example.fitness.R
import com.example.fitness.databinding.DialogEditRecipeBinding
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
        } 

        // Pozorování vybraného receptu
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedRecipe.collectLatest { recipeWithIngredients ->
                if (recipeWithIngredients != null) {
                    binding.recipeName.text = recipeWithIngredients.recipe.name
                    binding.recipeDescription.text = recipeWithIngredients.recipe.description
                    binding.ingredientsList.text = recipeWithIngredients.ingredients.joinToString(", ") { it.name }
                    Log.d("RecipeDetailFragment", "Loaded recipe: ${recipeWithIngredients.recipe.name}")
                } 
            }
        }

        binding.deleteRecipeButton.setOnClickListener {
            if (recipeId != -1L) {
                // Potvrzení vymazání
                confirmDeleteRecipe(recipeId)
            } else {
                Toast.makeText(requireContext(), "Neplatný ID receptu", Toast.LENGTH_SHORT).show()
            }
        }

        binding.editRecipeButton.setOnClickListener {
            if (recipeId != -1L) {
                showEditRecipeDialog(recipeId)
            } else {
                Toast.makeText(requireContext(), "Neplatný ID receptu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDeleteRecipe(recipeId: Long) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Vymazat Recept")
        builder.setMessage("Opravdu chcete vymazat tento recept?")
        builder.setPositiveButton("Ano") { dialog, _ ->
            viewModel.deleteRecipe(recipeId)
            Toast.makeText(requireContext(), "Recept vymazán", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            dialog.dismiss()
        }
        builder.setNegativeButton("Ne") { dialog, _ ->
            // Zrušení akce
            dialog.dismiss()
        }
        builder.create().show()
    }


    private fun showEditRecipeDialog(recipeId: Long) {
        // Vytvoření bindingu pro dialog
        val dialogBinding = DialogEditRecipeBinding.inflate(LayoutInflater.from(context))

        // Naplnění aktuálními daty receptu
        viewLifecycleOwner.lifecycleScope.launch {
            val recipeWithIngredients = viewModel.selectedRecipe.value
            if (recipeWithIngredients != null) {
                dialogBinding.editRecipeNameEditText.setText(recipeWithIngredients.recipe.name)
                dialogBinding.editRecipeDescriptionEditText.setText(recipeWithIngredients.recipe.description)
                dialogBinding.editRecipeIngredientsEditText.setText(
                    recipeWithIngredients.ingredients.joinToString(", ") { it.name }
                )
            }
        }

        // Vytvoření AlertDialog s vlastním layoutem
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Upravit Recept")
            .setView(dialogBinding.root)
            .setPositiveButton("Upravit") { dialogInterface, _ ->
                // Získání textu z input fieldů
                val recipeName = dialogBinding.editRecipeNameEditText.text.toString().trim()
                val recipeDescription = dialogBinding.editRecipeDescriptionEditText.text.toString().trim()
                val recipeIngredientsInput = dialogBinding.editRecipeIngredientsEditText.text.toString().trim()

                // Validace vstupů
                if (recipeName.isEmpty()) {
                    Toast.makeText(requireContext(), "Název receptu je povinný.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (recipeIngredientsInput.isEmpty()) {
                    Toast.makeText(requireContext(), "Ingredience jsou povinné.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Rozdělení ingrediencí podle čárky a odstranění mezer
                val ingredients = recipeIngredientsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                // Vytvoření aktualizovaného Recipe objektu
                val updatedRecipe = com.example.fitness.Data.entities.Recipe(
                    recipeId = recipeId,
                    name = recipeName,
                    description = recipeDescription
                )

                // Aktualizace receptu v databázi
                viewModel.updateRecipe(updatedRecipe, ingredients)

                Toast.makeText(requireContext(), "Recept upraven.", Toast.LENGTH_SHORT).show()

                dialogInterface.dismiss()
            }
            .setNegativeButton("Zrušit") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
