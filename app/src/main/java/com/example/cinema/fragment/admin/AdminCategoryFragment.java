package com.example.cinema.fragment.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cinema.MyApplication;
import com.example.cinema.R;
import com.example.cinema.activity.admin.AddCategoryActivity;
import com.example.cinema.adapter.admin.AdminCategoryAdapter;
import com.example.cinema.constant.ConstantKey;
import com.example.cinema.constant.GlobalFunction;
import com.example.cinema.databinding.FragmentAdminCategoryBinding;
import com.example.cinema.model.Category;
import com.example.cinema.util.StringUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoryFragment extends Fragment {

    private FragmentAdminCategoryBinding mFragmentAdminCategoryBinding;
    private List<Category> mListCategory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentAdminCategoryBinding = FragmentAdminCategoryBinding.inflate(inflater, container, false);

        initListener();
        getListCategory("");
        return mFragmentAdminCategoryBinding.getRoot();
    }

    private void initListener() {
        mFragmentAdminCategoryBinding.btnAddCategory.setOnClickListener(v -> onClickAddCategory());

        mFragmentAdminCategoryBinding.imgSearch.setOnClickListener(view1 -> searchCategory());

        mFragmentAdminCategoryBinding.edtSearchName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchCategory();
                return true;
            }
            return false;
        });

        mFragmentAdminCategoryBinding.edtSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String strKey = s.toString().trim();
                if (strKey.equals("") || strKey.length() == 0) {
                    getListCategory("");
                }
            }
        });
    }

    private void searchCategory() {
        String strKey = mFragmentAdminCategoryBinding.edtSearchName.getText().toString().trim();
        getListCategory(strKey);
        GlobalFunction.hideSoftKeyboard(getActivity());
    }

    private void onClickAddCategory() {
        GlobalFunction.startActivity(getActivity(), AddCategoryActivity.class);
    }

    private void onClickEditCategory(Category category) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ConstantKey.KEY_INTENT_CATEGORY_OBJECT, category);
        GlobalFunction.startActivity(getActivity(), AddCategoryActivity.class, bundle);
    }

    private void deleteCategoryItem(Category category) {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok), (dialogInterface, i) -> {
                    if (getActivity() == null) {
                        return;
                    }
                    MyApplication.get(getActivity()).getCategoryDatabaseReference()
                            .child(String.valueOf(category.getId())).removeValue((error, ref) ->
                            Toast.makeText(getActivity(),
                                    getString(R.string.msg_delete_category_successfully), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    public void getListCategory(String key) {
        if (getActivity() == null) {
            return;
        }
        MyApplication.get(getActivity()).getCategoryDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mListCategory != null) {
                    mListCategory.clear();
                } else {
                    mListCategory = new ArrayList<>();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Category category = dataSnapshot.getValue(Category.class);
                    if (category != null) {
                        if (StringUtil.isEmpty(key)) {
                            mListCategory.add(0, category);
                        } else {
                            if (GlobalFunction.getTextSearch(category.getName()).toLowerCase().trim()
                                    .contains(GlobalFunction.getTextSearch(key).toLowerCase().trim())) {
                                mListCategory.add(0, category);
                            }
                        }
                    }
                }
                loadListData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadListData() {
        if (getActivity() == null) {
            return;
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mFragmentAdminCategoryBinding.rcvCategory.setLayoutManager(linearLayoutManager);

        AdminCategoryAdapter adminCategoryAdapter = new AdminCategoryAdapter(mListCategory,
                new AdminCategoryAdapter.IManagerCategoryListener() {
                    @Override
                    public void editCategory(Category category) {
                        onClickEditCategory(category);
                    }

                    @Override
                    public void deleteCategory(Category category) {
                        deleteCategoryItem(category);
                    }
                });
        mFragmentAdminCategoryBinding.rcvCategory.setAdapter(adminCategoryAdapter);
    }
}
