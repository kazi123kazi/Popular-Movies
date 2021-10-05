package eu.kazisrahi.popularmovies;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.kazisrahi.popularmovies.Adapters.MovieReviewsAdapter;
import eu.kazisrahi.popularmovies.Adapters.MovieTrailersAdapter;
import eu.kazisrahi.popularmovies.DataModels.MoviesCollection;
import eu.kazisrahi.popularmovies.DataModels.MoviesDBContract;
import eu.kazisrahi.popularmovies.DataModels.ReviewsCollection;
import eu.kazisrahi.popularmovies.DataModels.TrailersCollection;
import eu.kazisrahi.popularmovies.Utils.AnimatedTabHostListener;
import eu.kazisrahi.popularmovies.Utils.NetworkUtils;
import eu.kazisrahi.popularmovies.Utils.PopularMoviesJSonUtils;
import eu.kazisrahi.popularmovies.databinding.ActivityDetailBinding;

public class DetailActivity extends AppCompatActivity implements
        MovieTrailersAdapter.TrailerItemClickListener {

    // full info for single movie
    // https://api.themoviedb.org/3/movie/269149?api_key=bcb3bde29d27f221a554d9f491bfa0c3

    private static final int DATA_RESULT_MOVIE_TRAILERS_LOADER = 10;
    private static final int DATA_RESULT_MOVIE_REVIEWS_LOADER = 20;

    public static TrailersCollection cachedTrailersCollection = null;
    public static ReviewsCollection cachedReviewsCollection = null;

    private Bitmap lowResBitmap = null;

    private MoviesCollection.Movie movie = null;
    private ActivityDetailBinding binding;
    private MovieTrailersAdapter trailersAdapter;
    private MovieReviewsAdapter reviewsAdapter;
    /**
     * Trailers Loader Callbacks
     * <p>
     * As we have multiple loaders in a single activity (haven't mastered fragments yet) we won't "implement"
     * the callbacks in our class definition, instead we will contain it here
     */
    private LoaderManager.LoaderCallbacks<TrailersCollection> trailersCollectionListener = new LoaderManager.LoaderCallbacks<TrailersCollection>() {
        @SuppressLint("StaticFieldLeak")
        @Override
        public Loader<TrailersCollection> onCreateLoader(int i, Bundle bundle) {
            String movieId = bundle.getString("MOVIE_ID");

            final URL urlTrailers = NetworkUtils.buildUrl(
                    getString(R.string.network_base_url) + "/" + movieId + getString(R.string.network_folder_videos),
                    bundle.getString("API_PARAM"),
                    bundle.getString("API_KEY"));

            return new AsyncTaskLoader<TrailersCollection>(getApplicationContext()) {
                @Override
                public TrailersCollection loadInBackground() {
                    String trailersSearchResults;

                    try {
                        trailersSearchResults = NetworkUtils.getResponseFromHttpUrl(urlTrailers);
                        return PopularMoviesJSonUtils.parseTrailersJson(trailersSearchResults);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();

                    if (cachedTrailersCollection == null) {
                        forceLoad();
                    } else {
                        deliverResult(cachedTrailersCollection);
                    }
                }

                @Override
                public void deliverResult(TrailersCollection data) {
                    cachedTrailersCollection = data;
                    super.deliverResult(data);
                }

            };
        }

        @Override
        public void onLoadFinished(Loader<TrailersCollection> loader, TrailersCollection trailersCollection) {
            if (trailersCollection != null) {
                trailersAdapter.setTrailersData(trailersCollection);

                // if there are no trailers for that movie, display a message on its tab informing the user about it
                // Hint: On the "most voted", the "Godfather" movie has no trailers. (You can check the condition with that movie)
                if (trailersCollection.getCollectionSize()==0){
                    binding.includedTab2.tvTrailersNoData.setVisibility(View.VISIBLE);
                }
                else{
                    binding.includedTab2.tvTrailersNoData.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<TrailersCollection> loader) {

        }
    };
    /**
     * Reviews Loader Callbacks
     * <p>
     * As we have multiple loaders in a single activity (haven't mastered fragments yet) we won't "implement"
     * the callbacks in our class definition, instead we will contain it here
     */
    private LoaderManager.LoaderCallbacks<ReviewsCollection> reviewsCollectionListener = new LoaderManager.LoaderCallbacks<ReviewsCollection>() {
        @SuppressLint("StaticFieldLeak")
        @Override
        public Loader<ReviewsCollection> onCreateLoader(int i, final Bundle bundle) {
            String movieId = bundle.getString("MOVIE_ID");


            final URL urlReviews = NetworkUtils.buildUrl(
                    getString(R.string.network_base_url) + "/" + movieId + getString(R.string.network_folder_reviews),
                    bundle.getString("API_PARAM"),
                    bundle.getString("API_KEY"));

            return new AsyncTaskLoader<ReviewsCollection>(getApplicationContext()) {
                @Override
                public ReviewsCollection loadInBackground() {
                    String reviewsSearchResults;

                    try {
                        reviewsSearchResults = NetworkUtils.getResponseFromHttpUrl(urlReviews);
                        return PopularMoviesJSonUtils.parseReviewsJson(reviewsSearchResults);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();

                    if (cachedReviewsCollection == null) {
                        forceLoad();
                    } else {
                        deliverResult(cachedReviewsCollection);
                    }
                }

                @Override
                public void deliverResult(ReviewsCollection data) {
                    cachedReviewsCollection = data;
                    super.deliverResult(data);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<ReviewsCollection> loader, ReviewsCollection reviewsCollection) {
            reviewsAdapter.setReviewsData(reviewsCollection);

            // if there are no reviews for that movie, display a message on its tab informing the user about it
            // Hint: On the "most voted", the "Forest Gump" movie has no reviews. (You can check the condition with that movie)
            if (reviewsCollection.getCollectionSize()==0){
                binding.includedTab3.tvReviewsNoData.setVisibility(View.VISIBLE);
            }
            else{
                binding.includedTab3.tvReviewsNoData.setVisibility(View.GONE);
            }
        }

        @Override
        public void onLoaderReset(Loader<ReviewsCollection> loader) {

        }
    };

    /**
     * Try to play the youtube video first on the YouTube app, otherwise on a browser
     * Source: https://stackoverflow.com/questions/574195/android-youtube-app-play-video-intent
     *
     * @param context the context that initiates the intent
     * @param id      the youtube video id
     */
    public static void watchYoutubeVideo(Context context, String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(context.getString(R.string.youtube_video_url) + id));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
        }
    }

    /**
     * Call "insert" on the Content Provider, to insert the existing movie to the database
     */
    private void addMovieToFavorites(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MoviesDBContract.MovieEntry.COL_ID, movie.getId());
        contentValues.put(MoviesDBContract.MovieEntry.COL_TITLE, movie.getTitle());
        contentValues.put(MoviesDBContract.MovieEntry.COL_POSTER_PATH, movie.getPosterPath());
        contentValues.put(MoviesDBContract.MovieEntry.COL_BACKDROP_PATH, movie.getBackdropPath());

        Date releaseDate = movie.getReleaseDate();
        if (releaseDate!=null) {
            contentValues.put(MoviesDBContract.MovieEntry.COL_RELEASE_DATE, movie.getReleaseDate().getTime());
        }
        else{
            contentValues.put(MoviesDBContract.MovieEntry.COL_RELEASE_DATE, 0);
        }

        contentValues.put(MoviesDBContract.MovieEntry.COL_VOTE_AVERAGE, movie.getVoteAverage());
        contentValues.put(MoviesDBContract.MovieEntry.COL_OVERVIEW, movie.getOverview());

        Uri uri = getContentResolver().insert(MoviesDBContract.MovieEntry.CONTENT_URI, contentValues);
        if(uri != null) {
            binding.fab.setImageResource(R.drawable.ic_star_white_48dp);
        }

        Snackbar.make(binding.detailContentLayout, R.string.favourites_add, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Call "delete" on the Content Provider, to remove the existing movie from the database
     */
    private void removeMovieFromFavorites(){
        Uri uri = MoviesDBContract.MovieEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(String.valueOf(movie.getId())).build();
        getContentResolver().delete(uri, null, null);

        binding.fab.setImageResource(R.drawable.ic_star_border_white_48dp);

        Snackbar.make(binding.detailContentLayout, R.string.favourites_removed, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Call "query" on the Content Provider to find out whether or not the movie exists in the database
     * @return whether the existing movie exists in the favorites database
     */
    private boolean isMovieInFavorites(){

        Cursor cursor = getContentResolver().query(MoviesDBContract.MovieEntry.CONTENT_URI,
                null,
                MoviesDBContract.MovieEntry.COL_ID + " = " + movie.getId(),
                null,
                null
        );


        return ((cursor != null ? cursor.getCount() : 0) >0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // set the toolbar in our xml as the ActionBar for this activity and then define a default back button
        // That way we implemented native back button (not ImageButton) functionality as a "transparent" ActionBar
        // Source: https://stackoverflow.com/questions/26651602/display-back-arrow-on-toolbar
        setSupportActionBar(binding.detailTvTitleToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Gesture detection
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };

        // Specify what views will make use of the Gesture Detector
        binding.tab1.setOnTouchListener(gestureListener);

        binding.tab2.setOnTouchListener(gestureListener);
        binding.includedTab2.rvTrailersList.setOnTouchListener(gestureListener);

        binding.tab3.setOnTouchListener(gestureListener);
        binding.includedTab3.rvReviewsList.setOnTouchListener(gestureListener);



        supportPostponeEnterTransition();

        // Receive the Parcelable Movie object from the extras of the intent.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("movie")) {
                movie = getIntent().getParcelableExtra("movie");
            }
            // place directly the low res image from the main activity so there are no delays in the Transition
            // then later we will load the higher res image
            if (extras.containsKey("low_res_bitmap")){
                lowResBitmap = getIntent().getParcelableExtra("low_res_bitmap");
            }
        }

        binding.tabHost.setup();

        // change the "star" button appearance depending on if the movie is in favorites or not.
        if (isMovieInFavorites()){
            binding.fab.setImageResource(R.drawable.ic_star_white_48dp);
        }
        else{
            binding.fab.setImageResource(R.drawable.ic_star_border_white_48dp);
            binding.detailIvImgHoriz.setImageBitmap(lowResBitmap);
            supportStartPostponedEnterTransition();
        }

        binding.fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isMovieInFavorites())
                    removeMovieFromFavorites();
                else
                    addMovieToFavorites();
            }
        });

        //=================== MOVIE INFORMATION TAB =====================
        addTab("Movie Info", R.id.tab1);

        // Set ActionBar title to that of the movie title.
        setTitle(movie.getTitle());
        binding.detailTvTitleToolbar.setTitle(movie.getTitle());
        binding.includedTab1.detailTvTitle2.setText(movie.getTitle());

        // setup the rating
        binding.includedTab1.detailRbMovieRating.setRating(movie.getRating(1) / 2);
        binding.includedTab1.detailTvMovieRating.setText(getString(R.string.detail_rating_text, movie.getRating(1)));

        // Get the date
        binding.includedTab1.detailTvReleaseDate.setText(getString(R.string.detail_released, getLocalizedDateStr(movie.getReleaseDate())));

        // Get the synopsis (overview)
        binding.includedTab1.detailTvSynopsis.setText(movie.getOverview());

        supportPostponeEnterTransition();

        // then load the large image
        setImage(false, movie.getBackdropPath(), true);
        setImage(true, movie.getPosterPath(), false);

        //=================== TRAILERS =====================
        addTab("Trailers", R.id.tab2);
        GridLayoutManager trailersLayoutManager = new GridLayoutManager(this, getOrientationGridSpans());
        binding.includedTab2.rvTrailersList.setLayoutManager(trailersLayoutManager);
        binding.includedTab2.rvTrailersList.setHasFixedSize(true);

        // set the trailers adapter
        trailersAdapter = new MovieTrailersAdapter(this);
        binding.includedTab2.rvTrailersList.setAdapter(trailersAdapter);

        Bundle trailersBundle = new Bundle();
        trailersBundle.putString("MOVIE_ID", String.valueOf(movie.getId()));
        trailersBundle.putString("API_PARAM", getString(R.string.network_api_param));
        trailersBundle.putString("API_KEY", getString(R.string.network_api_key).trim());

        Loader<String> trailersLoader = getLoaderManager().getLoader(DATA_RESULT_MOVIE_TRAILERS_LOADER);
        if (trailersLoader == null) {
            getLoaderManager().initLoader(DATA_RESULT_MOVIE_TRAILERS_LOADER, trailersBundle, trailersCollectionListener);
        } else {
            getLoaderManager().restartLoader(DATA_RESULT_MOVIE_TRAILERS_LOADER, trailersBundle, trailersCollectionListener);
        }


        //=================== REVIEWS =====================
        addTab("Reviews", R.id.tab3);

        LinearLayoutManager reviewsLayoutManager = new LinearLayoutManager(this);
        binding.includedTab3.rvReviewsList.setLayoutManager(reviewsLayoutManager);

        // set the reviews adapter
        reviewsAdapter = new MovieReviewsAdapter();
        binding.includedTab3.rvReviewsList.setAdapter(reviewsAdapter);

        Bundle reviewsBundle = new Bundle();
        reviewsBundle.putString("MOVIE_ID", String.valueOf(movie.getId()));
        reviewsBundle.putString("API_PARAM", getString(R.string.network_api_param));
        reviewsBundle.putString("API_KEY", getString(R.string.network_api_key).trim());

        Loader<String> reviewsLoader = getLoaderManager().getLoader(DATA_RESULT_MOVIE_REVIEWS_LOADER);
        if (reviewsLoader == null) {
            getLoaderManager().initLoader(DATA_RESULT_MOVIE_REVIEWS_LOADER, reviewsBundle, reviewsCollectionListener);
        } else {
            getLoaderManager().restartLoader(DATA_RESULT_MOVIE_REVIEWS_LOADER, reviewsBundle, reviewsCollectionListener);
        }


        // save the currently selected tab, so we can restore it in case of a phone rotation
        if (savedInstanceState != null) {
            binding.tabHost.setCurrentTab(savedInstanceState.getInt("CURRENT_TAB"));
        }

        // enable tabhost animation when changing tabs - Thus will enchant the GESTURES with a nice sliding animation
        binding.tabHost.setOnTabChangedListener(new AnimatedTabHostListener(this, binding.tabHost));
    }

    private void addTab(String tabName, int contentId) {
        TabHost.TabSpec spec = binding.tabHost.newTabSpec(tabName);
        spec.setContent(contentId);
        spec.setIndicator(tabName);
        binding.tabHost.addTab(spec);
    }

    /**
     * On landscape mode we want 2 rows of results on our grid, on portrait we want 1.
     *
     * @return the amount of grid rows (spans) based on device orientation.
     */
    private int getOrientationGridSpans() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            return 2;
        else
            return 1;
    }


    // =============== MOVIE TRAILERS LOADER =================================

    /**
     * Gets a date, and returns a localized date format string with 4 digits on the year.
     *
     * @param theDate
     * @return The String with the localized date with 4 year digits
     */
    private String getLocalizedDateStr(Date theDate) {
        if (theDate!=null) {
            // get local date format
            DateFormat df = android.text.format.DateFormat.getDateFormat(this);
            String pattern = ((SimpleDateFormat) df).toPattern();

            // local date year format is always using 2 digits - we transform year to 4 digits
            pattern = pattern.replaceAll("yy", "yyyy");
            ((SimpleDateFormat) df).applyLocalizedPattern(pattern);

            return df.format(theDate);
        }
        else{
            return getString(R.string.detail_release_unavailable);
        }
    }

    /**
     * Takes the image id and applies it to the tmdb.org URL to draw it using
     * the Picasso library on the ImageView
     *
     * @param setPoster if true the image refers to poster, backdropPath otherwise
     * @param image The image id
     */
    void setImage(boolean setPoster, String image, boolean withTransition) {
        // sample
        // Source: https://image.tmdb.org/t/p/w780/lkOZcsXcOLZYeJ2YxJd3vSldvU4.jpg

        ImageView targetView = null;
        String targetWidth = null;

        if (setPoster) {
            // set target view and dimensions for the poster to be downloaded
            targetView = binding.includedTab1.detailIvPoster;
            targetWidth = getString(R.string.network_width_185);

            if (image.equals("")){
                // if no poster exist, display "no image" placeholder
                targetView.setImageResource(R.drawable.poster_noimage);
                return;
            }
        }
        else{
            // set target view and dimensions for the backdrop to be downloaded
            targetView = binding.detailIvImgHoriz;
            targetWidth = getString(R.string.network_width_780);

            if (image.equals("")){
                // if no backdrop exist, simply don't display anything to reserve unecessary space
                return;
            }
        }

        // load the image from the web
        String imagePath = getString(R.string.network_url_images) +targetWidth;
        if (withTransition){
            targetView.setTransitionName(getString(R.string.transition_photo));

            Picasso.with(this)
                    .load(imagePath + image)
                    .noFade()
                    .placeholder(targetView.getDrawable())
                    .into(targetView, new Callback() {
                        @Override
                        public void onSuccess() {
                            supportStartPostponedEnterTransition();
                        }

                        @Override
                        public void onError() {
                            supportStartPostponedEnterTransition();
                        }
                    });
        }
        else {
            Picasso.with(this)
                    .load(imagePath + image)
                    .placeholder(targetView.getDrawable())
                    .into(targetView);
        }
    }

    /**
     * IF WE DONT MAKE USE OF THE ACTUAL ANDROID TOOLBAR
     * Then we have a drawable arrow icon on the upper left of the poster to act as back button
     * @param view
     */
    public void actionUpClicked(View view) {
        super.onBackPressed();
    }

    /**
     * IF WE MAKE USE OF THE ACTUAL ANDROID TOOLBAR...
     * When the arrow is pressed on the action bar, close the activity
     * by invoking the original "back button pressing". This is to reverse transition
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                super.onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("CURRENT_TAB", binding.tabHost.getCurrentTab());
    }

    @Override
    public void onTrailerItemClick(int clickedItemIndex) {
        watchYoutubeVideo(this, trailersAdapter.getMovieTrailerAtIndex(clickedItemIndex).getKey());
    }

    /**
     *  GESTURE DETECTION
     */

    // Source: https://stackoverflow.com/questions/937313/fling-gesture-detection-on-grid-layout
    // Swipe - Slide tabs
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(DetailActivity.this, "Left Swipe", Toast.LENGTH_SHORT).show();
                    binding.tabHost.setCurrentTab(binding.tabHost.getCurrentTab()+1);
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(DetailActivity.this, "Right Swipe", Toast.LENGTH_SHORT).show();
                    binding.tabHost.setCurrentTab(binding.tabHost.getCurrentTab()-1);
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }
    }

}