package tn.isilan.projet.ui.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import tn.isilan.projet.databinding.FragmentAddRecipeBinding
import tn.isilan.projet.ui.viewmodels.RecipeViewModel
import tn.isilan.projet.ui.viewmodels.ViewModelFactory

class AddRecipeFragment : Fragment() {

    private var _binding: FragmentAddRecipeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecipeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser le ViewModel
        val database = tn.isilan.projet.data.database.RecipeDatabase.getInstance(requireContext())
        val repository = tn.isilan.projet.data.repository.RecipeRepository(database.recipeDao() , database.shoppingListDao())
        val factory = ViewModelFactory(repository)
        viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[RecipeViewModel::class.java]

        setupDifficultySpinner()
        setupSaveButton()
    }

    private fun setupDifficultySpinner() {
        val difficulties = arrayOf("Facile", "Moyen", "Difficile")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, difficulties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDifficulty.adapter = adapter
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

        viewModel.addRecipe(
            title = title,
            description = description,
            ingredients = ingredients,
            instructions = instructions,
            preparationTime = preparationTime,
            difficulty = difficulty
        )

        // Retour à la liste des recettes
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}