package eu.anifantakis.popularmovies.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.anifantakis.popularmovies.DataModels.ReviewsCollection;
import eu.anifantakis.popularmovies.R;
import eu.anifantakis.popularmovies.databinding.ReviewRowBinding;

public class MovieReviewsAdapter extends RecyclerView.Adapter<MovieReviewsAdapter.MovieReviewsHolder> {
    private ReviewRowBinding binding;
    private ReviewsCollection collection;

    public ReviewsCollection.MovieReview getMovieReviewAtIndex(int index) {
        return collection.getMovieReview(index);
    }

    @NonNull
    @Override
    public MovieReviewsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ReviewRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.review_row, parent, false);
        return new MovieReviewsHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull MovieReviewsHolder holder, int position) {
        holder.setAuthor(collection.getMovieReview(position).getAuthor());
        holder.setContent(collection.getMovieReview(position).getContent());
        holder.setUrl(collection.getMovieReview(position).getUrl());
    }

    @Override
    public int getItemCount() {
        if (collection != null)
            return collection.getCollectionSize();
        else
            return 0;
    }

    public void setReviewsData(ReviewsCollection reviews) {
        this.collection = reviews;
        notifyDataSetChanged();
    }

    public class MovieReviewsHolder extends RecyclerView.ViewHolder {
        private Context context;

        public MovieReviewsHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            context = itemView.getContext();
        }

        @SuppressLint("SetTextI18n")
        void setAuthor(String author){

            binding.rowReviewTvAuthor.setText(context.getString(R.string.detail_by_author) + " " + author);
        }

        void setContent(String content){
            binding.rowReviewTvContent.setText(content);
        }

        void setUrl(String url){
            binding.rowReviewTvUrl.setText(url);
        }
    }
}
