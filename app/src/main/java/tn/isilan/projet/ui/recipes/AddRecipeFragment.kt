package tn.isilan.projet.ui.recipes

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import tn.isilan.projet.R
import tn.isilan.projet.data.database.RecipeDatabase
import tn.isilan.projet.data.entities.Recipe
import tn.isilan.projet.databinding.FragmentAddRecipeBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddRecipeFragment : Fragment() {

    private var _binding: FragmentAddRecipeBinding? = null
    private val binding get() = _binding!!

    // Images par défaut
    private val defaultImages = listOf(
        R.drawable.recipe_default_1,
        R.drawable.recipe_default_2,
        R.drawable.recipe_default_3
    )

    private var selectedImageResId: Int = R.drawable.recipe_default_1
    private var selectedImagePath: String? = null

    // Sélecteur d'image (API moderne)
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImagePath = copyImageToInternalStorage(it)
                displaySelectedImage(selectedImagePath)
                binding.buttonRemovePhoto.visibility = View.VISIBLE
            }
        }

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

        setupDifficultySpinner()
        setupImageButtons()
        setupSaveButton()

        showRandomImage()
    }

    // ================= IMAGE =================

    private fun setupImageButtons() {
        binding.buttonAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.buttonRemovePhoto.setOnClickListener {
            removeSelectedImage()
        }

        binding.buttonRemovePhoto.visibility = View.GONE
    }

    private fun showRandomImage() {
        selectedImageResId = defaultImages.random()
        selectedImagePath = null

        binding.imageRecipePreview.setImageResource(selectedImageResId)
        binding.imageRecipePreview.visibility = View.VISIBLE
        binding.textNoPhoto.visibility = View.GONE
        binding.buttonRemovePhoto.visibility = View.GONE
    }

    private fun displaySelectedImage(imagePath: String?) {
        if (imagePath.isNullOrEmpty()) {
            showRandomImage()
            return
        }

        val bitmap = BitmapFactory.decodeFile(imagePath)
        binding.imageRecipePreview.setImageBitmap(bitmap)
        binding.imageRecipePreview.visibility = View.VISIBLE
        binding.textNoPhoto.visibility = View.GONE
    }

    private fun removeSelectedImage() {
        selectedImagePath = null
        showRandomImage()
    }

    private fun copyImageToInternalStorage(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(requireContext().filesDir, "recipe_$timeStamp.jpg")

        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file.absolutePath
    }

    // ================= FORM =================

    private fun setupDifficultySpinner() {
        val difficulties = arrayOf("Facile", "Moyen", "Difficile")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            difficulties
        )
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
            binding.editTextTitle.error = "Titre obligatoire"
            isValid = false
        }

        if (binding.editTextIngredients.text.toString().trim().isEmpty()) {
            binding.editTextIngredients.error = "Ingrédients obligatoires"
            isValid = false
        }

        if (binding.editTextInstructions.text.toString().trim().isEmpty()) {
            binding.editTextInstructions.error = "Instructions obligatoires"
            isValid = false
        }

        return isValid
    }

    // ================= SAVE =================

    private fun saveRecipe() {
        val recipe = Recipe(
            title = binding.editTextTitle.text.toString().trim(),
            description = binding.editTextDescription.text.toString().trim(),
            ingredients = binding.editTextIngredients.text.toString().trim(),
            instructions = binding.editTextInstructions.text.toString().trim(),
            preparationTime = binding.editTextPreparationTime.text.toString().toIntOrNull() ?: 30,
            difficulty = binding.spinnerDifficulty.selectedItem.toString(),
            imageUri = selectedImagePath ?: getDefaultImageName()
        )

        lifecycleScope.launch {
            RecipeDatabase.getInstance(requireContext())
                .recipeDao()
                .insertRecipe(recipe)

            Toast.makeText(
                requireContext(),
                "Recette sauvegardée avec succès",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().popBackStack()
        }
    }

    private fun getDefaultImageName(): String {
        return when (selectedImageResId) {
            R.drawable.recipe_default_1 -> "recipe_default_1"
            R.drawable.recipe_default_2 -> "recipe_default_2"
            R.drawable.recipe_default_3 -> "recipe_default_3"
            else -> "recipe_default_1"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
