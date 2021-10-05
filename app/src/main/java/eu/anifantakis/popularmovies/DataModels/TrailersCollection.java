package eu.anifantakis.popularmovies.DataModels;

import java.util.ArrayList;
import java.util.List;

public class TrailersCollection {
    private List<MovieTrailer> trailersList;

    public TrailersCollection() {
        trailersList = new ArrayList<>();
    }

    public int addMovieTrailer(MovieTrailer movieTrailer) {
        trailersList.add(movieTrailer);
        return trailersList.size();
    }

    public MovieTrailer getMovieTrailer(int location) {
        return trailersList.get(location);
    }

    public int getCollectionSize() {
        return trailersList.size();
    }

    public List<MovieTrailer> getAllItems() {
        return trailersList;
    }

    public static class MovieTrailer {
        private String id;
        private String iso_3166_1;
        private String iso_639_1;
        private String key;
        private String name;
        private String site;
        private int size = 0;
        private String type;

        public MovieTrailer(String id, String iso_3166_1, String iso_639_1, String key, String name, String site, int size, String type) {
            this.id = id;
            this.iso_3166_1 = iso_3166_1;
            this.iso_639_1 = iso_639_1;
            this.key = key;
            this.name = name;
            this.site = site;
            this.size = size;
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public String getIso_3166_1() {
            return iso_3166_1;
        }

        public String getIso_639_1() {
            return iso_639_1;
        }

        public String getKey() {
            return key;
        }

        public String getName() {
            return name;
        }

        public String getSite() {
            return site;
        }

        public int getSize() {
            return size;
        }

        public String getType() {
            return type;
        }
    }
}
