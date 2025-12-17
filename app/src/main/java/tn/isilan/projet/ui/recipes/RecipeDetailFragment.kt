package tn.isilan.projet.ui.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tn.isilan.projet.data.database.RecipeDatabase
import tn.isilan.projet.data.entities.Recipe
import tn.isilan.projet.data.repository.RecipeRepository
import tn.isilan.projet.databinding.FragmentRecipeDetailBinding
import tn.isilan.projet.ui.viewmodels.RecipeViewModel
import tn.isilan.projet.ui.viewmodels.ViewModelFactory

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
        viewModel = ViewModelProvider(this, factory)[RecipeViewModel::class.java]

        loadRecipeDetails()
    }

    private fun loadRecipeDetails() {
        // Collect the Flow from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recipes.collectLatest { recipeList: List<Recipe> ->
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
        return ingredients.split(",")
            .joinToString("\n") { "• ${it.trim()}" }
    }

    private fun formatInstructions(instructions: String): String {
        return instructions.split("\n")
            .mapIndexed { index, step -> "${index + 1}. ${step.trim()}" }
            .joinToString("\n")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
