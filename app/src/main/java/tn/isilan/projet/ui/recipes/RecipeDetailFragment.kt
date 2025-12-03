package tn.isilan.projet.ui.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import tn.isilan.projet.data.database.RecipeDatabase
import tn.isilan.projet.data.repository.RecipeRepository
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.navArgs
import tn.isilan.projet.databinding.FragmentRecipeDetailBinding
import tn.isilan.projet.ui.viewmodels.RecipeViewModel
import tn.isilan.projet.ui.viewmodels.ViewModelFactory
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecipeViewModel
    private var recipeId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Récupérer l'ID depuis les arguments
        recipeId = arguments?.getLong("recipeId", -1L) ?: -1L

        // Initialiser le ViewModel
        val database = RecipeDatabase.getInstance(requireContext())
        val repository = RecipeRepository(database.recipeDao(), database.shoppingListDao())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(RecipeViewModel::class.java)

        loadRecipeDetails()
    }

    private fun loadRecipeDetails() {
        lifecycleScope.launch {
            viewModel.allRecipes.collect { recipes ->
                // ✅ Spécifier explicitement le type pour éviter les erreurs
                val recipeList: List<tn.isilan.projet.data.entities.Recipe> = recipes
                val recipe = recipeList.find { it.id == recipeId }

                recipe?.let { foundRecipe ->
                    binding.textTitle.text = foundRecipe.title
                    binding.textDescription.text = foundRecipe.description
                    binding.textIngredientsList.text = formatIngredients(foundRecipe.ingredients)
                    binding.textInstructionsList.text = formatInstructions(foundRecipe.instructions)
                    binding.textPreparationInfo.text = "Temps: ${foundRecipe.preparationTime} min"
                    binding.textDifficultyInfo.text = "Difficulté: ${foundRecipe.difficulty}"
                } ?: run {
                    binding.textTitle.text = "Recette non trouvée"
                    binding.textDescription.text = "La recette n'existe pas."
                }
            }
        }
    }

    private fun formatIngredients(ingredients: String): String {
        return if (ingredients.contains(",")) {
            ingredients.split(",").joinToString("\n") { "• ${it.trim()}" }
        } else {
            "• $ingredients"
        }
    }

    private fun formatInstructions(instructions: String): String {
        return if (instructions.contains("\n")) {
            instructions.split("\n").mapIndexed { index, step ->
                "${index + 1}. ${step.trim()}"
            }.joinToString("\n")
        } else {
            "1. $instructions"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}