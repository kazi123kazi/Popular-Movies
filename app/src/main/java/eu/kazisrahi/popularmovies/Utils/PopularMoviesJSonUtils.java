package eu.kazisrahi.popularmovies.Utils;

import android.annotation.SuppressLint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.kazisrahi.popularmovies.DataModels.MoviesCollection;
import eu.kazisrahi.popularmovies.DataModels.ReviewsCollection;
import eu.kazisrahi.popularmovies.DataModels.TrailersCollection;


/**
 * Created by ioannisa on 25/2/2018.
 */

public final class PopularMoviesJSonUtils {

    public static TrailersCollection parseTrailersJson(String json) {
        TrailersCollection trailers = null;

        try {
            // retrieve the root
            JSONObject moviesJson = new JSONObject(json);
            trailers = new TrailersCollection();

            JSONArray moviesJsonJSONArray = moviesJson.getJSONArray("results");
            for (int i = 0; i < moviesJsonJSONArray.length(); i++) {
                JSONObject trailerJson = (JSONObject) moviesJsonJSONArray.get(i);
                if (trailerJson.optString("type").equals("Trailer") && trailerJson.optString("site").equals("YouTube")) {
                    trailers.addMovieTrailer(
                            new TrailersCollection.MovieTrailer(
                                    trailerJson.optString("id"),
                                    trailerJson.optString("iso_3166_1"),
                                    trailerJson.optString("iso_639_1"),
                                    trailerJson.optString("key"),
                                    trailerJson.optString("name"),
                                    trailerJson.optString("site"),
                                    trailerJson.optInt("size"),
                                    trailerJson.optString("type")
                            )
                    );
                }
            }

            return trailers;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ReviewsCollection parseReviewsJson(String json) {
        ReviewsCollection reviews = null;

        try {
            // retrieve the root
            JSONObject moviesJson = new JSONObject(json);
            reviews = new ReviewsCollection();

            JSONArray moviesJsonJSONArray = moviesJson.getJSONArray("results");
            for (int i = 0; i < moviesJsonJSONArray.length(); i++) {
                JSONObject trailerJson = (JSONObject) moviesJsonJSONArray.get(i);

                // String id, String author, String content, String url

                reviews.addMovieReview(
                        new ReviewsCollection.MovieReview(
                                trailerJson.optString("id"),
                                trailerJson.optString("author"),
                                trailerJson.optString("content"),
                                trailerJson.optString("url")
                        )
                );
            }

            return reviews;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static MoviesCollection parseMoviesJson(String json) {
        MoviesCollection collection = null;
        try {
            // retrieve the root
            JSONObject moviesJson = new JSONObject(json);
            collection = new MoviesCollection();

            collection.setPage(moviesJson.optInt("page", 0));
            collection.setTotalPages(moviesJson.optInt("total_pages", 0));
            collection.setTotalResults(moviesJson.optInt("total_results", 0));

            JSONArray moviesJsonJSONArray = moviesJson.getJSONArray("results");
            for (int i = 0; i < moviesJsonJSONArray.length(); i++) {
                JSONObject movie = (JSONObject) moviesJsonJSONArray.get(i);

                Date releaseDate = null;
                if (! movie.optString("release_date").trim().equals("")){
                    releaseDate = new SimpleDateFormat("yyyy-mm-dd").parse(movie.optString("release_date"));
                }

                String backdropPath = movie.optString("backdrop_path");
                if (backdropPath.trim().equals("null")){
                    backdropPath = "";
                }

                collection.addMovie(
                        new MoviesCollection.Movie(
                                movie.optInt("vote_count"),
                                movie.optInt("id"),
                                movie.optBoolean("video"),
                                movie.optDouble("vote_average"),
                                movie.optString("title"),
                                movie.optDouble("popularity"),
                                movie.optString("poster_path"),
                                movie.optString("original_language"),
                                movie.optString("original_title"),
                                null,
                                backdropPath,
                                movie.optBoolean("adult"),
                                movie.optString("overview"),
                                releaseDate
                        )
                );
            }

            return collection;
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
            // if some error occurred, return null
            return null;
        }
    }
}
