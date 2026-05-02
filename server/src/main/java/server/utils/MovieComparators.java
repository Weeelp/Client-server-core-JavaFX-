package server.utils;

import java.util.Comparator;
import common.model.movie.Movie;

public class MovieComparators {

    public static Comparator<Movie> byName() {
        return Comparator.comparing(Movie::getName);
    }
    
    public static Comparator<Movie> byIdAscending() {
        return Comparator.comparingLong(Movie::getId);
    }
    
    public static Comparator<Movie> byIdDescending() {
        return Comparator.comparingLong(Movie::getId).reversed();
    }
}