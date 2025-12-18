package tn.isilan.projet.ui.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tn.isilan.projet.R
import tn.isilan.projet.data.database.RecipeDatabase
import tn.isilan.projet.data.entities.Recipe
import tn.isilan.projet.data.repository.RecipeRepository
import tn.isilan.projet.databinding.FragmentAddRecipeBinding
import tn.isilan.projet.ui.viewmodels.RecipeViewModel
import tn.isilan.projet.ui.viewmodels.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class AddRecipeFragment : Fragment() {

    private var _binding: FragmentAddRecipeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecipeViewModel

    private val defaultImages = listOf(
        R.drawable.recipe_default_1,
        R.drawable.recipe_default_2,
        R.drawable.recipe_default_3
    )

    private var selectedImageResId: Int = R.drawable.recipe_default_1
    private var isEditMode = false
    private var recipeId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupDifficultySpinner()
        setupImageButton()
        setupSaveButton()

        checkEditMode()

        if (!isEditMode) {
            showRandomImage()
        }
    }

    private fun setupViewModel() {
        val database = RecipeDatabase.getInstance(requireContext())
        val repository = RecipeRepository(database.recipeDao(), database.shoppingListDao())
        val factory = ViewModelFactory(repository)
        viewModel = androidx.lifecycle.ViewModelProvider(this, factory).get(RecipeViewModel::class.java)
    }

    private fun setupDifficultySpinner() {
        val difficulties = arrayOf("Facile", "Moyen", "Difficile")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, difficulties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDifficulty.adapter = adapter
    }

    private fun checkEditMode() {
        arguments?.let { bundle ->
            recipeId = bundle.getLong("recipeId", -1L)

            if (recipeId != -1L) {
                isEditMode = true

                binding.editTextTitle.setText(bundle.getString("recipeTitle", ""))
                binding.editTextDescription.setText(bundle.getString("recipeDescription", ""))
                binding.editTextIngredients.setText(bundle.getString("recipeIngredients", ""))
                binding.editTextInstructions.setText(bundle.getString("recipeInstructions", ""))
                binding.editTextPreparationTime.setText(bundle.getInt("recipePreparationTime", 30).toString())

                val difficulty = bundle.getString("recipeDifficulty", "Facile")
                val difficulties = arrayOf("Facile", "Moyen", "Difficile")
                val position = difficulties.indexOf(difficulty)
                if (position >= 0) {
                    binding.spinnerDifficulty.setSelection(position)
                }

                val imageUri = bundle.getString("recipeImageUri", "recipe_default_1")
                selectedImageResId = when (imageUri) {
                    "recipe_default_1" -> R.drawable.recipe_default_1
                    "recipe_default_2" -> R.drawable.recipe_default_2
                    "recipe_default_3" -> R.drawable.recipe_default_3
                    else -> R.drawable.recipe_default_1
                }
                binding.imageRecipePreview.setImageResource(selectedImageResId)
                binding.imageRecipePreview.visibility = View.VISIBLE
                binding.textNoPhoto.visibility = View.GONE

                binding.toolbar.title = "Modifier la recette"
                binding.buttonSave.text = "Mettre à jour"
            }
        }
    }

    private fun setupImageButton() {
        binding.buttonAddPhoto.setOnClickListener {
            showRandomImage()
        }
        binding.buttonRemovePhoto.visibility = View.GONE
    }

    private fun showRandomImage() {
        selectedImageResId = defaultImages.random()
        binding.imageRecipePreview.setImageResource(selectedImageResId)
        binding.imageRecipePreview.visibility = View.VISIBLE
        binding.textNoPhoto.visibility = View.GONE

        binding.imageRecipePreview.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(200)
            .withEndAction {
                binding.imageRecipePreview.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
            }
    }

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener {
            if (validateForm()) {
                saveRecipe()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (binding.editTextTitle.text.toString().trim().isEmpty()) {
            binding.editTextTitle.error = "Le titre est obligatoire"
            isValid = false
        }

        if (binding.editTextIngredients.text.toString().trim().isEmpty()) {
            binding.editTextIngredients.error = "Les ingrédients sont obligatoires"
            isValid = false
        }

        if (binding.editTextInstructions.text.toString().trim().isEmpty()) {
            binding.editTextInstructions.error = "Les instructions sont obligatoires"
            isValid = false
        }

        return isValid
    }

    private fun saveRecipe() {
        val title = binding.editTextTitle.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val ingredients = binding.editTextIngredients.text.toString().trim()
        val instructions = binding.editTextInstructions.text.toString().trim()
        val preparationTime = binding.editTextPreparationTime.text.toString().toIntOrNull() ?: 30
        val difficulty = binding.spinnerDifficulty.selectedItem.toString()

        val imageResourceName = when (selectedImageResId) {
            R.drawable.recipe_default_1 -> "recipe_default_1"
            R.drawable.recipe_default_2 -> "recipe_default_2"
            R.drawable.recipe_default_3 -> "recipe_default_3"
            else -> "recipe_default_1"
        }

        val recipe = if (isEditMode) {
            Recipe(
                id = recipeId,
                title = title,
                description = description,
                ingredients = ingredients,
                instructions = instructions,
                preparationTime = preparationTime,
                difficulty = difficulty,
                imageUri = imageResourceName,
                createdAt = System.currentTimeMillis()
            )
        } else {
            Recipe(
                title = title,
                description = description,
                ingredients = ingredients,
                instructions = instructions,
                preparationTime = preparationTime,
                difficulty = difficulty,
                imageUri = imageResourceName
            )
        }

        binding.buttonSave.isEnabled = false
        binding.buttonSave.text = if (isEditMode) "Mise à jour..." else "Sauvegarde..."

        lifecycleScope.launch {
            try {
                val database = RecipeDatabase.getInstance(requireContext())

                if (isEditMode) {
                    database.recipeDao().updateRecipe(recipe)
                } else {
                    database.recipeDao().insertRecipe(recipe)
                }

                withContext(Dispatchers.Main) {
                    binding.buttonSave.isEnabled = true
                    binding.buttonSave.text = if (isEditMode) "Mettre à jour" else "Sauvegarder"

                    Toast.makeText(
                        requireContext(),
                        if (isEditMode) "Recette mise à jour!" else "Recette sauvegardée!",
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().popBackStack()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.buttonSave.isEnabled = true
                    binding.buttonSave.text = if (isEditMode) "Mettre à jour" else "Sauvegarder"

                    Toast.makeText(
                        requireContext(),
                        "Erreur: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}