package eu.kazisrahi.popularmovies;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import eu.kazisrahi.popularmovies.Adapters.MoviesAdapter;
import eu.kazisrahi.popularmovies.DataModels.MoviesCollection;
import eu.kazisrahi.popularmovies.DataModels.MoviesDBContract;
import eu.kazisrahi.popularmovies.Utils.EndlessRecyclerViewScrollListener;
import eu.kazisrahi.popularmovies.Utils.NetworkUtils;
import eu.kazisrahi.popularmovies.Utils.PopularMoviesJSonUtils;
import eu.kazisrahi.popularmovies.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements
        MoviesAdapter.MovieItemClickListener,
        LoaderManager.LoaderCallbacks<MoviesCollection>,
        SwipeRefreshLayout.OnRefreshListener {

    //private ActivityMainBinding binding;
    private static final int MOVIES_FEED_LOADER = 0;
    private static MoviesCollection cachedCollection = null;
    private MoviesAdapter moviesAdapter;
    private EndlessRecyclerViewScrollListener endlessScrollListener;

    private ActivityMainBinding binding;

    private static final int FETCH_POPULAR = 1;
    private static final int FETCH_TOP_RATED = 2;
    private static final int FETCH_FAVORITES = 3;

    private static int fetchState = FETCH_POPULAR;

    private String FETCH_QUERY_STATE = "FETCH_QUERY_STATE";
    private int currentPage = 1;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // if no API KEY is defined in the strings.xml, notify about it and close the app
        if (getString(R.string.network_api_key).trim().equals("")) {
            makeBadApiKeyDialog(getString(R.string.main_no_api_key));
        }

        GridLayoutManager mLayoutManager = new GridLayoutManager(this, getOrientationGridSpans());
        binding.mainRvMoviesList.setLayoutManager(mLayoutManager);

        binding.mainRvMoviesList.setHasFixedSize(true);
        moviesAdapter = new MoviesAdapter(this);
        binding.mainRvMoviesList.setAdapter(moviesAdapter);

        // allow swipe to refresh
        binding.mainLayoutSwipe.setOnRefreshListener(this);

        currentPage = 1;

        if (savedInstanceState != null) {
            fetchState = savedInstanceState.getInt(FETCH_QUERY_STATE);

            if (fetchState == FETCH_POPULAR) {
                setTitle(getString(R.string.main_actionbar_most_popular));
            } else if (fetchState == FETCH_TOP_RATED){
                setTitle(getString(R.string.main_actionbar_most_voted));
            } else{
                setTitle(getString(R.string.main_actionbar_favourites));
            }
        }

        // make initial page load
        if (fetchState==FETCH_FAVORITES) {
            getLoaderManager().restartLoader(MOVIES_FEED_LOADER, null, this);
        }
        else {
            makeMoviesLoaderQuery(fetchState, currentPage);
        }

        // allow enless scroll of the Recycler View
        // Source: https://github.com/codepath/android_guides/wiki/Endless-Scrolling-with-AdapterViews-and-RecyclerView
        endlessScrollListener = new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                int nextPage = page + 1;
                Log.d("MAIN", "TIME TO LOAD PAGE " + nextPage);
                makeMoviesLoaderQuery(fetchState, nextPage);
                currentPage = nextPage;
            }
        };
        binding.mainRvMoviesList.addOnScrollListener(endlessScrollListener);
    }

    /**
     * On landscape mode we want 3 rows of results on our grid, on portrait we want 2.
     *
     * @return the amount of grid rows (spans) based on device orientation.
     */
    private int getOrientationGridSpans() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            return 3;
        else
            return 2;
    }

    /**
     * Save the RecyclerView state to recover it on rotation
     *
     * @param state
     */
    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(FETCH_QUERY_STATE, fetchState);



        Log.d("MAIN", "SAVING STATE");
    }

    /**
     * Provide the latest version of the collection incase we press the "back" button
     * This is because due to the endless scroll we might not get the full version of the cached collection
     * when pressing back.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cachedCollection = moviesAdapter.getCollection();
    }

    /**
     * Manage the menu items to switch between Most Popular, Top Rated, and Favourites
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        this.menu = menu;

        // persistence.  Set checked state based on the fetchPopular boolean
        if (fetchState==FETCH_POPULAR) {
            menu.findItem(R.id.action_popular).setChecked(true);
            menu.findItem(R.id.action_voted).setChecked(false);
            menu.findItem(R.id.action_favorites).setChecked(false);
        } else if (fetchState==FETCH_TOP_RATED) {
            menu.findItem(R.id.action_popular).setChecked(false);
            menu.findItem(R.id.action_voted).setChecked(true);
            menu.findItem(R.id.action_favorites).setChecked(false);
        } else {
            menu.findItem(R.id.action_popular).setChecked(false);
            menu.findItem(R.id.action_favorites).setChecked(false);
            menu.findItem(R.id.action_favorites).setChecked(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        cachedCollection = null;
        currentPage = 1;
        endlessScrollListener.resetState();
        moviesAdapter.clearCollection();

        if (id == R.id.action_popular && fetchState!=FETCH_POPULAR) {
            fetchState = FETCH_POPULAR;
            item.setChecked(true);
            menu.findItem(R.id.action_voted).setChecked(false);
            menu.findItem(R.id.action_favorites).setChecked(false);
            makeMoviesLoaderQuery(fetchState, currentPage);
            return true;
        } else if (id == R.id.action_voted && fetchState!=FETCH_TOP_RATED) {
            fetchState = FETCH_TOP_RATED;
            item.setChecked(true);
            menu.findItem(R.id.action_popular).setChecked(false);
            menu.findItem(R.id.action_favorites).setChecked(false);
            makeMoviesLoaderQuery(fetchState, currentPage);
            return true;
        } else if (id == R.id.action_favorites && fetchState!=FETCH_FAVORITES) {
            fetchState = FETCH_FAVORITES;
            item.setChecked(true);
            menu.findItem(R.id.action_popular).setChecked(false);
            menu.findItem(R.id.action_voted).setChecked(false);
            setTitle(getString(R.string.main_actionbar_favourites));
            getLoaderManager().restartLoader(MOVIES_FEED_LOADER, null, this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        currentPage = 1;
        endlessScrollListener.resetState();
        cachedCollection = null;

        Log.d("MAIN", "REFRESHING...");
        // clear cachedCollection to make sure no cached results are produced and set current page back to 1
        if (fetchState == FETCH_FAVORITES){
            menu.findItem(R.id.action_popular).setChecked(false);
            menu.findItem(R.id.action_voted).setChecked(false);
            menu.findItem(R.id.action_favorites).setChecked(true);
            moviesAdapter.clearCollection();

            setTitle(getString(R.string.main_actionbar_favourites));
            getLoaderManager().restartLoader(MOVIES_FEED_LOADER, null, this);
        }
        else {
            makeMoviesLoaderQuery(fetchState, currentPage);
        }
    }

    @Override
    public void onMovieItemClick(int clickedItemIndex, ImageView sharedImage) {
        // clear the loaders caching on the Detail Activity to receive fresh content per movie.
        DetailActivity.cachedTrailersCollection = null;
        DetailActivity.cachedReviewsCollection = null;

        // create an intent to call the detail activity.
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("movie", moviesAdapter.getMovieAtIndex(clickedItemIndex));

        // together with the info of the movie, pass the low res image of the recycler view
        // so there are ZERO delays in the "Share Element Transitions" and the DetailActivity loads instantly
        // then, later inside the DetailActivity, we will replace the low res image with high res
        Bitmap bm=((BitmapDrawable)sharedImage.getDrawable()).getBitmap();
        intent.putExtra("low_res_bitmap", bm);

        // bundle for the transition effect
        Bundle bundle = ActivityOptionsCompat
                .makeSceneTransitionAnimation(
                        this,
                        sharedImage,
                        sharedImage.getTransitionName()
                ).toBundle();

        startActivity(intent, bundle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // when the detail activity is finished, if we are on the favorites view
        // we need to refresh it in case the detail activity was added/removed from favorites.
        if (fetchState == FETCH_FAVORITES) {
            cachedCollection = null;
            moviesAdapter.clearCollection();
            getLoaderManager().restartLoader(MOVIES_FEED_LOADER, null, this);
        }
    }

    /**
     * This method makes all preparations to call the AsyncTask that queries the movies
     *
     * @param fetchState
     */
    private void makeMoviesLoaderQuery(final int fetchState, final int page) {
        if (fetchState==FETCH_FAVORITES)
            return;

        boolean fetchPopular = (fetchState==FETCH_POPULAR);

        if (currentPage < page)
            cachedCollection = null;
        // if no network connection show error message and exit
        if (!NetworkUtils.isNetworkAvailable(this)) {
            if (page > 1)
                return;
            ;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.main_no_network)
                    .setNegativeButton(R.string.main_no_network_try_again, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            makeMoviesLoaderQuery(fetchState, page);
                        }
                    })
                    .setPositiveButton(R.string.main_no_network_close, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            // Create the AlertDialog object and return it
            builder.create().show();
        } else {
            String movieSearchUrlStr;
            if (fetchPopular) {
                setTitle(getString(R.string.main_actionbar_most_popular));
                movieSearchUrlStr = getString(R.string.network_base_url) + getString(R.string.network_folder_popular);
            } else {
                setTitle(getString(R.string.main_actionbar_most_voted));
                movieSearchUrlStr = getString(R.string.network_base_url) + getString(R.string.network_folder_top_rated);
            }

            Bundle bundle = new Bundle();
            bundle.putString("URL_STR", movieSearchUrlStr);
            bundle.putString("API_PARAM", getString(R.string.network_api_param));
            bundle.putString("API_KEY", getString(R.string.network_api_key).trim());
            bundle.putString("PAGE_PARAM", "page");
            bundle.putString("PAGE", String.valueOf(page));


            Loader<String> loader = getLoaderManager().getLoader(MOVIES_FEED_LOADER);
            if (loader == null) {
                getLoaderManager().initLoader(MOVIES_FEED_LOADER, bundle, this);
            } else {
                getLoaderManager().restartLoader(MOVIES_FEED_LOADER, bundle, this);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<MoviesCollection> onCreateLoader(int i, final Bundle bundle) {
        return new AsyncTaskLoader<MoviesCollection>(this) {
            @Override
            public MoviesCollection loadInBackground() {

                if (fetchState == FETCH_FAVORITES) {
                    Cursor cursor = getContentResolver().query(MoviesDBContract.MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            MoviesDBContract.MovieEntry._ID + " DESC"
                    );
                    if (cursor!=null) {
                        if (cursor.getCount() > 0) {
                            MoviesCollection favoriteMovies = new MoviesCollection();

                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()){
                                Date releaseDate = new Date(cursor.getLong(cursor.getColumnIndex(MoviesDBContract.MovieEntry.COL_RELEASE_DATE)));

                                MoviesCollection.Movie movie = new MoviesCollection.Movie(
                                        0,
                                        cursor.getInt(cursor.getColumnIndex(MoviesDBContract.MovieEntry.COL_ID)),
                                        false,
                                        cursor.getDouble(cursor.getColumnIndex(MoviesDBContract.MovieEntry.COL_VOTE_AVERAGE)),
                                        cursor.getString(cursor.getColumnIndex(MoviesDBContract.MovieEntry.COL_TITLE)),
                                        0,
                                        cursor.getString(cursor.getColumnIndex(MoviesDBContract.MovieEntry.COL_POSTER_PATH)),
                                        "",
                                        "",
                                        null,
                                        cursor.getString(cursor.getColumnIndex(MoviesDBContract.MovieEntry.COL_BACKDROP_PATH)),
                                        false,
                                        cursor.getString(cursor.getColumnIndex(MoviesDBContract.MovieEntry.COL_OVERVIEW)),
                                        releaseDate
                                );
                                favoriteMovies.addMovie(movie);
                                cursor.moveToNext();
                            }
                            // all results of the favorites are contained in a single page (no endless load here)
                            favoriteMovies.setPage(1);
                            return favoriteMovies;
                        }
                    }
                    return null;
                }
                else {
                    String movieSearchResults = null;
                    try {
                        URL movieSearchUrl = NetworkUtils.buildUrl(
                                bundle.getString("URL_STR"),
                                bundle.getString("API_PARAM"),
                                bundle.getString("API_KEY"),
                                bundle.getString("PAGE_PARAM"),
                                bundle.getString("PAGE"));

                        movieSearchResults = NetworkUtils.getResponseFromHttpUrl(movieSearchUrl);
                        return PopularMoviesJSonUtils.parseMoviesJson(movieSearchResults);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }

            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                if (cachedCollection == null) {
                    Log.d("LOADER", "FETCHING NEW DATA");
                    forceLoad();
                } else {
                    Log.d("LOADER", "SHOWING CACHED DATA");
                    deliverResult(cachedCollection);
                }
            }

            @Override
            public void deliverResult(MoviesCollection data) {
                cachedCollection = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<MoviesCollection> loader, MoviesCollection collection) {
        binding.mainLayoutSwipe.setRefreshing(false);

        Log.d("LOADING", "LOAD FINISHED");

        // check the json result, when bad api key is supplied page response code is 401 (anauthorised)
        // in this case we have a null object returned.  If thats the case warn user about this and exit.
        if (collection == null && fetchState != FETCH_FAVORITES) {
            makeBadApiKeyDialog(getString(R.string.main_bad_api_key));
        } else {
            moviesAdapter.appendCollectionData(collection);
        }
    }

    @Override
    public void onLoaderReset(Loader<MoviesCollection> loader) {

    }

    private void makeBadApiKeyDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton(R.string.main_no_api_key_close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        // Create the AlertDialog object and return it
        builder.create().show();
    }
}
