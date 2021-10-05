package eu.kazisrahi.popularmovies.DataModels;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MoviesCollection {
    private List<Movie> movieList;
    private int page = 0;
    private int totalPages = 0;
    private int totalResults = 0;

    public MoviesCollection() {
        movieList = new ArrayList<>();
    }

    public int addMovie(Movie movie) {
        movieList.add(movie);
        return movieList.size();
    }

    public void clear(){
        movieList.clear();
    }

    public Movie getMovie(int location) {
        return movieList.get(location);
    }

    public int getCollectionSize() {
        return movieList.size();
    }

    public List<Movie> getAllItems() {
        return movieList;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    /**
     * A Parcelable Movie object that is added to the collection
     * When a movie will be selected from the main activity, the movie object will be
     * added as a Parcelable extra and passed to the Detail Activity.
     */
    public static class Movie implements Parcelable {
        public static final Creator<Movie> CREATOR = new Creator<Movie>() {
            @Override
            public Movie createFromParcel(Parcel in) {
                return new Movie(in);
            }

            @Override
            public Movie[] newArray(int size) {
                return new Movie[size];
            }
        };
        private int voteCount, id;
        private double voteAverage;
        private boolean video;
        private String title;
        private double popularity;
        private String posterPath, originalLanguage, originalTitle, backdropPath;
        private List<Integer> genreIds;
        private boolean adult;
        private String overview;
        private Date releaseDate;


        public Movie(int voteCount, int id, boolean video, double voteAverage, String title,
                     double popularity, String posterPath, String originalLanguage,
                     String originalTitle, List<Integer> genreIds, String backdropPath,
                     boolean adult, String overview, Date releaseDate) {

            this.voteCount = voteCount;
            this.id = id;
            this.video = video;
            this.voteAverage = voteAverage;
            this.title = title;
            this.popularity = popularity;
            this.posterPath = posterPath;
            this.originalLanguage = originalLanguage;
            this.originalTitle = originalTitle;
            this.genreIds = genreIds;
            this.backdropPath = backdropPath;
            this.adult = adult;
            this.overview = overview;
            this.releaseDate = releaseDate;
        }

        protected Movie(Parcel in) {
            voteCount = in.readInt();
            id = in.readInt();
            voteAverage = in.readDouble();
            video = in.readByte() != 0;
            title = in.readString();
            popularity = in.readDouble();
            posterPath = in.readString();
            originalLanguage = in.readString();
            originalTitle = in.readString();
            backdropPath = in.readString();
            adult = in.readByte() != 0;
            overview = in.readString();
            long releaseDateLong = in.readLong();
            if (releaseDateLong>0) {
                releaseDate = new Date(releaseDateLong);
            }
            else{
                releaseDate = null;
            }
            in.readList(genreIds, null);
        }

        public int getVoteCount() {
            return this.voteCount;
        }

        public int getId() {
            return this.id;
        }

        public boolean getVideo() {
            return this.video;
        }

        public double getVoteAverage() {
            return this.voteAverage;
        }

        public String getTitle() {
            return this.title;
        }

        public double getPopularity() {
            return this.popularity;
        }

        public String getPosterPath() {
            return posterPath;
        }

        public String getOriginalLanguage() {
            return this.originalLanguage;
        }

        public String getOriginalTitle() {
            return this.originalTitle;
        }

        public List<Integer> getGenreIds() {
            return this.genreIds;
        }

        public String getBackdropPath() {
            return backdropPath;
        }

        public boolean getAdult() {
            return this.adult;
        }

        public String getOverview() {
            return this.overview;
        }

        public Date getReleaseDate() {
            return releaseDate;
        }

        /**
         * Get the float average voting with the specified decimal spaces
         *
         * @param decimals
         * @return the average rating (float) with the specified decimal spaces
         */
        public float getRating(int decimals) {
            return round(this.voteAverage, decimals);
        }

        /**
         * Rounding decimal places
         * Source: https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
         *
         * @param value
         * @param places
         * @return
         */
        private float round(double value, int places) {
            if (places < 0) throw new IllegalArgumentException();

            long factor = (long) Math.pow(10, places);
            value = value * factor;
            long tmp = Math.round(value);
            return (float) tmp / factor;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(voteCount);
            parcel.writeInt(id);
            parcel.writeDouble(voteAverage);
            parcel.writeByte((byte) (video ? 1 : 0));
            parcel.writeString(title);
            parcel.writeDouble(popularity);
            parcel.writeString(posterPath);
            parcel.writeString(originalLanguage);
            parcel.writeString(originalTitle);
            parcel.writeString(backdropPath);
            parcel.writeByte((byte) (adult ? 1 : 0));
            parcel.writeString(overview);
            if (releaseDate!=null) {
                parcel.writeLong(releaseDate.getTime());
            }
            else{
                parcel.writeLong(0);
            }
            parcel.writeList(genreIds);
        }
    }
}
