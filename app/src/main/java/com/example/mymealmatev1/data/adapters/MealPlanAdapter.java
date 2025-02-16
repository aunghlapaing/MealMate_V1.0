package com.example.mymealmatev1.data.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymealmatev1.R;
import com.example.mymealmatev1.data.entity.MealPlanWithRecipe;

import java.util.List;

public class MealPlanAdapter extends RecyclerView.Adapter<MealPlanAdapter.MealPlanViewHolder> {
    private List<MealPlanWithRecipe> mealPlans;
    private final OnMealPlanClickListener listener;

    public interface OnMealPlanClickListener {
        void onMealPlanClick(MealPlanWithRecipe mealPlanWithRecipe);
    }

    public MealPlanAdapter(List<MealPlanWithRecipe> mealPlans, OnMealPlanClickListener listener) {
        this.mealPlans = mealPlans;
        this.listener = listener;
    }

    public void updateMealPlans(List<MealPlanWithRecipe> newMealPlans) {
        this.mealPlans = newMealPlans;
        notifyDataSetChanged();
    }

    public MealPlanWithRecipe getMealPlanAt(int position) {
        return mealPlans.get(position);
    }

    @NonNull
    @Override
    public MealPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_plan, parent, false);
        return new MealPlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealPlanViewHolder holder, int position) {
        MealPlanWithRecipe mealPlanWithRecipe = mealPlans.get(position);
        holder.bind(mealPlanWithRecipe);
    }

    @Override
    public int getItemCount() {
        return mealPlans != null ? mealPlans.size() : 0;
    }

    class MealPlanViewHolder extends RecyclerView.ViewHolder {
        private final TextView recipeName;
        private final TextView dayOfWeek;

        MealPlanViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.recipeName);
            dayOfWeek = itemView.findViewById(R.id.dayOfWeek);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMealPlanClick(mealPlans.get(position));
                }
            });
        }

        void bind(MealPlanWithRecipe mealPlanWithRecipe) {
            recipeName.setText(mealPlanWithRecipe.recipe.getName());
            dayOfWeek.setText(mealPlanWithRecipe.mealPlan.getDay());
        }
    }
}