package com.example.cinema.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.cinema.MyApplication;
import com.example.cinema.activity.CategoryActivity;
import com.example.cinema.activity.SearchActivity;
import com.example.cinema.adapter.BannerMovieAdapter;
import com.example.cinema.adapter.CategoryAdapter;
import com.example.cinema.adapter.MovieAdapter;
import com.example.cinema.constant.ConstantKey;
import com.example.cinema.constant.GlobalFunction;
import com.example.cinema.databinding.FragmentHomeBinding;
import com.example.cinema.model.Category;
import com.example.cinema.model.Movie;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int MAX_BANNER_SIZE = 3;
    private FragmentHomeBinding mFragmentHomeBinding;

    private List<Movie> mListMovies;
    private List<Movie> mListMoviesBanner;
    private List<Category> mListCategory;

    private final Handler mHandlerBanner = new Handler(Looper.getMainLooper());
    private final Runnable mRunnableBanner = new Runnable() {
        @Override
        public void run() {
            if (mListMoviesBanner == null || mListMoviesBanner.isEmpty()) {
                return;
            }
            if (mFragmentHomeBinding.viewPager2.getCurrentItem() == mListMoviesBanner.size() - 1) {
                mFragmentHomeBinding.viewPager2.setCurrentItem(0);
                return;
            }
            mFragmentHomeBinding.viewPager2.setCurrentItem(mFragmentHomeBinding.viewPager2.getCurrentItem() + 1);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false);

        getListMovies();
        getListCategory();
        initListener();

        return mFragmentHomeBinding.getRoot();
    }

    private void initListener() {
        mFragmentHomeBinding.layoutSearch.setOnClickListener(v -> GlobalFunction.startActivity(getActivity(), SearchActivity.class));
    }

    private void getListMovies() {
        if (getActivity() == null) {
            return;
        }
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
                    if (movie != null) {
                        mListMovies.add(0, movie);
                    }
                }
                displayListBannerMovies();
                displayListAllMovies();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayListBannerMovies() {
        BannerMovieAdapter bannerMovieAdapter = new BannerMovieAdapter(getListBannerMovies(),
                movie -> GlobalFunction.goToMovieDetail(getActivity(), movie));
        mFragmentHomeBinding.viewPager2.setAdapter(bannerMovieAdapter);
        mFragmentHomeBinding.indicator3.setViewPager(mFragmentHomeBinding.viewPager2);

        mFragmentHomeBinding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mHandlerBanner.removeCallbacks(mRunnableBanner);
                mHandlerBanner.postDelayed(mRunnableBanner, 3000);
            }
        });
    }

    private List<Movie> getListBannerMovies() {
        if (mListMoviesBanner != null) {
            mListMoviesBanner.clear();
        } else {
            mListMoviesBanner = new ArrayList<>();
        }
        if (mListMovies == null || mListMovies.isEmpty()) {
            return mListMoviesBanner;
        }
        List<Movie> listClone = new ArrayList<>(mListMovies);
        listClone.sort((movie1, movie2) -> movie2.getBooked() - movie1.getBooked());

        for (Movie movie : listClone) {
            if (mListMoviesBanner.size() < MAX_BANNER_SIZE) {
                mListMoviesBanner.add(movie);
            }
        }
        return mListMoviesBanner;
    }

    private void displayListAllMovies() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mFragmentHomeBinding.rcvMovie.setLayoutManager(gridLayoutManager);
        MovieAdapter movieAdapter = new MovieAdapter(mListMovies,
                movie -> GlobalFunction.goToMovieDetail(getActivity(), movie));
        mFragmentHomeBinding.rcvMovie.setAdapter(movieAdapter);
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
                displayListCategories();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayListCategories() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        mFragmentHomeBinding.rcvCategory.setLayoutManager(linearLayoutManager);
        CategoryAdapter categoryAdapter = new CategoryAdapter(mListCategory, category -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable(ConstantKey.KEY_INTENT_CATEGORY_OBJECT, category);
            GlobalFunction.startActivity(getActivity(), CategoryActivity.class, bundle);
        });
        mFragmentHomeBinding.rcvCategory.setAdapter(categoryAdapter);
    }
}
