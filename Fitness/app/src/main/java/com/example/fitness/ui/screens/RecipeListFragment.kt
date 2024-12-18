package com.example.fitness.ui.screens

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
import androidx.navigation.fragment.findNavController
import com.example.fitness.R
import com.example.fitness.Data.AppDatabase
import com.example.fitness.databinding.FragmentRecipeListBinding
import com.example.fitness.repository.RecipeRepository
import com.example.fitness.ui.viewmodels.RecipeViewModel
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
            val selectedRecipe = viewModel.recipes.value[position]
            Toast.makeText(requireContext(), "Kliknuto na ${selectedRecipe.name}", Toast.LENGTH_SHORT).show()

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
                // Volání metody pro vyhledávání receptů ve ViewModelu
                viewModel.searchRecipes(newText ?: "")
                return true
            }
        })

        // Pozorování výsledků vyhledávání
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collectLatest { searchList ->
                adapter.clear()
                adapter.addAll(searchList.map { it.name })
                adapter.notifyDataSetChanged()
                Log.d("RecipeListFragment", "Search results updated: ${searchList.size} items")
            }
        }
        // Vložení ukázkových dat při prvním spuštění
        /*
        viewLifecycleOwner.lifecycleScope.launch {
            if (viewModel.recipes.value.isEmpty()) {
                Log.d("RecipeListFragment", "Inserting sample data")
                repository.insertSampleData()
                viewModel.loadRecipes()
            }
        }
        */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
