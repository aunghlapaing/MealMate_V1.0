package com.example.mymealmatev1.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;

import com.example.mymealmatev1.R;
import com.example.mymealmatev1.data.AppDatabase;
import com.example.mymealmatev1.data.adapters.RecipeAdapter;
import com.example.mymealmatev1.data.dao.RecipeDao;
import com.example.mymealmatev1.data.entity.Recipe;
import com.example.mymealmatev1.ui.auth.LoginActivity;
import com.example.mymealmatev1.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecipesFragment extends Fragment implements RecipeAdapter.OnRecipeDeleteListener {
    private RecyclerView recyclerView;
    private TextView emptyView;
    private View editFormContainer;
    private RecipeAdapter adapter;
    private RecipeDao recipeDao;
    private ExecutorService executorService;
    private EditText nameInput, ingredientsInput, instructionsInput;
    private Recipe currentEditingRecipe;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipes, container, false);

        // Initialize session manager
        sessionManager = new SessionManager(requireContext());

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        editFormContainer = view.findViewById(R.id.editFormContainer);
        FloatingActionButton fab = view.findViewById(R.id.addRecipeFab);
        
        // Initialize form inputs
        nameInput = view.findViewById(R.id.recipeNameInput);
        ingredientsInput = view.findViewById(R.id.ingredientsInput);
        instructionsInput = view.findViewById(R.id.instructionsInput);
        
        // Initialize buttons
        MaterialButton saveButton = view.findViewById(R.id.saveButton);
        MaterialButton cancelButton = view.findViewById(R.id.cancelButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Initialize adapter with empty list
        adapter = new RecipeAdapter(new ArrayList<>(), this, this::showRecipeForm);
        recyclerView.setAdapter(adapter);

        // Initialize database and executor
        AppDatabase db = AppDatabase.getInstance(getContext());
        recipeDao = db.recipeDao();
        executorService = Executors.newSingleThreadExecutor();

        // Set up FAB click listener
        fab.setOnClickListener(v -> {
            if (isGuestUser()) {
                showLoginDialog();
            } else {
                showRecipeForm(null);
            }
        });

        // Set up form buttons
        saveButton.setOnClickListener(v -> saveRecipe());
        cancelButton.setOnClickListener(v -> hideRecipeForm());

        // Observe LiveData for all recipes (both guest and registered users can view)
        recipeDao.getAllRecipes().observe(getViewLifecycleOwner(), this::updateRecipeList);

        return view;
    }

    private boolean isGuestUser() {
        return !sessionManager.isLoggedIn();
    }

    private void showLoginDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Guest User")
            .setMessage("You are a guest user. Please login to create, edit, or delete recipes.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Login", (dialog, which) -> {
                Intent loginIntent = new Intent(requireActivity(), LoginActivity.class);
                startActivity(loginIntent);
            })
            .show();
    }

    private void updateRecipeList(List<Recipe> recipes) {
        if (recipes.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
        adapter.setRecipes(recipes);
        adapter.notifyDataSetChanged();
    }

    private void showRecipeForm(Recipe recipe) {
        if (isGuestUser()) {
            showLoginDialog();
            return;
        }

        currentEditingRecipe = recipe;
        
        // If editing, populate the fields
        if (recipe != null) {
            // Check if the recipe belongs to the current user
            if (recipe.getUserId() != getCurrentUserId()) {
                Toast.makeText(getContext(), "You can only edit your own recipes", Toast.LENGTH_SHORT).show();
                return;
            }
            nameInput.setText(recipe.getName());
            ingredientsInput.setText(recipe.getIngredients());
            instructionsInput.setText(recipe.getInstructions());
        } else {
            // Clear fields for new recipe
            nameInput.setText("");
            ingredientsInput.setText("");
            instructionsInput.setText("");
        }

        // Show the form
        editFormContainer.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }

    private void hideRecipeForm() {
        editFormContainer.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        
        // Check if we have any recipes
        List<Recipe> currentRecipes = adapter.getRecipes();
        if (currentRecipes.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        }
        currentEditingRecipe = null;
    }

    private void saveRecipe() {
        if (isGuestUser()) {
            showLoginDialog();
            return;
        }

        String name = nameInput.getText().toString().trim();
        String ingredients = ingredientsInput.getText().toString().trim();
        String instructions = instructionsInput.getText().toString().trim();

        if (name.isEmpty() || ingredients.isEmpty() || instructions.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = getCurrentUserId();

        executorService.execute(() -> {
            if (currentEditingRecipe != null) {
                // Verify ownership before updating
                if (currentEditingRecipe.getUserId() != userId) {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "You can only edit your own recipes", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }
                // Update existing recipe
                currentEditingRecipe.setName(name);
                currentEditingRecipe.setIngredients(ingredients);
                currentEditingRecipe.setInstructions(instructions);
                recipeDao.update(currentEditingRecipe);
            } else {
                // Create new recipe
                Recipe newRecipe = new Recipe(name, ingredients, instructions, userId);
                recipeDao.insert(newRecipe);
            }

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Recipe saved successfully", Toast.LENGTH_SHORT).show();
                hideRecipeForm();
            });
        });
    }

    @Override
    public void onRecipeDelete(Recipe recipe) {
        if (isGuestUser()) {
            showLoginDialog();
            return;
        }
        
        // Check if the recipe belongs to the current user
        if (recipe.getUserId() != getCurrentUserId()) {
            Toast.makeText(getContext(), "You can only delete your own recipes", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete this recipe?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete", (dialog, which) -> {
                executorService.execute(() -> {
                    recipeDao.delete(recipe);
                });
            })
            .show();
    }

    private int getCurrentUserId() {
        return sessionManager.getUserId();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}