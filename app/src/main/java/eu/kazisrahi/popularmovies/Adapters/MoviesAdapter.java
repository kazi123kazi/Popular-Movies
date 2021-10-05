package eu.kazisrahi.popularmovies.Adapters;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.kazisrahi.popularmovies.DataModels.MoviesCollection;
import eu.kazisrahi.popularmovies.R;
import eu.kazisrahi.popularmovies.databinding.MovieRowBinding;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder> {
    final private MovieItemClickListener mOnClickListener;
    private MoviesCollection collection;
    private static Picasso picassoCached = null;

    Activity mActivity;
    public MoviesAdapter(MovieItemClickListener clickListener) {
        this.mOnClickListener = clickListener;
        mActivity = (Activity) clickListener;

        clearOldFileCache(2);
        if (picassoCached == null) {
            picassoCached = getPicasso();
        }
    }

    public MoviesAdapter(MovieItemClickListener clickListener, Activity activity) {
        this.mOnClickListener = clickListener;
        mActivity = activity;

        clearOldFileCache(2);
        if (picassoCached == null) {
            picassoCached = getPicasso();
        }
    }

    public MoviesCollection.Movie getMovieAtIndex(int index) {
        return collection.getMovie(index);
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        MovieRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.movie_row, parent, false);
        return new MovieViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        holder.setTitle(collection.getMovie(position).getTitle());
        holder.setImage(collection.getMovie(position).getBackdropPath());
        holder.setRating(collection.getMovie(position).getRating(1) / 2);
        holder.setYear(collection.getMovie(position).getReleaseDate());
    }

    /**
     *
     * @return
     */
    public Picasso getPicasso() {
        // Source: https://gist.github.com/iamtodor/eb7f02fc9571cc705774408a474d5dcb
        OkHttpClient okHttpClient1 = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());

                        int days=2;
                        long cacheTime = 60 * 60 * 24 * days;

                        return originalResponse.newBuilder().header("Cache-Control", "max-age=" + (cacheTime))
                                .build();
                    }
                })
                .cache(new Cache(mActivity.getCacheDir(), Integer.MAX_VALUE))
                .build();

        OkHttp3Downloader downloader = new OkHttp3Downloader(okHttpClient1);
        Picasso picasso = new Picasso.Builder(mActivity).downloader(downloader).build();
        Picasso.setSingletonInstance(picasso);

        File[] files=mActivity.getCacheDir().listFiles();
        Log.d("FILES IN CACHE", ""+files.length);

        // indicator for checking picasso caching - need to comment out on release
        //picasso.setIndicatorsEnabled(true);

        return picasso;
    }

    /**
     * Clears file cache
     */
    public void clearFileCache(){
        File[] files=mActivity.getCacheDir().listFiles();
        for(File f:files)
            f.delete();
    }

    /**
     * Clears the file cache of old files (more than the given days old)
     */
    public void clearOldFileCache(int days){
        File[] files=mActivity.getCacheDir().listFiles();
        for(File f:files) {
            long lastModified = f.lastModified()/1000;
            long currentTime = System.currentTimeMillis()/1000;

            long cacheTime = 60 * 60 * 24 * days;

            if (currentTime-lastModified >= cacheTime)
                f.delete();
        }
    }

    @Override
    public int getItemCount() {
        if (null == collection) return 0;
        return collection.getCollectionSize();
    }

    public void clearCollection(){
        if (collection!=null) {
            collection.clear();
            notifyDataSetChanged();
        }
    }

    public void appendCollectionData(MoviesCollection fetchedMovies) {
        if (fetchedMovies != null) { // if there is at least one movie fetched (useful check if favorites is empty)...
            if (this.collection == null || fetchedMovies.getPage() == 1) {
                this.collection = fetchedMovies;
            } else {
                if (this.collection.getPage() < fetchedMovies.getPage()) {
                    this.collection.getAllItems().addAll(fetchedMovies.getAllItems());
                }
            }
        }
        notifyDataSetChanged();
    }

    public MoviesCollection getCollection() {
        return collection;
    }

    public interface MovieItemClickListener {
        void onMovieItemClick(int clickedItemIndex, ImageView sharedImage);
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        MovieRowBinding binding;
        private Context context;

        private MovieViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            context = itemView.getContext();

            itemView.setOnClickListener(this);
        }

        /**
         * Set the holder movie's thumbnail
         *
         * @param image
         */
        void setImage(String image) {
            if (image.trim().equals("")){
                // if movie has no accompanied backdrop image, load the "no image found" from the drawable folder
                binding.rowIvMovieThumb.setImageResource(R.drawable.backdrop_noimage);
            }else {
                image = context.getString(R.string.network_url_images) + context.getString(R.string.network_width_342) + image;
                Picasso.with(context)
                        .load(image)
                        .into(binding.rowIvMovieThumb);
            }
        }



        /**
         * Set the holder movie's rating
         *
         * @param rating
         */
        void setRating(float rating) {
            binding.rowRatingBar.setRating(rating);
        }

        /**
         * Set the holder movie's title
         *
         * @param title
         */
        void setTitle(String title) {
            binding.rowTvTitle.setText(title);
        }

        /**
         * Set the holder movie's release year
         *
         * @param date
         */
        void setYear(Date date) {
            if (date==null){
                binding.rowTvYear.setText("(N/A)");
            }
            else {
                DateFormat df = android.text.format.DateFormat.getDateFormat(context);
                ((SimpleDateFormat) df).applyLocalizedPattern("yyyy");
                binding.rowTvYear.setText("(".concat(df.format(date)).concat(")"));
            }
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onMovieItemClick(clickedPosition, binding.rowIvMovieThumb);
        }
    }
}
