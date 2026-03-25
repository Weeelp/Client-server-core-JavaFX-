package server.manager;

import common.model.movie.Movie;
import server.utils.MovieComparators;
import java.time.ZonedDateTime;
import java.util.LinkedList;

public class CollectionManager {
    private LinkedList<Movie> movies;
    private ZonedDateTime initializationDate;
    
    public CollectionManager() {
        this.movies = new LinkedList<>();
        this.initializationDate = ZonedDateTime.now();
    }
    
    public void add(Movie movie) {
        movies.add(movie);
    }
    
    public boolean remove(long id) {
        return movies.removeIf(movie -> movie.getId() == id);
    }
    
    public void clear() {
        movies.clear();
        initializationDate = ZonedDateTime.now();
    }
    
    public boolean removeLast() {
        if (!movies.isEmpty()) {
            movies.removeLast();
            return true;
        }
        return false;
    }
    
    public boolean removeGreaterThan(long id) {
        return movies.removeIf(movie -> movie.getId() > id);
    }
    
    public Movie findById(long id) {
        return movies.stream()
                .filter(m -> m.getId() == id)
                .findFirst()
                .orElse(null);
    }
    
    public LinkedList<Movie> getAll() {
        return new LinkedList<>(movies);
    }
    
    public boolean isEmpty() {
        return movies.isEmpty();
    }
    
    public int size() {
        return movies.size();
    }
    
    public long getMaxId() {
        return movies.stream()
                .mapToLong(Movie::getId)
                .max()
                .orElse(0);
    }
    
    public long generateId() {
        return getMaxId() + 1;
    }
    
    public void sortByIdAscending() {
        movies.sort(MovieComparators.byIdAscending());
    }
    
    public void sortByIdDescending() {
        movies.sort(MovieComparators.byIdDescending());
    }
    
    public Movie getMaxByOscarsCount() {
        return movies.stream()
                .max((m1, m2) -> Integer.compare(m1.getOscarsCount(), m2.getOscarsCount()))
                .orElse(null);
    }
    
    public ZonedDateTime getInitializationDate() {
        return initializationDate;
    }
    
    public void setMovies(LinkedList<Movie> newMovies) {
        this.movies = newMovies;
        this.initializationDate = ZonedDateTime.now();
        sortByIdAscending();
    }
}