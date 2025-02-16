package com.example.mymealmatev1.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "shopping_items")
public class ShoppingItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private String itemName;    // For recipe name
    private String ingredients;
    private String quantity;
    private String storeLocation;
    private boolean isShared;

    // Default constructor (required by Room)
    public ShoppingItem() {
    }

    // Constructor for items from meal plan
    public ShoppingItem(int userId, String itemName, String ingredients, String quantity, String storeLocation) {
        this.userId = userId;
        this.itemName = itemName;
        this.ingredients = ingredients;
        this.quantity = quantity;
        this.storeLocation = storeLocation;
        this.isShared = false;
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

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getStoreLocation() {
        return storeLocation;
    }

    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
    }

    public boolean isShared() {
        return isShared;
    }

    public void setShared(boolean shared) {
        isShared = shared;
    }
}
