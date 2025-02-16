package com.example.mymealmatev1.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.mymealmatev1.data.entity.MealPlan;
import com.example.mymealmatev1.data.entity.MealPlanWithRecipe;

import java.util.List;

@Dao
public interface MealPlanDao {
    @Insert
    long insert(MealPlan mealPlan);

    @Update
    void update(MealPlan mealPlan);

    @Delete
    void delete(MealPlan mealPlan);

    @Transaction
    @Query("SELECT * FROM meal_plan WHERE userId = :userId")
    LiveData<List<MealPlanWithRecipe>> getAllMealPlansForUser(int userId);

    @Transaction
    @Query("SELECT * FROM meal_plan WHERE userId = :userId AND day = :day")
    List<MealPlanWithRecipe> getMealPlansForDay(int userId, String day);

    @Query("DELETE FROM meal_plan WHERE userId = :userId AND day = :day")
    void deleteMealPlansForDay(int userId, String day);
}
