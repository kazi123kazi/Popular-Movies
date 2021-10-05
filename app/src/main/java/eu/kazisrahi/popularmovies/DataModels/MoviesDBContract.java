package eu.kazisrahi.popularmovies.DataModels;

import android.net.Uri;
import android.provider.BaseColumns;

public class MoviesDBContract {
    /**
     * No instances of the contract class are allowed
     */

    public static final String AUTHORITY = "eu.anifantakis.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_MOVIES = "movies";

    private MoviesDBContract() {}

    public static class MovieEntry implements BaseColumns{
        // Content URI for the Content Provider of a single movie
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        // Fields regarding the table "movies" and its columns
        public static final String TABLE_NAME = "movies";
        public static final String COL_ID = "id";
        public static final String COL_TITLE = "title";
        public static final String COL_POSTER_PATH = "poster_path";
        public static final String COL_BACKDROP_PATH = "backdrop_path";
        public static final String COL_RELEASE_DATE = "release_date";
        public static final String COL_VOTE_AVERAGE = "vote_average";
        public static final String COL_OVERVIEW = "overview";
    }
}
