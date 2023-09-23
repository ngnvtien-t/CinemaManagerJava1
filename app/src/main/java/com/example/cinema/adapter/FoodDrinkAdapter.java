package com.example.cinema.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinema.R;
import com.example.cinema.constant.ConstantKey;
import com.example.cinema.databinding.ItemFoodDrinkBinding;
import com.example.cinema.model.Food;
import com.example.cinema.util.StringUtil;

import java.util.List;

public class FoodDrinkAdapter extends RecyclerView.Adapter<FoodDrinkAdapter.FoodDrinkViewHolder> {

    private final List<Food> mListFood;

    private final IManagerFoodDrinkListener iManagerFoodDrinkListener;

    public interface IManagerFoodDrinkListener {
        void selectCount(Food food, int count);
    }

    public FoodDrinkAdapter(List<Food> mListFood, IManagerFoodDrinkListener iManagerFoodDrinkListener) {
        this.mListFood = mListFood;
        this.iManagerFoodDrinkListener = iManagerFoodDrinkListener;
    }

    @NonNull
    @Override
    public FoodDrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodDrinkBinding itemFoodDrinkBinding = ItemFoodDrinkBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FoodDrinkViewHolder(itemFoodDrinkBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodDrinkViewHolder holder, int position) {
        Food food = mListFood.get(position);
        if (food == null) {
            return;
        }
        holder.mItemFoodDrinkBinding.tvNameFood.setText(food.getName());
        String strPrice = food.getPrice() + ConstantKey.UNIT_CURRENCY;
        holder.mItemFoodDrinkBinding.tvPriceFood.setText(strPrice);
        holder.mItemFoodDrinkBinding.tvStock.setText(String.valueOf(food.getQuantity()));
        holder.mItemFoodDrinkBinding.edtCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!StringUtil.isEmpty(editable.toString().trim())) {
                    int count = Integer.parseInt(editable.toString());
                    if (count > food.getQuantity()) {
                        Context context = holder.mItemFoodDrinkBinding.edtCount.getContext();
                        Toast.makeText(context, context.getString(R.string.msg_count_invalid), Toast.LENGTH_SHORT).show();
                        holder.mItemFoodDrinkBinding.edtCount.setText("");
                    } else {
                        iManagerFoodDrinkListener.selectCount(food, count);
                    }
                } else {
                    iManagerFoodDrinkListener.selectCount(food, 0);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mListFood != null) {
            return mListFood.size();
        }
        return 0;
    }

    public static class FoodDrinkViewHolder extends RecyclerView.ViewHolder {

        private final ItemFoodDrinkBinding mItemFoodDrinkBinding;

        public FoodDrinkViewHolder(@NonNull ItemFoodDrinkBinding itemFoodDrinkBinding) {
            super(itemFoodDrinkBinding.getRoot());
            this.mItemFoodDrinkBinding = itemFoodDrinkBinding;
        }
    }
}
