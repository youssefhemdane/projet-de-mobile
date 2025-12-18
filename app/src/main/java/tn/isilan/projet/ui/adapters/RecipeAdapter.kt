package tn.isilan.projet.ui.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tn.isilan.projet.R
import tn.isilan.projet.data.entities.Recipe

class RecipeAdapter(
    private val onItemClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }

    class RecipeViewHolder(
        itemView: View,
        private val onItemClick: (Recipe) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val titleTextView: TextView =
            itemView.findViewById(R.id.textRecipeTitle)
        private val descriptionTextView: TextView =
            itemView.findViewById(R.id.textRecipeDescription)
        private val timeTextView: TextView =
            itemView.findViewById(R.id.textPreparationTime)
        private val difficultyTextView: TextView =
            itemView.findViewById(R.id.textDifficulty)
        private val imageView: ImageView =
            itemView.findViewById(R.id.imageRecipeThumbnail)

        fun bind(recipe: Recipe) {
            titleTextView.text = recipe.title
            descriptionTextView.text = recipe.description
            timeTextView.text = "${recipe.preparationTime} min"
            difficultyTextView.text = recipe.difficulty

            loadRecipeImage(recipe)

            itemView.setOnClickListener {
                onItemClick(recipe)
            }
        }

        private fun loadRecipeImage(recipe: Recipe) {
            val imageUri = recipe.imageUri

            if (!imageUri.isNullOrEmpty() && imageUri.startsWith("/")) {
                // 📷 Image réelle depuis le stockage interne
                try {
                    val bitmap = BitmapFactory.decodeFile(imageUri)
                    imageView.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    imageView.setImageResource(R.drawable.recipe_default_1)
                }
            } else {
                // 🖼 Image par défaut (ressource)
                val imageResId = when (imageUri) {
                    "recipe_default_1" -> R.drawable.recipe_default_1
                    "recipe_default_2" -> R.drawable.recipe_default_2
                    "recipe_default_3" -> R.drawable.recipe_default_3
                    else -> R.drawable.recipe_default_1
                }
                imageView.setImageResource(imageResId)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(
        holder: RecipeViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }
}
