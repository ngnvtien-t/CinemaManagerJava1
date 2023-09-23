package com.example.cinema.adapter.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinema.constant.ConstantKey;
import com.example.cinema.databinding.ItemFoodBinding;
import com.example.cinema.model.Food;

import java.util.List;

public class AdminFoodAdapter extends RecyclerView.Adapter<AdminFoodAdapter.FoodViewHolder> {

    private final List<Food> mListFood;
    private final IManagerFoodListener iManagerFoodListener;

    public interface IManagerFoodListener {
        void editFood(Food food);

        void deleteFood(Food food);
    }

    public AdminFoodAdapter(List<Food> list, IManagerFoodListener listener) {
        this.mListFood = list;
        this.iManagerFoodListener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodBinding itemFoodBinding = ItemFoodBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FoodViewHolder(itemFoodBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = mListFood.get(position);
        if (food == null) {
            return;
        }
        holder.mItemFoodBinding.tvName.setText(food.getName());
        String strPrice = food.getPrice() + ConstantKey.UNIT_CURRENCY;
        holder.mItemFoodBinding.tvPrice.setText(strPrice);
        holder.mItemFoodBinding.tvQuantity.setText(String.valueOf(food.getQuantity()));
        holder.mItemFoodBinding.imgEdit.setOnClickListener(v -> iManagerFoodListener.editFood(food));
        holder.mItemFoodBinding.imgDelete.setOnClickListener(v -> iManagerFoodListener.deleteFood(food));
    }

    @Override
    public int getItemCount() {
        if (mListFood != null) {
            return mListFood.size();
        }
        return 0;
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {

        private final ItemFoodBinding mItemFoodBinding;

        public FoodViewHolder(@NonNull ItemFoodBinding itemFoodBinding) {
            super(itemFoodBinding.getRoot());
            this.mItemFoodBinding = itemFoodBinding;
        }
    }
}
