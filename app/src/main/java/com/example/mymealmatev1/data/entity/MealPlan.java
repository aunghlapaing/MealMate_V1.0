package com.example.mymealmatev1.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "meal_plan")
public class MealPlan {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private int recipeId;
    private String day;

    // Default constructor (required by Room)
    public MealPlan() {

    }

    // Parameterized constructor (optional)
    public MealPlan(int userId, int recipeId, String day) {
        this.userId = userId;
        this.recipeId = recipeId;
        this.day = day;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }
}
