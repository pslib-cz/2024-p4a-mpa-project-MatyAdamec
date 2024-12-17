package com.example.fitness.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fitness.Data.AppDatabase
import com.example.fitness.databinding.FragmentIngredientListBinding
import com.example.fitness.repository.RecipeRepository
import com.example.fitness.ui.viewmodels.IngredientViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class IngredientListFragment : Fragment() {
    private var _binding: FragmentIngredientListBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy {
        val db = AppDatabase.getDatabase(requireContext())
        RecipeRepository(db.recipeDao())
    }

    private val viewModel by viewModels<IngredientViewModel> {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return IngredientViewModel(repository) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentIngredientListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Načtení ingrediencí
        viewModel.loadIngredients()

        // Vytvoření adapteru pro ListView
        val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, mutableListOf())
        binding.listView.adapter = adapter

        // Pozorování změn v ingrediencích
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ingredients.collectLatest { ingredientList ->
                adapter.clear()
                adapter.addAll(ingredientList.map { it.name })
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}