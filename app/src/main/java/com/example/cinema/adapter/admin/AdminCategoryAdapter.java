package com.example.cinema.adapter.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinema.databinding.ItemCategoryAdminBinding;
import com.example.cinema.model.Category;
import com.example.cinema.util.GlideUtils;

import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.CategoryViewHolder> {

    private final List<Category> mListCategory;
    private final IManagerCategoryListener iManagerCategoryListener;

    public interface IManagerCategoryListener {
        void editCategory(Category category);

        void deleteCategory(Category category);
    }

    public AdminCategoryAdapter(List<Category> mListCategory, IManagerCategoryListener iManagerCategoryListener) {
        this.mListCategory = mListCategory;
        this.iManagerCategoryListener = iManagerCategoryListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryAdminBinding itemCategoryAdminBinding = ItemCategoryAdminBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(itemCategoryAdminBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = mListCategory.get(position);
        if (category == null) {
            return;
        }
        GlideUtils.loadUrl(category.getImage(), holder.mItemCategoryAdminBinding.imgCategory);
        holder.mItemCategoryAdminBinding.tvCategoryName.setText(category.getName());
        holder.mItemCategoryAdminBinding.imgEdit.setOnClickListener(v -> iManagerCategoryListener.editCategory(category));
        holder.mItemCategoryAdminBinding.imgDelete.setOnClickListener(v -> iManagerCategoryListener.deleteCategory(category));
    }

    @Override
    public int getItemCount() {
        if (mListCategory != null) {
            return mListCategory.size();
        }
        return 0;
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {

        private final ItemCategoryAdminBinding mItemCategoryAdminBinding;

        public CategoryViewHolder(@NonNull ItemCategoryAdminBinding itemCategoryAdminBinding) {
            super(itemCategoryAdminBinding.getRoot());
            this.mItemCategoryAdminBinding = itemCategoryAdminBinding;
        }
    }
}
