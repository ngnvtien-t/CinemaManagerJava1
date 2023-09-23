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
import com.example.cinema.activity.admin.AddFoodActivity;
import com.example.cinema.adapter.admin.AdminFoodAdapter;
import com.example.cinema.constant.ConstantKey;
import com.example.cinema.constant.GlobalFunction;
import com.example.cinema.databinding.FragmentAdminFoodBinding;
import com.example.cinema.model.Food;
import com.example.cinema.util.StringUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminFoodFragment extends Fragment {

    private FragmentAdminFoodBinding mFragmentAdminFoodBinding;
    private List<Food> mListFood;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentAdminFoodBinding = FragmentAdminFoodBinding.inflate(inflater, container, false);

        getListFoods("");
        initListener();

        return mFragmentAdminFoodBinding.getRoot();
    }

    private void initListener() {
        mFragmentAdminFoodBinding.btnAddFood.setOnClickListener(v -> onClickAddFood());

        mFragmentAdminFoodBinding.imgSearch.setOnClickListener(view1 -> searchFood());

        mFragmentAdminFoodBinding.edtSearchName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchFood();
                return true;
            }
            return false;
        });

        mFragmentAdminFoodBinding.edtSearchName.addTextChangedListener(new TextWatcher() {
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
                    getListFoods("");
                }
            }
        });
    }

    private void searchFood() {
        String strKey = mFragmentAdminFoodBinding.edtSearchName.getText().toString().trim();
        getListFoods(strKey);
        GlobalFunction.hideSoftKeyboard(getActivity());
    }


    private void onClickAddFood() {
        GlobalFunction.startActivity(getActivity(), AddFoodActivity.class);
    }

    private void onClickEditFood(Food food) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ConstantKey.KEY_INTENT_FOOD_OBJECT, food);
        GlobalFunction.startActivity(getActivity(), AddFoodActivity.class, bundle);
    }

    private void deleteFoodItem(Food food) {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok), (dialogInterface, i) -> {
                    if (getActivity() == null) {
                        return;
                    }
                    MyApplication.get(getActivity()).getFoodDatabaseReference()
                            .child(String.valueOf(food.getId())).removeValue((error, ref) ->
                            Toast.makeText(getActivity(),
                                    getString(R.string.msg_delete_food_successfully), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    public void getListFoods(String key) {
        if (getActivity() == null) {
            return;
        }
        MyApplication.get(getActivity()).getFoodDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mListFood != null) {
                    mListFood.clear();
                } else {
                    mListFood = new ArrayList<>();
                }

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Food food = dataSnapshot.getValue(Food.class);
                    if (food != null) {
                        if (StringUtil.isEmpty(key)) {
                            mListFood.add(0, food);
                        } else {
                            if (GlobalFunction.getTextSearch(food.getName()).toLowerCase().trim()
                                    .contains(GlobalFunction.getTextSearch(key).toLowerCase().trim())) {
                                mListFood.add(0, food);
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
        mFragmentAdminFoodBinding.rcvFood.setLayoutManager(linearLayoutManager);

        AdminFoodAdapter adminFoodAdapter = new AdminFoodAdapter(mListFood, new AdminFoodAdapter.IManagerFoodListener() {
            @Override
            public void editFood(Food food) {
                onClickEditFood(food);
            }

            @Override
            public void deleteFood(Food food) {
                deleteFoodItem(food);
            }
        });
        mFragmentAdminFoodBinding.rcvFood.setAdapter(adminFoodAdapter);
    }
}
