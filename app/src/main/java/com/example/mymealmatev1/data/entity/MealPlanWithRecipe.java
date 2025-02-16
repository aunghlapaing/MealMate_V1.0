package com.example.mymealmatev1.data.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class MealPlanWithRecipe {
    @Embedded
    public MealPlan mealPlan;

    @Relation(
        parentColumn = "recipeId",
        entityColumn = "id"
    )
    public Recipe recipe;

    public String getName() {
        return recipe != null ? recipe.getName() : "";
    }

    public String getInstructions() {
        return recipe != null ? recipe.getInstructions() : "";
    }

    public String getIngredients() {
        return recipe != null ? recipe.getIngredients() : "";
    }
}
