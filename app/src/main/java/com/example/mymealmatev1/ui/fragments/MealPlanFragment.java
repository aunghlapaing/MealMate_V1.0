package com.example.mymealmatev1.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymealmatev1.R;
import com.example.mymealmatev1.data.AppDatabase;
import com.example.mymealmatev1.data.adapters.MealPlanAdapter;
import com.example.mymealmatev1.data.dao.MealPlanDao;
import com.example.mymealmatev1.data.dao.RecipeDao;
import com.example.mymealmatev1.data.entity.MealPlan;
import com.example.mymealmatev1.data.entity.MealPlanWithRecipe;
import com.example.mymealmatev1.data.entity.Recipe;
import com.example.mymealmatev1.ui.auth.LoginActivity;
import com.example.mymealmatev1.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MealPlanFragment extends Fragment implements MealPlanAdapter.OnMealPlanClickListener {
    private RecyclerView recyclerView;
    private TextView emptyView;
    private TextView weekLabel;
    private View editFormContainer;
    private MealPlanAdapter adapter;
    private FloatingActionButton fabAddMeal;
    private MealPlanDao mealPlanDao;
    private RecipeDao recipeDao;
    private ExecutorService executorService;
    private SessionManager sessionManager;
    private Spinner recipeSpinner;
    private Spinner daySpinner;
    private List<Recipe> availableRecipes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal_plan, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupFab();
        setupFormButtons(view);
        observeMealPlans();
        loadAvailableRecipes();
        
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        weekLabel = view.findViewById(R.id.weekLabel);
        fabAddMeal = view.findViewById(R.id.fabAddMeal);
        editFormContainer = view.findViewById(R.id.editFormContainer);
        recipeSpinner = view.findViewById(R.id.recipeSpinner);
        daySpinner = view.findViewById(R.id.daySpinner);
        
        sessionManager = new SessionManager(requireContext());
        AppDatabase db = AppDatabase.getInstance(getContext());
        mealPlanDao = db.mealPlanDao();
        recipeDao = db.recipeDao();
        executorService = Executors.newSingleThreadExecutor();

        // Initialize adapter with empty list
        adapter = new MealPlanAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Setup day spinner
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(requireContext(),
            R.array.days_of_week, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);
    }

    private void loadAvailableRecipes() {
        recipeDao.getAllRecipes().observe(getViewLifecycleOwner(), recipes -> {
            availableRecipes = recipes;
            ArrayAdapter<Recipe> recipeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, recipes);
            recipeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            recipeSpinner.setAdapter(recipeAdapter);
        });
    }

    private void setupFormButtons(View view) {
        MaterialButton saveButton = view.findViewById(R.id.saveButton);
        MaterialButton cancelButton = view.findViewById(R.id.cancelButton);

        saveButton.setOnClickListener(v -> saveMealPlan());
        cancelButton.setOnClickListener(v -> hideForm());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                MealPlanWithRecipe mealPlanWithRecipe = adapter.getMealPlanAt(position);
                
                if (direction == ItemTouchHelper.LEFT) {
                    deleteMealPlan(mealPlanWithRecipe);
                } else {
                    shareMealPlan(mealPlanWithRecipe);
                }
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void setupFab() {
        fabAddMeal.setOnClickListener(v -> {
            if (isGuestUser()) {
                showLoginDialog();
            } else {
                showForm();
            }
        });
    }

    private boolean isGuestUser() {
        return !sessionManager.isLoggedIn();
    }

    private void showLoginDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Guest User")
            .setMessage("You need to login to manage meal plans.")
            .setPositiveButton("Login", (dialog, which) -> {
                // Navigate to login
                Intent loginIntent = new Intent(requireActivity(), LoginActivity.class);
                startActivity(loginIntent);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showForm() {
        editFormContainer.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        fabAddMeal.setVisibility(View.GONE);
    }

    private void hideForm() {
        editFormContainer.setVisibility(View.GONE);
        fabAddMeal.setVisibility(View.VISIBLE);
        updateListVisibility();
    }

    private void updateListVisibility() {
        if (adapter != null && adapter.getItemCount() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void saveMealPlan() {
        if (isGuestUser()) {
            showLoginDialog();
            return;
        }

        Recipe selectedRecipe = (Recipe) recipeSpinner.getSelectedItem();
        String selectedDay = daySpinner.getSelectedItem().toString();

        if (selectedRecipe == null) {
            Toast.makeText(getContext(), "Please select a recipe", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            MealPlan newMealPlan = new MealPlan(sessionManager.getUserId(), selectedRecipe.getId(), selectedDay);
            mealPlanDao.insert(newMealPlan);

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Meal plan saved successfully", Toast.LENGTH_SHORT).show();
                hideForm();
            });
        });
    }

    private void observeMealPlans() {
        mealPlanDao.getAllMealPlansForUser(sessionManager.getUserId()).observe(getViewLifecycleOwner(), mealPlans -> {
            if (mealPlans != null) {
                adapter.updateMealPlans(mealPlans);
                updateListVisibility();
            }
        });
    }

    private void deleteMealPlan(MealPlanWithRecipe mealPlanWithRecipe) {
        if (isGuestUser()) {
            showLoginDialog();
            return;
        }

        if (mealPlanWithRecipe.mealPlan.getUserId() != sessionManager.getUserId()) {
            Toast.makeText(getContext(), "You can only delete your own meal plans", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Meal Plan")
            .setMessage("Are you sure you want to delete this meal plan?")
            .setPositiveButton("Delete", (dialog, which) -> {
                executorService.execute(() -> {
                    mealPlanDao.delete(mealPlanWithRecipe.mealPlan);
                });
                Snackbar.make(recyclerView, "Meal plan deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo", v -> {
                        executorService.execute(() -> {
                            mealPlanDao.insert(mealPlanWithRecipe.mealPlan);
                        });
                    }).show();
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                adapter.notifyDataSetChanged();
            })
            .show();
    }

    private void shareMealPlan(MealPlanWithRecipe mealPlanWithRecipe) {
        if (isGuestUser()) {
            showLoginDialog();
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Share Grocery List")
            .setMessage("Would you like to share this meal plan's grocery list via SMS?")
            .setPositiveButton("Share", (dialog, which) -> {
                showContactPickerDialog(mealPlanWithRecipe);
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                adapter.notifyDataSetChanged();
            })
            .show();
    }

    private void showContactPickerDialog(MealPlanWithRecipe mealPlanWithRecipe) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Enter Phone Number")
            .setView(R.layout.dialog_phone_input)
            .setPositiveButton("Send", (dialog, which) -> {
                String phoneNumber = "1234567890"; // This would come from the dialog input
                sendGroceryListSms(phoneNumber, mealPlanWithRecipe);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void sendGroceryListSms(String phoneNumber, MealPlanWithRecipe mealPlanWithRecipe) {
        try {
            String message = "Grocery list for " + mealPlanWithRecipe.getName() + ":\n" + mealPlanWithRecipe.getIngredients();
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getContext(), "Grocery list sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to send SMS", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMealPlanClick(MealPlanWithRecipe mealPlanWithRecipe) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(mealPlanWithRecipe.getName())
            .setMessage("Instructions:\n" + mealPlanWithRecipe.getInstructions() + "\n\nIngredients:\n" + mealPlanWithRecipe.getIngredients())
            .setPositiveButton("Close", null)
            .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}