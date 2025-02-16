package com.example.mymealmatev1.data.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mymealmatev1.R;
import com.example.mymealmatev1.data.entity.ShoppingItem;
import java.util.ArrayList;
import java.util.List;

public class GroceryListAdapter extends RecyclerView.Adapter<GroceryListAdapter.GroceryListViewHolder> {
    private List<ShoppingItem> shoppingItems;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ShoppingItem item);
    }

    public GroceryListAdapter(List<ShoppingItem> shoppingItems, OnItemClickListener listener) {
        this.shoppingItems = new ArrayList<>(shoppingItems);
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroceryListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grocery_list, parent, false);
        return new GroceryListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroceryListViewHolder holder, int position) {
        ShoppingItem item = shoppingItems.get(position);

        // Set recipe name
        holder.itemName.setText(item.getItemName());

        // Set ingredients
        holder.ingredients.setText(item.getIngredients());

        // Set quantity with label
        String quantityText = "Qty: " + (item.getQuantity() != null ? item.getQuantity() : "1");
        holder.quantity.setText(quantityText);

        // Set store location
        String locationText = item.getStoreLocation() != null && !item.getStoreLocation().isEmpty()
                ? item.getStoreLocation()
                : "Location not set";
        holder.storeLocation.setText(locationText);

        // Show shared status if item is shared
        holder.sharedStatus.setVisibility(item.isShared() ? View.VISIBLE : View.GONE);

        // Set background tint based on shared status
        holder.itemView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
            item.isShared() ? 
            holder.itemView.getContext().getColor(R.color.shared_item_background) : 
            holder.itemView.getContext().getColor(R.color.default_item_background)
        ));

        // Click listeners - disable click for shared items
        holder.itemView.setEnabled(!item.isShared());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && !item.isShared()) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return shoppingItems != null ? shoppingItems.size() : 0;
    }

    public List<ShoppingItem> getItems() {
        return new ArrayList<>(shoppingItems);
    }

    public void updateItems(List<ShoppingItem> newItems) {
        if (newItems == null) {
            this.shoppingItems = new ArrayList<>();
        } else {
            this.shoppingItems = new ArrayList<>(newItems);
        }
        notifyDataSetChanged();
    }

    public ShoppingItem getShoppingItemAt(int position) {
        if (position >= 0 && position < shoppingItems.size()) {
            return shoppingItems.get(position);
        }
        return null;
    }

    public int getPosition(ShoppingItem item) {
        if (item == null) return -1;
        
        for (int i = 0; i < shoppingItems.size(); i++) {
            ShoppingItem current = shoppingItems.get(i);
            if (current.getId() == item.getId() || 
                (current.getItemName().equals(item.getItemName()) &&
                 current.getIngredients().equals(item.getIngredients()))) {
                return i;
            }
        }
        return -1;
    }

    static class GroceryListViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        TextView ingredients;
        TextView quantity;
        TextView storeLocation;
        TextView sharedStatus;

        public GroceryListViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            ingredients = itemView.findViewById(R.id.ingredients);
            quantity = itemView.findViewById(R.id.quantity);
            storeLocation = itemView.findViewById(R.id.storeLocation);
            sharedStatus = itemView.findViewById(R.id.sharedStatus);
        }
    }
}