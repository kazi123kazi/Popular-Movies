package eu.kazisrahi.popularmovies.Adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import eu.kazisrahi.popularmovies.R;
import eu.kazisrahi.popularmovies.DataModels.TrailersCollection;
import eu.kazisrahi.popularmovies.databinding.TrailerRowBinding;

public class MovieTrailersAdapter extends RecyclerView.Adapter<MovieTrailersAdapter.MovieTrailersHolder> {
    final private TrailerItemClickListener mOnClickListener;
    private TrailerRowBinding binding;
    private TrailersCollection collection;

    public MovieTrailersAdapter(TrailerItemClickListener clickListener) {
        this.mOnClickListener = clickListener;
    }

    public TrailersCollection.MovieTrailer getMovieTrailerAtIndex(int index) {
        return collection.getMovieTrailer(index);
    }

    @NonNull
    @Override
    public MovieTrailersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        TrailerRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.trailer_row, parent, false);
        return new MovieTrailersHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull MovieTrailersHolder holder, int position) {
        holder.setImage(collection.getMovieTrailer(position).getKey());
    }

    @Override
    public int getItemCount() {
        if (collection != null)
            return collection.getCollectionSize();
        else
            return 0;
    }

    public void setTrailersData(TrailersCollection trailers) {
        this.collection = trailers;
        notifyDataSetChanged();
    }

    public interface TrailerItemClickListener {
        void onTrailerItemClick(int clickedItemIndex);
    }

    public class MovieTrailersHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Context context;

        public MovieTrailersHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            context = itemView.getContext();
            itemView.setOnClickListener(this);
        }

        void setImage(String image) {
            String thumbnailUrl = context.getString(R.string.youtube_img_url) + image + "/mqdefault.jpg";

            Picasso.with(context)
                    .load(thumbnailUrl)
                    .into(binding.rowIvTrailerThumb);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onTrailerItemClick(clickedPosition);
        }
    }
}
