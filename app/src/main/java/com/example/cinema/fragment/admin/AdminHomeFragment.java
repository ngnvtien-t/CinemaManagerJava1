package com.example.cinema.fragment.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cinema.MyApplication;
import com.example.cinema.R;
import com.example.cinema.activity.admin.AddMovieActivity;
import com.example.cinema.adapter.admin.AdminMovieAdapter;
import com.example.cinema.constant.ConstantKey;
import com.example.cinema.constant.GlobalFunction;
import com.example.cinema.databinding.FragmentAdminHomeBinding;
import com.example.cinema.model.Category;
import com.example.cinema.model.Movie;
import com.example.cinema.util.StringUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeFragment extends Fragment implements View.OnClickListener {

    private FragmentAdminHomeBinding mFragmentAdminHomeBinding;
    private List<Movie> mListMovies;
    private AdminMovieAdapter mAdminMovieAdapter;

    private List<Category> mListCategory;
    private Category mCategorySelected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentAdminHomeBinding = FragmentAdminHomeBinding.inflate(inflater, container, false);

        initListener();
        getListCategory();
        return mFragmentAdminHomeBinding.getRoot();
    }

    private void initListener() {
        mFragmentAdminHomeBinding.btnAddMovie.setOnClickListener(v -> onClickAddMovie());

        mFragmentAdminHomeBinding.imgSearch.setOnClickListener(view1 -> searchMovie());

        mFragmentAdminHomeBinding.edtSearchName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchMovie();
                return true;
            }
            return false;
        });

        mFragmentAdminHomeBinding.edtSearchName.addTextChangedListener(new TextWatcher() {
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
                    searchMovie();
                }
            }
        });
    }

    private void getListCategory() {
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
                        mListCategory.add(0, category);
                    }
                }
                mCategorySelected = new Category(0, getString(R.string.label_all), "");
                mListCategory.add(0, mCategorySelected);
                initLayoutCategory("0");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void initLayoutCategory(String tag) {
        mFragmentAdminHomeBinding.layoutCategory.removeAllViews();
        if (mListCategory != null && !mListCategory.isEmpty()) {
            for (int i = 0; i < mListCategory.size(); i++) {
                Category category = mListCategory.get(i);

                FlowLayout.LayoutParams params =
                        new FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT,
                                FlowLayout.LayoutParams.WRAP_CONTENT);
                TextView textView = new TextView(getActivity());
                params.setMargins(0, 10, 20, 10);
                textView.setLayoutParams(params);
                textView.setPadding(30, 10, 30, 10);
                textView.setTag(String.valueOf(category.getId()));
                textView.setText(category.getName());
                if (tag.equals(String.valueOf(category.getId()))) {
                    mCategorySelected = category;
                    textView.setBackgroundResource(R.drawable.bg_white_shape_round_corner_border_red);
                    textView.setTextColor(getResources().getColor(R.color.red));
                    searchMovie();
                } else {
                    textView.setBackgroundResource(R.drawable.bg_white_shape_round_corner_border_grey);
                    textView.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                textView.setTextSize(((int) getResources().getDimension(R.dimen.text_size_xsmall) /
                        getResources().getDisplayMetrics().density));
                textView.setOnClickListener(this);
                mFragmentAdminHomeBinding.layoutCategory.addView(textView);
            }
        }
    }

    private void onClickAddMovie() {
        GlobalFunction.startActivity(getActivity(), AddMovieActivity.class);
    }

    private void onClickEditMovie(Movie movie) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ConstantKey.KEY_INTENT_MOVIE_OBJECT, movie);
        GlobalFunction.startActivity(getActivity(), AddMovieActivity.class, bundle);
    }

    private void deleteMovieItem(Movie movie) {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok), (dialogInterface, i) -> {
                    if (getActivity() == null) {
                        return;
                    }
                    MyApplication.get(getActivity()).getMovieDatabaseReference()
                            .child(String.valueOf(movie.getId())).removeValue((error, ref) ->
                            Toast.makeText(getActivity(),
                                    getString(R.string.msg_delete_movie_successfully), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    public void searchMovie() {
        if (getActivity() == null) {
            return;
        }
        GlobalFunction.hideSoftKeyboard(getActivity());

        MyApplication.get(getActivity()).getMovieDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mListMovies != null) {
                    mListMovies.clear();
                } else {
                    mListMovies = new ArrayList<>();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Movie movie = dataSnapshot.getValue(Movie.class);
                    if (isMovieResult(movie)) {
                        mListMovies.add(0, movie);
                    }
                }
                loadListMovie();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadListMovie() {
        if (getActivity() == null) {
            return;
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mFragmentAdminHomeBinding.rcvMovie.setLayoutManager(linearLayoutManager);
        mAdminMovieAdapter = new AdminMovieAdapter(getActivity(), mListMovies, new AdminMovieAdapter.IManagerMovieListener() {
            @Override
            public void editMovie(Movie movie) {
                onClickEditMovie(movie);
            }

            @Override
            public void deleteMovie(Movie movie) {
                deleteMovieItem(movie);
            }

            @Override
            public void clickItemMovie(Movie movie) {

            }
        });
        mFragmentAdminHomeBinding.rcvMovie.setAdapter(mAdminMovieAdapter);
    }

    private boolean isMovieResult(Movie movie) {
        if (movie == null) {
            return false;
        }
        String key = mFragmentAdminHomeBinding.edtSearchName.getText().toString().trim();
        long categoryId = 0;
        if (mCategorySelected != null) {
            categoryId = mCategorySelected.getId();
        }
        if (StringUtil.isEmpty(key)) {
            if (categoryId == 0) {
                return true;
            } else return movie.getCategoryId() == categoryId;
        } else {
            boolean isMatch = GlobalFunction.getTextSearch(movie.getName()).toLowerCase().trim()
                    .contains(GlobalFunction.getTextSearch(key).toLowerCase().trim());
            if (categoryId == 0) {
                return isMatch;
            } else return isMatch && movie.getCategoryId() == categoryId;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdminMovieAdapter != null) {
            mAdminMovieAdapter.release();
        }
    }

    @Override
    public void onClick(View v) {
        initLayoutCategory(v.getTag().toString());
    }
}
