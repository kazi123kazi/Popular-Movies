package eu.anifantakis.popularmovies.DataModels;

import java.util.ArrayList;
import java.util.List;

public class ReviewsCollection {
    private List<MovieReview> reviewsList;

    public ReviewsCollection() {
        reviewsList = new ArrayList<>();
    }

    public int addMovieReview(MovieReview movieReview) {
        reviewsList.add(movieReview);
        return reviewsList.size();
    }

    public MovieReview getMovieReview(int location) {
        return reviewsList.get(location);
    }

    public int getCollectionSize() {
        return reviewsList.size();
    }

    public List<MovieReview> getAllItems() {
        return reviewsList;
    }

    public static class MovieReview {
        private String id;
        private String author;
        private String content;
        private String url;

        public MovieReview(String id, String author, String content, String url) {
            this.id = id;
            this.author = author;
            this.content = content;
            this.url = url;
        }

        public String getId() {
            return id;
        }

        public String getAuthor() {
            return author;
        }

        public String getContent() {
            return content;
        }

        public String getUrl() {
            return url;
        }
    }
}
