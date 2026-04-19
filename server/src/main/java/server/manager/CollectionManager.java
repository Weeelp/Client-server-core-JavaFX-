package server.manager;

import common.model.movie.Movie;
import server.utils.MovieComparators;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CollectionManager {
    private LinkedList<Movie> movies;
    private ZonedDateTime initializationDate;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public CollectionManager() {
        this.movies = new LinkedList<>();
        this.initializationDate = ZonedDateTime.now();
    }

    public void add(Movie movie) {
        lock.writeLock().lock();
        try {
            movies.add(movie);
        } finally { lock.writeLock().unlock(); }
    }

    public boolean remove(long id) {
        lock.writeLock().lock();
        try {
            return movies.removeIf(movie -> movie.getId() == id);
        } finally { lock.writeLock().unlock(); }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            movies.clear();
            initializationDate = ZonedDateTime.now();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean removeLast() {
        lock.writeLock().lock();
        try {
            if (!movies.isEmpty()) {
                movies.removeLast();
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean removeGreaterThan(long id) {
        lock.writeLock().lock();
        try { return movies.removeIf(movie -> movie.getId() > id); } finally {
            lock.writeLock().unlock();
        }
    }

    public void setMovies(LinkedList<Movie> newMovies) {
        lock.writeLock().lock();
        try {
            this.movies = newMovies;
            this.initializationDate = ZonedDateTime.now();
            sortByIdAscending(); 
            movies.sort(MovieComparators.byIdAscending());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Movie findById(long id) {
        lock.readLock().lock();
        try {
            return movies.stream()
                    .filter(m -> m.getId() == id)
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    public LinkedList<Movie> getAll() {
        lock.readLock().lock();
        try { return new LinkedList<>(movies); } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return movies.isEmpty();
        } finally { lock.readLock().unlock(); }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return movies.size();
        } finally { lock.readLock().unlock(); }
    }

    public long getMaxId() {
        lock.readLock().lock();
        try {
            return movies.stream()
                    .mapToLong(Movie::getId)
                    .max()
                    .orElse(0);
        } finally {
            lock.readLock().unlock();
        }
    }

    public long generateId() {
        lock.readLock().lock();
        try {
            return getMaxId() + 1;
        } finally { lock.readLock().unlock(); }
    }

    public void sortByIdAscending() {
        lock.readLock().lock();
        try {
            movies.sort(MovieComparators.byIdAscending());
        } finally { lock.readLock().unlock(); }
    }

    public void sortByIdDescending() {
        lock.readLock().lock();
        try {
            movies.sort(MovieComparators.byIdDescending());
        } finally { lock.readLock().unlock(); }
    }

    public Movie getMaxByOscarsCount() {
        lock.readLock().lock();
        try {
            return movies.stream()
                    .max((m1, m2) -> Integer.compare(m1.getOscarsCount(), m2.getOscarsCount()))
                    .orElse(null);
        } finally { lock.readLock().unlock(); }
    }

    public ZonedDateTime getInitializationDate() {
        lock.readLock().lock();
        try {
            return initializationDate;
        } finally { lock.readLock().unlock(); }
    }
}