package com.example.mymealmatev1.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mymealmatev1.data.dao.UserDao;
import com.example.mymealmatev1.data.dao.RecipeDao;
import com.example.mymealmatev1.data.dao.ShoppingItemDao;
import com.example.mymealmatev1.data.dao.MealPlanDao;
import com.example.mymealmatev1.data.entity.User;
import com.example.mymealmatev1.data.entity.Recipe;
import com.example.mymealmatev1.data.entity.ShoppingItem;
import com.example.mymealmatev1.data.entity.MealPlan;

@Database(entities = {
        User.class,
        Recipe.class,
        ShoppingItem.class,
        MealPlan.class,
}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "mealmate_db";
    private static AppDatabase instance;

    public abstract UserDao userDao();

    public abstract RecipeDao recipeDao();

    public abstract ShoppingItemDao shoppingItemDao();

    public abstract MealPlanDao mealPlanDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
