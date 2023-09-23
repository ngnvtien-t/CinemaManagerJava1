package com.example.cinema.activity;

import android.animation.ObjectAnimator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cinema.MyApplication;
import com.example.cinema.R;
import com.example.cinema.constant.ConstantKey;
import com.example.cinema.constant.GlobalFunction;
import com.example.cinema.databinding.ActivityMovieDetailBinding;
import com.example.cinema.model.Movie;
import com.example.cinema.util.DateTimeUtils;
import com.example.cinema.util.GlideUtils;
import com.example.cinema.util.StringUtil;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MovieDetailActivity extends AppCompatActivity {

    private ActivityMovieDetailBinding mActivityMovieDetailBinding;
    private Movie mMovie;

    private ExtractorMediaSource mMediaSource;
    private SimpleExoPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityMovieDetailBinding = ActivityMovieDetailBinding.inflate(getLayoutInflater());
        setContentView(mActivityMovieDetailBinding.getRoot());

        getDataIntent();
    }

    private void getDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }
        Movie movie = (Movie) bundle.get(ConstantKey.KEY_INTENT_MOVIE_OBJECT);
        getMovieInformation(movie.getId());
    }

    private void getMovieInformation(long movieId) {
        MyApplication.get(this).getMovieDatabaseReference().child(String.valueOf(movieId))
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMovie = snapshot.getValue(Movie.class);

                displayDataMovie();
                initListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayDataMovie() {
        if (mMovie == null) {
            return;
        }
        GlideUtils.loadUrl(mMovie.getImage(), mActivityMovieDetailBinding.imgMovie);
        mActivityMovieDetailBinding.tvTitleMovie.setText(mMovie.getName());
        mActivityMovieDetailBinding.tvCategoryName.setText(mMovie.getCategoryName());
        mActivityMovieDetailBinding.tvDateMovie.setText(mMovie.getDate());
        String strPrice = mMovie.getPrice() + ConstantKey.UNIT_CURRENCY_MOVIE;
        mActivityMovieDetailBinding.tvPriceMovie.setText(strPrice);
        mActivityMovieDetailBinding.tvDescriptionMovie.setText(mMovie.getDescription());

        if (!StringUtil.isEmpty(mMovie.getUrl())) {
            Log.e("Movie Url", mMovie.getUrl());
            initExoPlayer();
        }
    }

    private void initListener() {
        mActivityMovieDetailBinding.imgBack.setOnClickListener(view -> onBackPressed());
        mActivityMovieDetailBinding.btnWatchTrailer.setOnClickListener(view -> scrollToLayoutTrailer());
        mActivityMovieDetailBinding.imgPlayMovie.setOnClickListener(view -> startVideo());
        mActivityMovieDetailBinding.btnBooking.setOnClickListener(view -> onClickGoToConfirmBooking());
    }

    private void onClickGoToConfirmBooking() {
        if (mMovie == null) {
            return;
        }
        if (DateTimeUtils.convertDateToTimeStamp(mMovie.getDate()) < DateTimeUtils.getLongCurrentTimeStamp()) {
            Toast.makeText(this, getString(R.string.msg_movie_date_invalid), Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable(ConstantKey.KEY_INTENT_MOVIE_OBJECT, mMovie);
        GlobalFunction.startActivity(this, ConfirmBookingActivity.class, bundle);
    }

    private void scrollToLayoutTrailer() {
        long dulation = 500;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            float y = mActivityMovieDetailBinding.labelMovieTrailer.getY();
            ScrollView sv = mActivityMovieDetailBinding.scrollView;
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(sv, "scrollY", 0, (int) y);
            objectAnimator.start();

            startVideo();
        }, dulation);
    }

    private void initExoPlayer() {
        PlayerView mExoPlayerView = mActivityMovieDetailBinding.exoplayer;

        if (mPlayer != null) {
            return;
        }
        String userAgent = Util.getUserAgent(this, this.getApplicationInfo().packageName);
        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent,
                null, DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, true);
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
                null, httpDataSourceFactory);
        mMediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(mMovie.getUrl()));

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();

        mPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this),
                trackSelector, loadControl);
        mPlayer.addListener(new Player.EventListener() {

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
                // Do nothing
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                // Do nothing
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                // Do nothing
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                // Do nothing
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                // Do nothing
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                // Do nothing
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                // Do nothing
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                // Do nothing
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                // Do nothing
            }

            @Override
            public void onSeekProcessed() {
                // Do nothing
            }
        });
        // Set player
        mExoPlayerView.setPlayer(mPlayer);
        mExoPlayerView.hideController();
    }

    private void startVideo() {
        mActivityMovieDetailBinding.imgPlayMovie.setVisibility(View.GONE);
        if (mPlayer != null) {
            // Prepare video source
            mPlayer.prepare(mMediaSource);
            // Set play video
            mPlayer.setPlayWhenReady(true);
        }
    }
}