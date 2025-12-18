package tn.isilan.projet.ui.recipes

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var currentRecipe: Recipe? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipeId = arguments?.getLong("recipeId", -1L) ?: -1L

        val database = RecipeDatabase.getInstance(requireContext())
        val repository = RecipeRepository(
            database.recipeDao(),
            database.shoppingListDao()
        )
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[RecipeViewModel::class.java]

        setupMenuButtons()
        loadRecipeDetails()
    }

    /* ---------------- MENU ---------------- */

    private fun setupMenuButtons() {
        binding.toolbar.inflateMenu(R.menu.menu_recipe_detail)

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    currentRecipe?.let { navigateToEditRecipe(it) }
                    true
                }
                R.id.action_share -> {
                    currentRecipe?.let { shareRecipe(it) }
                    true
                }
                R.id.action_delete -> {
                    currentRecipe?.let { showDeleteConfirmation(it) }
                    true
                }
                else -> false
            }
        }
    }

    /* ---------------- DATA ---------------- */

    private fun loadRecipeDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recipes.collectLatest { recipes ->
                val recipe = recipes.find { it.id == recipeId }

                if (recipe != null) {
                    currentRecipe = recipe
                    displayRecipe(recipe)
                } else {
                    showRecipeNotFound()
                }
            }
        }
    }

    private fun displayRecipe(recipe: Recipe) {
        binding.textTitle.text = recipe.title
        binding.textDescription.text = recipe.description
        binding.textIngredientsList.text = formatIngredients(recipe.ingredients)
        binding.textInstructionsList.text = formatInstructions(recipe.instructions)
        binding.textPreparationInfo.text = "Temps : ${recipe.preparationTime} min"
        binding.textDifficultyInfo.text = "Difficulté : ${recipe.difficulty}"

        val imageResId = when (recipe.imageUri) {
            "recipe_default_1" -> R.drawable.recipe_default_1
            "recipe_default_2" -> R.drawable.recipe_default_2
            "recipe_default_3" -> R.drawable.recipe_default_3
            else -> R.drawable.recipe_default_1
        }

        binding.imageRecipe.setImageResource(imageResId)
        binding.imageRecipe.visibility = View.VISIBLE
    }

    private fun showRecipeNotFound() {
        binding.textTitle.text = "Recette non trouvée"
        binding.textDescription.text = "Cette recette n'existe plus."

        binding.toolbar.menu.findItem(R.id.action_edit).isVisible = false
        binding.toolbar.menu.findItem(R.id.action_share).isVisible = false
        binding.toolbar.menu.findItem(R.id.action_delete).isVisible = false
    }

    /* ---------------- ACTIONS ---------------- */

    private fun navigateToEditRecipe(recipe: Recipe) {
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

    private fun showDeleteConfirmation(recipe: Recipe) {
        AlertDialog.Builder(requireContext())
            .setTitle("Supprimer la recette")
            .setMessage("Voulez-vous supprimer « ${recipe.title} » ?")
            .setPositiveButton("Supprimer") { _, _ ->
                deleteRecipe(recipe)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun deleteRecipe(recipe: Recipe) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteRecipe(recipe)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "Recette supprimée",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun shareRecipe(recipe: Recipe) {
        val shareText = """
            🍽️ ${recipe.title}

            📝 Description :
            ${recipe.description}

            🛒 Ingrédients :
            ${formatIngredientsForShare(recipe.ingredients)}

            👨‍🍳 Instructions :
            ${formatInstructionsForShare(recipe.instructions)}

            ⏱️ Temps : ${recipe.preparationTime} min
            🎯 Difficulté : ${recipe.difficulty}

            Partagé depuis Smart Recipes
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(intent, "Partager la recette"))
    }

    /* ---------------- FORMATTERS ---------------- */

    private fun formatIngredients(ingredients: String): String =
        ingredients.split(",")
            .joinToString("\n") { "• ${it.trim()}" }

    private fun formatInstructions(instructions: String): String =
        instructions.split("\n")
            .mapIndexed { index, step -> "${index + 1}. ${step.trim()}" }
            .joinToString("\n")

    private fun formatIngredientsForShare(ingredients: String): String =
        formatIngredients(ingredients)

    private fun formatInstructionsForShare(instructions: String): String =
        formatInstructions(instructions)

    /* ---------------- CLEANUP ---------------- */

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
