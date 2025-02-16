package com.example.mymealmatev1.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymealmatev1.R;
import com.example.mymealmatev1.data.AppDatabase;
import com.example.mymealmatev1.data.adapters.GroceryListAdapter;
import com.example.mymealmatev1.data.dao.MealPlanDao;
import com.example.mymealmatev1.data.dao.ShoppingItemDao;
import com.example.mymealmatev1.data.entity.MealPlanWithRecipe;
import com.example.mymealmatev1.data.entity.ShoppingItem;
import com.example.mymealmatev1.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroceryListFragment extends Fragment {
    private static final int SMS_PERMISSION_REQUEST = 1;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private GroceryListAdapter adapter;
    private ShoppingItemDao shoppingItemDao;
    private MealPlanDao mealPlanDao;
    private ExecutorService executorService;
    private SessionManager sessionManager;
    private Map<String, ShoppingItem> groceryItemsMap = new HashMap<>();
    private ShoppingItem pendingShareItem;
    private String pendingPhoneNumber;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission granted, proceed with SMS
                        if (pendingShareItem != null && pendingPhoneNumber != null) {
                            proceedWithSmsShare(pendingShareItem, pendingPhoneNumber);
                        }
                    } else {
                        // Permission denied
                        Toast.makeText(requireContext(),
                                "SMS permission denied. Cannot share grocery list.",
                                Toast.LENGTH_LONG).show();
                    }
                    // Clear pending items
                    pendingShareItem = null;
                    pendingPhoneNumber = null;
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grocery_list, container, false);

        // Initialize session manager
        sessionManager = new SessionManager(requireContext());

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);

        // Initialize database access
        AppDatabase db = AppDatabase.getInstance(requireContext());
        shoppingItemDao = db.shoppingItemDao();
        mealPlanDao = db.mealPlanDao();
        executorService = Executors.newSingleThreadExecutor();

        // Initialize RecyclerView
        setupRecyclerView();

        // Observe grocery list
        observeGroceryList();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new GroceryListAdapter(new ArrayList<ShoppingItem>(), item -> {
            if (!sessionManager.isLoggedIn()) {
                showLoginPrompt();
                return;
            }
            if (!item.isShared()) {
                showEditDialog(item);
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Add swipe to delete functionality
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ShoppingItem item = adapter.getShoppingItemAt(position);

                if (direction == ItemTouchHelper.LEFT) {
                    // Delete
                    if (item.isShared()) {
                        deleteItem(item);
                    } else {
                        // If not shared, restore the item
                        adapter.notifyItemChanged(position);
                        Toast.makeText(requireContext(), "Share the item first before deleting", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Share via SMS
                    if (!item.isShared()) {
                        shareViaMessage(item);
                    }
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }

    private void deleteItem(ShoppingItem item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Item")
                .setMessage("This item will be permanently deleted from your grocery list. Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    executorService.execute(() -> {
                        // Delete from database
                        shoppingItemDao.delete(item);

                        // Remove from map
                        String key = getItemKey(item);
                        groceryItemsMap.remove(key);

                        // Update UI on main thread
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Item permanently deleted", Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Restore the item view in the list
                    int position = adapter.getPosition(item);
                    if (position != -1) {
                        adapter.notifyItemChanged(position);
                    }
                })
                .show();
    }

    private String getItemKey(ShoppingItem item) {
        return item.getItemName() + "|" + item.getIngredients();
    }

    private void showEditDialog(ShoppingItem item) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_grocery_item, null);
        EditText quantityInput = dialogView.findViewById(R.id.quantityInput);
        EditText storeLocationInput = dialogView.findViewById(R.id.storeLocationInput);

        quantityInput.setText(item.getQuantity());
        storeLocationInput.setText(item.getStoreLocation());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit Item")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String quantity = quantityInput.getText().toString();
                    String storeLocation = storeLocationInput.getText().toString();

                    if (quantity.isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter a quantity", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update the item
                    item.setQuantity(quantity);
                    item.setStoreLocation(storeLocation);

                    // Save to database and update UI
                    executorService.execute(() -> {
                        shoppingItemDao.update(item);
                        // Update the item in our map
                        String key = getItemKey(item);
                        groceryItemsMap.put(key, item);

                        // Update UI on main thread
                        requireActivity().runOnUiThread(() -> {
                            int position = adapter.getPosition(item);
                            if (position != -1) {
                                adapter.notifyItemChanged(position);
                            }
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void shareViaMessage(ShoppingItem item) {
        showPhoneNumberDialog(phoneNumber -> {
            if (checkSmsPermission()) {
                proceedWithSmsShare(item, phoneNumber);
            } else {
                // Save pending share and request permission
                pendingShareItem = item;
                pendingPhoneNumber = phoneNumber;
                requestSmsPermission();
            }
        });
    }

    private boolean checkSmsPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermission() {
        requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
    }

    private void proceedWithSmsShare(ShoppingItem item, String phoneNumber) {
        String message = String.format("Grocery Item from %s:\nRecipe: %s\nIngredient: %s\nQuantity: %s\nStore Location: %s",
                sessionManager.getUsername(),
                item.getItemName(),
                item.getIngredients(),
                item.getQuantity(),
                item.getStoreLocation());

        // Create a new shared item
        ShoppingItem sharedItem = new ShoppingItem(
                sessionManager.getUserId(),
                item.getItemName(),
                item.getIngredients(),
                item.getQuantity(),
                item.getStoreLocation()
        );
        sharedItem.setShared(true);

        // Try to send SMS first
        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    parts,
                    null,
                    null
            );

            // SMS sent successfully, now save the shared item
            executorService.execute(() -> {
                // First check if this item is already shared
                ShoppingItem existingShared = shoppingItemDao.findSharedItem(
                        sessionManager.getUserId(),
                        item.getItemName(),
                        item.getIngredients()
                );

                if (existingShared == null) {
                    // Save new shared item
                    long id = shoppingItemDao.insert(sharedItem);
                    sharedItem.setId((int) id);

                    // Add to local map
                    String key = getItemKey(sharedItem);
                    groceryItemsMap.put(key, sharedItem);

                    // Update UI on main thread
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(),
                                "Item shared successfully and saved",
                                Toast.LENGTH_SHORT).show();
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(),
                                "Item was already shared",
                                Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (Exception e) {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(),
                        "Failed to send SMS: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            });
        }
    }

    private void showPhoneNumberDialog(PhoneNumberCallback callback) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_phone_number, null);
        EditText phoneInput = dialogView.findViewById(R.id.editTextPhone);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Enter Phone Number")
                .setView(dialogView)
                .setPositiveButton("Share", (dialog, which) -> {
                    String phoneNumber = phoneInput.getText().toString().trim();
                    if (phoneNumber.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Please enter a valid phone number",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    callback.onPhoneNumberEntered(phoneNumber);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void observeGroceryList() {
        if (!sessionManager.isLoggedIn()) {
            updateListVisibility();
            return;
        }

        // Observe meal plan ingredients and shared items
        mealPlanDao.getAllMealPlansForUser(sessionManager.getUserId()).observe(getViewLifecycleOwner(), mealPlans -> {
            shoppingItemDao.getSharedItemsForUser(sessionManager.getUserId()).observe(getViewLifecycleOwner(), sharedItems -> {
                executorService.execute(() -> {
                    // Convert shared items to a HashMap for quick lookup
                    Map<String, ShoppingItem> sharedItemsMap = new HashMap<>();
                    for (ShoppingItem item : sharedItems) {
                        sharedItemsMap.put(getItemKey(item), item);
                    }

                    // Create groceryItems list
                    List<ShoppingItem> groceryItems = new ArrayList<>();
                    Map<String, ShoppingItem> groceryItemsMap = new HashMap<>(); // Ensure this exists

                    for (MealPlanWithRecipe mealPlan : mealPlans) {
                        String recipeName = mealPlan.getName();
                        String[] ingredients = new String[]{mealPlan.getIngredients()};

                        for (String ingredient : ingredients) {
                            String key = recipeName + "|" + ingredient;

                            // Check if this item is shared
                            ShoppingItem sharedItem = sharedItemsMap.get(key);
                            if (sharedItem != null) {
                                groceryItems.add(sharedItem);
                                continue;
                            }

                            // Check if we have an existing unshared item
                            ShoppingItem existingItem = groceryItemsMap.get(key);
                            if (existingItem != null && !existingItem.isShared()) {
                                groceryItems.add(existingItem);
                            } else {
                                // Create new item
                                ShoppingItem newItem = new ShoppingItem(
                                        sessionManager.getUserId(),
                                        recipeName,
                                        ingredient,
                                        "1", // Default quantity
                                        "" // Empty store location
                                );
                                groceryItemsMap.put(key, newItem);
                                groceryItems.add(newItem);
                            }
                        }
                    }

                    // Update UI on main thread
                    requireActivity().runOnUiThread(() -> {
                        adapter.updateItems(groceryItems);
                        updateListVisibility();
                    });
                });
            });
        });
    }

    private void updateListVisibility() {
        if (adapter.getItemCount() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoginPrompt() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Login Required")
                .setMessage("Please log in to manage your grocery list")
                .setPositiveButton("OK", null)
                .show();
    }

    private interface PhoneNumberCallback {
        void onPhoneNumberEntered(String phoneNumber);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}