package com.example.cinema.activity.admin;

import android.os.Bundle;
import android.widget.Toast;

import com.example.cinema.MyApplication;
import com.example.cinema.R;
import com.example.cinema.activity.BaseActivity;
import com.example.cinema.constant.ConstantKey;
import com.example.cinema.constant.GlobalFunction;
import com.example.cinema.databinding.ActivityAddFoodBinding;
import com.example.cinema.model.Food;
import com.example.cinema.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class AddFoodActivity extends BaseActivity {

    private ActivityAddFoodBinding mActivityAddFoodBinding;
    private boolean isUpdate;
    private Food mFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityAddFoodBinding = ActivityAddFoodBinding.inflate(getLayoutInflater());
        setContentView(mActivityAddFoodBinding.getRoot());

        Bundle bundleReceived = getIntent().getExtras();
        if (bundleReceived != null) {
            isUpdate = true;
            mFood = (Food) bundleReceived.get(ConstantKey.KEY_INTENT_FOOD_OBJECT);
        }

        initView();

        mActivityAddFoodBinding.imgBack.setOnClickListener(v -> onBackPressed());
        mActivityAddFoodBinding.btnAddOrEdit.setOnClickListener(v -> addOrEditFood());
    }

    private void initView() {
        if (isUpdate) {
            mActivityAddFoodBinding.tvTitle.setText(getString(R.string.edit_food_title));
            mActivityAddFoodBinding.btnAddOrEdit.setText(getString(R.string.action_edit));
            mActivityAddFoodBinding.edtName.setText(mFood.getName());
            mActivityAddFoodBinding.edtPrice.setText(String.valueOf(mFood.getPrice()));
            mActivityAddFoodBinding.edtQuantity.setText(String.valueOf(mFood.getQuantity()));
        } else {
            mActivityAddFoodBinding.tvTitle.setText(getString(R.string.add_food_title));
            mActivityAddFoodBinding.btnAddOrEdit.setText(getString(R.string.action_add));
        }
    }

    private void addOrEditFood() {
        String strName = mActivityAddFoodBinding.edtName.getText().toString().trim();
        String strPrice = mActivityAddFoodBinding.edtPrice.getText().toString().trim();
        String strQuantity = mActivityAddFoodBinding.edtQuantity.getText().toString().trim();

        if (StringUtil.isEmpty(strName)) {
            Toast.makeText(this, getString(R.string.msg_name_food_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strPrice)) {
            Toast.makeText(this, getString(R.string.msg_price_food_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strQuantity)) {
            Toast.makeText(this, getString(R.string.msg_quantity_food_require), Toast.LENGTH_SHORT).show();
            return;
        }

        // Update food
        if (isUpdate) {
            showProgressDialog(true);
            Map<String, Object> map = new HashMap<>();
            map.put("name", strName);
            map.put("price", Integer.parseInt(strPrice));

            map.put("quantity", Integer.parseInt(strQuantity));
            MyApplication.get(this).getFoodDatabaseReference()
                    .child(String.valueOf(mFood.getId())).updateChildren(map, (error, ref) -> {
                showProgressDialog(false);
                Toast.makeText(AddFoodActivity.this, getString(R.string.msg_edit_food_successfully), Toast.LENGTH_SHORT).show();
                GlobalFunction.hideSoftKeyboard(AddFoodActivity.this);
            });
            return;
        }

        // Add food
        showProgressDialog(true);
        long foodId = System.currentTimeMillis();

        Food food = new Food(foodId, strName, Integer.parseInt(strPrice), Integer.parseInt(strQuantity));
        MyApplication.get(this).getFoodDatabaseReference().child(String.valueOf(foodId)).setValue(food, (error, ref) -> {
            showProgressDialog(false);
            mActivityAddFoodBinding.edtName.setText("");
            mActivityAddFoodBinding.edtPrice.setText("");
            mActivityAddFoodBinding.edtQuantity.setText("");
            GlobalFunction.hideSoftKeyboard(this);
            Toast.makeText(this, getString(R.string.msg_add_food_successfully), Toast.LENGTH_SHORT).show();
        });
    }
}