package com.example.mymealmatev1.data.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymealmatev1.R;
import com.example.mymealmatev1.data.entity.Recipe;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipes;
    private final OnRecipeDeleteListener deleteListener;
    private final OnRecipeEditListener editListener;

    public interface OnRecipeDeleteListener {
        void onRecipeDelete(Recipe recipe);
    }

    public interface OnRecipeEditListener {
        void onRecipeEdit(Recipe recipe);
    }

    public RecipeAdapter(List<Recipe> recipes, OnRecipeDeleteListener deleteListener, OnRecipeEditListener editListener) {
        this.recipes = recipes;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final TextView recipeName;
        private final TextView recipeIngredients;
        private final TextView recipeInstructions;
        private final ImageButton deleteButton;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.recipeName);
            recipeIngredients = itemView.findViewById(R.id.recipeIngredients);
            recipeInstructions = itemView.findViewById(R.id.recipeInstructions);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            // Set click listener for the entire item view
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && editListener != null) {
                    editListener.onRecipeEdit(recipes.get(position));
                }
            });

            // Set click listener for delete button
            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && deleteListener != null) {
                    deleteListener.onRecipeDelete(recipes.get(position));
                }
            });
        }

        public void bind(Recipe recipe) {
            recipeName.setText(recipe.getName());
            recipeIngredients.setText(recipe.getIngredients());
            recipeInstructions.setText(recipe.getInstructions());
        }
    }
}
