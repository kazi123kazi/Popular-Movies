package eu.kazisrahi.popularmovies.DataModels;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import eu.kazisrahi.popularmovies.DataModels.MoviesDBContract.MovieEntry;

public class MoviesDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "movies.db";

    private static final int DATABASE_VERSION = 3;

    public MoviesDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIE_TABLE =
                "CREATE TABLE "+MovieEntry.TABLE_NAME + "(" +
                        MovieEntry._ID +                " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        MovieEntry.COL_ID +             " INTEGER NOT NULL, "+
                        MovieEntry.COL_TITLE +          " TEXT NOT NULL, "+
                        MovieEntry.COL_POSTER_PATH +    " TEXT NOT NULL, "+
                        MovieEntry.COL_BACKDROP_PATH +  " TEXT NOT NULL, "+
                        MovieEntry.COL_RELEASE_DATE +   " TIMESTAMP NOT NULL, "+
                        MovieEntry.COL_VOTE_AVERAGE +   " FLOAT NOT NULL, "+
                        MovieEntry.COL_OVERVIEW +       " TEXT NOT NULL "+
                ")";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
