package com.example.fitness.ui.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fitness.databinding.FragmentRecipeRandomBinding
import com.bumptech.glide.Glide
import com.example.fitness.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import kotlin.reflect.full.memberProperties

class RecipeRandomFragment : Fragment() {

    private var _binding: FragmentRecipeRandomBinding? = null
    private val binding get() = _binding!!

    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealApi::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRecipeRandomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchRandomMeal()

        // Nastavení obsluhy tlačítka pro načtení nového receptu
        binding.newMealButton.setOnClickListener {
            fetchRandomMeal()
        }
        binding.menuButton.setOnClickListener {
            findNavController().navigate(R.id.action_recipeRandomFragment_to_recipeListFragment)
        }
    }

    private fun fetchRandomMeal() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getRandomMeal()
                val meal = response.meals.firstOrNull()
                withContext(Dispatchers.Main) {
                    if (meal != null) {
                        Log.d("RecipeRandomFragment", "MEAL mealNameTextView: ${meal.strMeal}")
                        Log.d("RecipeRandomFragment", "MEAL mealCategoryTextView: ${meal.strCategory}")
                        Log.d("RecipeRandomFragment", "MEAL mealAreaTextView: ${meal.strArea}")
                        Log.d("RecipeRandomFragment", "MEAL mealInstructionsTextView: ${meal.strInstructions}")
                        Log.d("RecipeRandomFragment", "MEAL strMealThumb: ${meal.strInstructions}")
                        Log.d("RecipeRandomFragment", "MEAL mealInstructionsTextView: ${meal.strMealThumb}")
                        binding.mealNameTextView.text = meal.strMeal
                        binding.mealCategoryTextView.text = "Category: ${meal.strCategory}"
                        binding.mealAreaTextView.text = "Area: ${meal.strArea}"
                        binding.mealInstructionsTextView.text = meal.strInstructions
                        Glide.with(this@RecipeRandomFragment)
                            .load(meal.strMealThumb)
                            .into(binding.mealImageView)
                        binding.ingredientsTextView.text = getIngredients(meal)
                    } else {
                        Toast.makeText(requireContext(), "No meal found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error fetching meal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getIngredients(meal: Meal): String {
        val ingredients = mutableListOf<String>()
        try {
            for (i in 1..20) {
                Log.d("RecipeRandomFragment", "MEAL ingredient start for index $i")
                val ingredient = meal::class.memberProperties
                    .find { it.name == "strIngredient$i" }
                    ?.getter
                    ?.call(meal) as? String
                Log.d("RecipeRandomFragment", "MEAL ingredient: $ingredient")
                val measure = meal::class.memberProperties
                    .find { it.name == "strMeasure$i" }
                    ?.getter
                    ?.call(meal) as? String
                Log.d("RecipeRandomFragment", "MEAL measure: $measure")
                if (!ingredient.isNullOrBlank()) {
                    ingredients.add("${measure?.trim() ?: ""} ${ingredient.trim()}".trim())
                }
            }
        } catch (e: Exception) {
            Log.e("RecipeRandomFragment", "Error parsing ingredients", e)
        }
        return ingredients.joinToString("\n")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface MealApi {
        @GET("random.php")
        suspend fun getRandomMeal(): MealResponse
    }

    data class MealResponse(
        val meals: List<Meal>
    )

    data class Meal(
        val idMeal: String,
        val strMeal: String,
        val strDrinkAlternate: String?,
        val strCategory: String,
        val strArea: String,
        val strInstructions: String,
        val strMealThumb: String,
        val strTags: String?,
        val strYoutube: String?,
        val strIngredient1: String?,
        val strIngredient2: String?,
        val strIngredient3: String?,
        val strIngredient4: String?,
        val strIngredient5: String?,
        val strIngredient6: String?,
        val strIngredient7: String?,
        val strIngredient8: String?,
        val strIngredient9: String?,
        val strIngredient10: String?,
        val strIngredient11: String?,
        val strIngredient12: String?,
        val strIngredient13: String?,
        val strIngredient14: String?,
        val strIngredient15: String?,
        val strIngredient16: String?,
        val strIngredient17: String?,
        val strIngredient18: String?,
        val strIngredient19: String?,
        val strIngredient20: String?,
        val strMeasure1: String?,
        val strMeasure2: String?,
        val strMeasure3: String?,
        val strMeasure4: String?,
        val strMeasure5: String?,
        val strMeasure6: String?,
        val strMeasure7: String?,
        val strMeasure8: String?,
        val strMeasure9: String?,
        val strMeasure10: String?,
        val strMeasure11: String?,
        val strMeasure12: String?,
        val strMeasure13: String?,
        val strMeasure14: String?,
        val strMeasure15: String?,
        val strMeasure16: String?,
        val strMeasure17: String?,
        val strMeasure18: String?,
        val strMeasure19: String?,
        val strMeasure20: String?,
        val strSource: String?,
        val strImageSource: String?,
        val strCreativeCommonsConfirmed: String?,
        val dateModified: String?
    )
}
