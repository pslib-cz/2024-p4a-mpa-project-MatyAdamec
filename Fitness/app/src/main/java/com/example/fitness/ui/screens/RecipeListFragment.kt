package com.example.fitness.ui.screens

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.fitness.R
import com.example.fitness.Data.AppDatabase
import com.example.fitness.Data.entities.Recipe
import com.example.fitness.databinding.DialogAddRecipeBinding
import com.example.fitness.databinding.FragmentRecipeListBinding
import com.example.fitness.repository.RecipeRepository
import com.example.fitness.ui.viewmodels.RecipeViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecipeListFragment : Fragment() {

    private var _binding: FragmentRecipeListBinding? = null
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

    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Načtení receptů
        viewModel.loadRecipes()

        // Vytvoření adapteru pro ListView
        adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, mutableListOf())
        binding.listView.adapter = adapter

        // Nastavení kliknutí na položku seznamu
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val selectedRecipe = viewModel.combinedResults.value[position]
            //Toast.makeText(requireContext(), "Kliknuto na ${selectedRecipe.name}", Toast.LENGTH_SHORT).show()

            // Vytvoření bundle a navigace
            val bundle = Bundle().apply {
                putLong("recipeId", selectedRecipe.recipeId)
            }
            findNavController().navigate(R.id.action_recipeListFragment_to_recipeDetailFragment, bundle)
        }

        // Nastavení SearchView pro vyhledávání receptů
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterAndSearchRecipes(viewModel.selectedIngredients.value, newText)
                return true
            }
        })


        // Nastavení FloatingActionButton pro přidání nového receptu
        binding.addRecipeFab.setOnClickListener {
            showAddRecipeDialog()
        }

        binding.filterButton.setOnClickListener {
            showFilterDialog()
        }
        binding.goToRandomRecipes.setOnClickListener {
            findNavController().navigate(R.id.action_recipeListFragment_to_recipeRandomFragment)
        }

        // Pozorování vybraných ingrediencí
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedIngredients.collectLatest { selectedIngredients ->
                updateSelectedIngredientsDisplay(selectedIngredients)
            }
        }
/*
        // Pozorování filtrovaných receptů
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredRecipes.collectLatest { filteredList ->
                if (filteredList.isNotEmpty()) {
                    adapter.clear()
                    adapter.addAll(filteredList.map { it.name })
                    adapter.notifyDataSetChanged()
                    Log.d("RecipeListFragment", "Filtered recipes updated: ${filteredList.size} items")
                }
            }
        }
        // Pozorování výsledků vyhledávání
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collectLatest { searchList ->
                adapter.clear()
                adapter.addAll(searchList.map { it.name })
                adapter.notifyDataSetChanged()
                Log.d("RecipeListFragment", "Search results updated: ${searchList.size} items")
            }
        }*/

        // Observe combined results
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.combinedResults.collectLatest { combinedList ->
                adapter.clear()
                adapter.addAll(combinedList.map { it.name })
                adapter.notifyDataSetChanged()
                Log.d("RecipeListFragment", "Combined results updated: ${combinedList.size} items")
            }
        }

        // Vložení ukázkových dat při prvním spuštění

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getRecipesEmpty()
        }

    }
    private fun updateSelectedIngredientsDisplay(selectedIngredients: List<String>) {
        binding.selectedIngredientsChipGroup.removeAllViews()
        for (ingredient in selectedIngredients) {
            val chip = Chip(requireContext())
            chip.text = ingredient
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                // Remove ingredient from filter
                val updatedList = viewModel.selectedIngredients.value.toMutableList()
                updatedList.remove(ingredient)

                viewModel.filterAndSearchRecipes(updatedList, binding.searchView.query.toString())
            }
            binding.selectedIngredientsChipGroup.addView(chip)
        }
    }

    private fun showFilterDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            val ingredients = repository.getAllIngredients().map { it.name }.sorted()
            val selectedIngredients = BooleanArray(ingredients.size) { false }

            // Pre-select currently selected ingredients
            val currentSelected = viewModel.selectedIngredients.value
            currentSelected.forEach { ingredient ->
                val index = ingredients.indexOf(ingredient)
                if (index != -1) {
                    selectedIngredients[index] = true
                }
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Filtrovat podle ingrediencí")
                .setMultiChoiceItems(ingredients.toTypedArray(), selectedIngredients) { _, which, isChecked ->
                    selectedIngredients[which] = isChecked
                }
                .setPositiveButton("Filtrovat") { dialog, _ ->
                    val selectedList = ingredients.filterIndexed { index, _ -> selectedIngredients[index] }
                    viewModel.filterAndSearchRecipes(selectedList, binding.searchView.query.toString())
                    dialog.dismiss()
                }
                .setNegativeButton("Zrušit") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    private fun showAddRecipeDialog() {
        val dialogBinding = DialogAddRecipeBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Přidat nový recept")
            .setView(dialogBinding.root)
            .setPositiveButton("Přidat") { dialogInterface, _ ->
                val recipeName = dialogBinding.recipeNameEditText.text.toString().trim()
                val recipeDescription = dialogBinding.recipeDescriptionEditText.text.toString().trim()
                val recipeIngredientsInput = dialogBinding.recipeIngredientsEditText.text.toString().trim()

                if (recipeName.isEmpty()) {
                    Toast.makeText(requireContext(), "Název receptu je povinný.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (recipeIngredientsInput.isEmpty()) {
                    Toast.makeText(requireContext(), "Ingredience jsou povinné.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val ingredients = recipeIngredientsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                val newRecipe = Recipe(
                    name = recipeName,
                    description = recipeDescription
                )

                viewModel.addRecipe(newRecipe,ingredients);

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
