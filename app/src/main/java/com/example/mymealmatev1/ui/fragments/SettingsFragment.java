package com.example.mymealmatev1.ui.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.mymealmatev1.R;
import com.example.mymealmatev1.data.AppDatabase;
import com.example.mymealmatev1.data.dao.UserDao;
import com.example.mymealmatev1.data.entity.User;
import com.example.mymealmatev1.ui.auth.LoginActivity;
import com.example.mymealmatev1.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {
    private SessionManager sessionManager;
    private TextView profileSettings;
    private TextView logoutButton;
    private TextView loginButton;
    private TextView usernameText;
    private TextView deleteAccountButton;
    private LinearLayout accountSection;
    private UserDao userDao;
    private ExecutorService executorService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        initializeViews(view);
        setupClickListeners();
        updateUI();
        
        return view;
    }

    private void initializeViews(View view) {
        sessionManager = new SessionManager(requireContext());
        AppDatabase db = AppDatabase.getInstance(requireContext());
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();
        
        // Initialize views
        profileSettings = view.findViewById(R.id.profile_settings);
        logoutButton = view.findViewById(R.id.logout_button);
        loginButton = view.findViewById(R.id.login_button);
        usernameText = view.findViewById(R.id.username_text);
        accountSection = view.findViewById(R.id.account_section);
        deleteAccountButton = view.findViewById(R.id.delete_account_button);
    }

    private void setupClickListeners() {
        logoutButton.setOnClickListener(v -> handleLogout());
        profileSettings.setOnClickListener(v -> handleProfileSettings());
        loginButton.setOnClickListener(v -> navigateToLogin());
        deleteAccountButton.setOnClickListener(v -> handleDeleteAccount());
    }

    private void updateUI() {
        if (sessionManager.isLoggedIn()) {
            // Show logged-in user UI
            accountSection.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
            usernameText.setText(sessionManager.getUsername());
            logoutButton.setVisibility(View.VISIBLE);
            profileSettings.setVisibility(View.VISIBLE);
            deleteAccountButton.setVisibility(View.VISIBLE);
        } else {
            // Show guest user UI
            accountSection.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
            profileSettings.setVisibility(View.GONE);
            deleteAccountButton.setVisibility(View.GONE);
        }
    }

    private void handleLogout() {
        sessionManager.logout();
        updateUI();
    }

    private void handleProfileSettings() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_edit_profile);

        // Initialize dialog views
        TextInputEditText editName = dialog.findViewById(R.id.edit_name);
        TextInputEditText editEmail = dialog.findViewById(R.id.edit_email);
        TextInputEditText editPassword = dialog.findViewById(R.id.edit_password);
        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel);
        MaterialButton btnUpdate = dialog.findViewById(R.id.btn_update);

        // Get current user data
        executorService.execute(() -> {
            User user = userDao.getUserById(sessionManager.getUserId());
            if (user != null) {
                requireActivity().runOnUiThread(() -> {
                    editName.setText(user.getUsername());
                    editEmail.setText(user.getEmail());
                    // Don't set password for security reasons
                });
            }
        });

        // Set click listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnUpdate.setOnClickListener(v -> {
            String newName = editName.getText().toString().trim();
            String newEmail = editEmail.getText().toString().trim();
            String newPassword = editPassword.getText().toString().trim();

            if (newName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(requireContext(), "Name and email are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update user in database
            executorService.execute(() -> {
                User user = userDao.getUserById(sessionManager.getUserId());
                if (user != null) {
                    user.setUsername(newName);
                    user.setEmail(newEmail);
                    if (!newPassword.isEmpty()) {
                        user.setPassword(newPassword);
                    }
                    userDao.update(user);

                    requireActivity().runOnUiThread(() -> {
                        // Update session
                        sessionManager.getUsername();
                        
                        // Update UI
                        updateUI();
                        
                        // Show success message
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        
                        // Dismiss dialog
                        dialog.dismiss();
                    });
                }
            });
        });

        dialog.show();
    }

    private void handleDeleteAccount() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                executorService.execute(() -> {
                    User user = userDao.getUserById(sessionManager.getUserId());
                    if (user != null) {
                        userDao.delete(user);
                        
                        requireActivity().runOnUiThread(() -> {
                            // Clear session
                            sessionManager.logout();
                            
                            // Update UI
                            updateUI();
                            
                            // Show success message
                            Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
