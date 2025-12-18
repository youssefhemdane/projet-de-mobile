package tn.isilan.projet.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tn.isilan.projet.R
import tn.isilan.projet.data.entities.Recipe

class SelectableRecipeAdapter(
    private val onSelectionChanged: (List<Recipe>) -> Unit
) : ListAdapter<Recipe, SelectableRecipeAdapter.SelectableRecipeViewHolder>(RecipeAdapter.DiffCallback) {

    private val selectedRecipes = mutableSetOf<Long>() // Stocke les IDs des recettes sélectionnées

    class SelectableRecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxSelect)
        val titleTextView: TextView = itemView.findViewById(R.id.textRecipeTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textRecipeDescription)
        val timeTextView: TextView = itemView.findViewById(R.id.textPreparationTime)
        val difficultyTextView: TextView = itemView.findViewById(R.id.textDifficulty)
        val imageView: ImageView = itemView.findViewById(R.id.imageRecipeThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableRecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return SelectableRecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectableRecipeViewHolder, position: Int) {
        val recipe = getItem(position)

        // Afficher les données
        holder.titleTextView.text = recipe.title
        holder.descriptionTextView.text = recipe.description
        holder.timeTextView.text = "${recipe.preparationTime} min"
        holder.difficultyTextView.text = recipe.difficulty

        // Afficher l'image (même logique que RecipeAdapter)
        // ... code pour l'image ...

        // Afficher la checkbox
        holder.checkBox.visibility = View.VISIBLE
        holder.checkBox.isChecked = selectedRecipes.contains(recipe.id)

        // Gérer la sélection
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedRecipes.add(recipe.id)
            } else {
                selectedRecipes.remove(recipe.id)
            }
            // Notifier le changement
            onSelectionChanged(getSelectedRecipes())
        }

        // Permettre de cliquer sur toute la carte
        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
        }
    }

    fun getSelectedRecipes(): List<Recipe> {
        return currentList.filter { selectedRecipes.contains(it.id) }
    }

    fun clearSelection() {
        selectedRecipes.clear()
        notifyDataSetChanged()
    }
}