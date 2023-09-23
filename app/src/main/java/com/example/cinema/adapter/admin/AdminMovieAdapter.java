package com.example.cinema.adapter.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinema.constant.ConstantKey;
import com.example.cinema.databinding.ItemMovieAdminBinding;
import com.example.cinema.model.Movie;
import com.example.cinema.util.GlideUtils;

import java.util.List;

public class AdminMovieAdapter extends RecyclerView.Adapter<AdminMovieAdapter.MovieViewHolder> {

    private Context mContext;
    private final List<Movie> mListMovies;
    private final IManagerMovieListener iManagerMovieListener;

    public interface IManagerMovieListener {
        void editMovie(Movie movie);

        void deleteMovie(Movie movie);

        void clickItemMovie(Movie movie);
    }

    public AdminMovieAdapter(Context mContext, List<Movie> mListMovies, IManagerMovieListener iManagerMovieListener) {
        this.mContext = mContext;
        this.mListMovies = mListMovies;
        this.iManagerMovieListener = iManagerMovieListener;
    }

    public void release() {
        if (mContext != null) {
            mContext = null;
        }
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMovieAdminBinding itemMovieAdminBinding = ItemMovieAdminBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MovieViewHolder(itemMovieAdminBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = mListMovies.get(position);
        if (movie == null) {
            return;
        }
        GlideUtils.loadUrl(movie.getImage(), holder.mItemMovieAdminBinding.imgMovie);
        holder.mItemMovieAdminBinding.tvName.setText(movie.getName());
        holder.mItemMovieAdminBinding.tvCategory.setText(movie.getCategoryName());
        holder.mItemMovieAdminBinding.tvDescription.setText(movie.getDescription());
        String strPrice = movie.getPrice() + ConstantKey.UNIT_CURRENCY_MOVIE;
        holder.mItemMovieAdminBinding.tvPrice.setText(strPrice);
        holder.mItemMovieAdminBinding.tvDate.setText(movie.getDate());

        holder.mItemMovieAdminBinding.imgEdit.setOnClickListener(v -> iManagerMovieListener.editMovie(movie));
        holder.mItemMovieAdminBinding.imgDelete.setOnClickListener(v -> iManagerMovieListener.deleteMovie(movie));
        holder.mItemMovieAdminBinding.layoutItem.setOnClickListener(v -> iManagerMovieListener.clickItemMovie(movie));
    }

    @Override
    public int getItemCount() {
        if (mListMovies != null) {
            return mListMovies.size();
        }
        return 0;
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {

        private final ItemMovieAdminBinding mItemMovieAdminBinding;

        public MovieViewHolder(@NonNull ItemMovieAdminBinding itemMovieAdminBinding) {
            super(itemMovieAdminBinding.getRoot());
            this.mItemMovieAdminBinding = itemMovieAdminBinding;
        }
    }
}
