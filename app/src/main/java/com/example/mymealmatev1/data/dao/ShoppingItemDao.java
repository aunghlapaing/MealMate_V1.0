package com.example.mymealmatev1.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mymealmatev1.data.entity.ShoppingItem;

import java.util.List;

@Dao
public interface ShoppingItemDao {
    @Insert
    long insert(ShoppingItem item);

    @Update
    void update(ShoppingItem item);

    @Delete
    void delete(ShoppingItem item);

    @Query("SELECT * FROM shopping_items WHERE userId = :userId AND isShared = 0")
    LiveData<List<ShoppingItem>> getUnsharedItemsForUser(int userId);

    @Query("SELECT * FROM shopping_items WHERE userId = :userId AND isShared = 1")
    LiveData<List<ShoppingItem>> getSharedItemsForUser(int userId);

    @Query("DELETE FROM shopping_items WHERE userId = :userId AND isShared = 1")
    void deleteAllSharedItems(int userId);

    @Query("SELECT * FROM shopping_items WHERE userId = :userId AND itemName = :recipeName AND ingredients = :ingredient AND isShared = 0 LIMIT 1")
    ShoppingItem findUnsharedItem(int userId, String recipeName, String ingredient);

    @Query("SELECT * FROM shopping_items WHERE userId = :userId AND itemName = :recipeName AND ingredients = :ingredient AND isShared = 1 LIMIT 1")
    ShoppingItem findSharedItem(int userId, String recipeName, String ingredient);
}
