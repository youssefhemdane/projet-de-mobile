package tn.isilan.projet.ui.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope  // <-- AJOUTE CET IMPORT
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers  // <-- AJOUTE CET IMPORT
import kotlinx.coroutines.launch  // <-- AJOUTE CET IMPORT
import kotlinx.coroutines.withContext  // <-- AJOUTE CET IMPORT
import tn.isilan.projet.R
import tn.isilan.projet.data.database.RecipeDatabase
import tn.isilan.projet.data.entities.Recipe
import tn.isilan.projet.data.repository.RecipeRepository
import tn.isilan.projet.databinding.FragmentAddRecipeBinding
import tn.isilan.projet.ui.viewmodels.RecipeViewModel
import tn.isilan.projet.ui.viewmodels.ViewModelFactory
import kotlin.random.Random

class AddRecipeFragment : Fragment() {

    private var _binding: FragmentAddRecipeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecipeViewModel

    // Liste des images par défaut
    private val defaultImages = listOf(
        R.drawable.recipe_default_1,
        R.drawable.recipe_default_2,
        R.drawable.recipe_default_3
    )

    // Image sélectionnée (ressource ID)
    private var selectedImageResId: Int = R.drawable.recipe_default_1

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

        // Afficher une image aléatoire par défaut
        showRandomImage()
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

    private fun setupImageButton() {
        // Bouton pour changer d'image aléatoirement
        binding.buttonAddPhoto.setOnClickListener {
            showRandomImage()
        }

        // Cache le bouton supprimer (pas besoin)
        binding.buttonRemovePhoto.visibility = View.GONE
    }

    private fun showRandomImage() {
        // Choisir une image aléatoire
        selectedImageResId = defaultImages.random()

        // Afficher l'image
        binding.imageRecipePreview.setImageResource(selectedImageResId)
        binding.imageRecipePreview.visibility = View.VISIBLE
        binding.textNoPhoto.visibility = View.GONE

        // Petit effet visuel
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

        // Sauvegarder le nom de la ressource d'image
        val imageResourceName = when (selectedImageResId) {
            R.drawable.recipe_default_1 -> "recipe_default_1"
            R.drawable.recipe_default_2 -> "recipe_default_2"
            R.drawable.recipe_default_3 -> "recipe_default_3"
            else -> "recipe_default_1"
        }

        val recipe = Recipe(
            title = title,
            description = description,
            ingredients = ingredients,
            instructions = instructions,
            preparationTime = preparationTime,
            difficulty = difficulty,
            imageUri = imageResourceName
        )

        binding.buttonSave.isEnabled = false
        binding.buttonSave.text = "Sauvegarde..."

        // CORRECTION: Utilise lifecycleScope au lieu de runBlocking
        lifecycleScope.launch {
            try {
                // Appelle directement la fonction suspend
                val database = RecipeDatabase.getInstance(requireContext())
                database.recipeDao().insertRecipe(recipe)

                // Retour sur le thread principal
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    binding.buttonSave.isEnabled = true
                    binding.buttonSave.text = "Sauvegarder la recette"

                    Toast.makeText(
                        requireContext(),
                        "Recette sauvegardée avec une belle image!",
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().popBackStack()
                }

            } catch (e: Exception) {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    binding.buttonSave.isEnabled = true
                    binding.buttonSave.text = "Sauvegarder la recette"

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