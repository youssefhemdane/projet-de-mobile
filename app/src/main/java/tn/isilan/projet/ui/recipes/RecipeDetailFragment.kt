package tn.isilan.projet.ui.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tn.isilan.projet.R
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
    private var currentRecipe: Recipe? = null // Stocker la recette courante

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

        setupEditButton() // Nouveau : configurer le bouton
        loadRecipeDetails()
    }

    private fun setupEditButton() {
        // Ajouter un bouton Modifier dans la toolbar
        binding.toolbar.inflateMenu(R.menu.menu_recipe_detail)

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    currentRecipe?.let { navigateToEditRecipe(it) }
                    true
                }
                else -> false
            }
        }
    }

    private fun loadRecipeDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recipes.collectLatest { recipeList: List<Recipe> ->
                val recipe = recipeList.find { it.id == recipeId }

                recipe?.let { foundRecipe ->
                    currentRecipe = foundRecipe // Sauvegarder pour l'édition

                    binding.textTitle.text = foundRecipe.title
                    binding.textDescription.text = foundRecipe.description
                    binding.textIngredientsList.text = formatIngredients(foundRecipe.ingredients)
                    binding.textInstructionsList.text = formatInstructions(foundRecipe.instructions)
                    binding.textPreparationInfo.text = "Temps: ${foundRecipe.preparationTime} min"
                    binding.textDifficultyInfo.text = "Difficulté: ${foundRecipe.difficulty}"

                    val defaultImage = R.drawable.recipe_default_1
                    val imageResId = when (foundRecipe.imageUri) {
                        "recipe_default_1" -> R.drawable.recipe_default_1
                        "recipe_default_2" -> R.drawable.recipe_default_2
                        "recipe_default_3" -> R.drawable.recipe_default_3
                        else -> defaultImage
                    }
                    binding.imageRecipe.setImageResource(imageResId)
                    binding.imageRecipe.visibility = View.VISIBLE

                } ?: run {
                    binding.textTitle.text = "Recette non trouvée"
                    binding.textDescription.text = "La recette n'existe pas."
                    // Cacher le bouton Modifier si recette non trouvée
                    binding.toolbar.menu.findItem(R.id.action_edit).isVisible = false
                }
            }
        }
    }

    private fun navigateToEditRecipe(recipe: Recipe) {
        // Naviguer vers AddRecipeFragment avec les données pré-remplies
        val bundle = Bundle().apply {
            putLong("recipeId", recipe.id)
            putString("recipeTitle", recipe.title)
            putString("recipeDescription", recipe.description)
            putString("recipeIngredients", recipe.ingredients)
            putString("recipeInstructions", recipe.instructions)
            putInt("recipePreparationTime", recipe.preparationTime)
            putString("recipeDifficulty", recipe.difficulty)
            putString("recipeImageUri", recipe.imageUri)
        }

        findNavController().navigate(
            R.id.action_recipeDetail_to_addRecipe,
            bundle
        )
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