package common.model;

import jakarta.xml.bind.annotation.*;
import common.model.movie.Movie;

import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "movies")
@XmlAccessorType(XmlAccessType.FIELD)
public class MoviesWrapper implements Serializable{
    private static final long serialVersionUID = 1L;
    
    @XmlElement(name = "movie")
    private List<Movie> movies;
    
    public MoviesWrapper() {}
    
    public MoviesWrapper(List<Movie> movies) {
        this.movies = movies;
    }
    
    public List<Movie> getMovies() { return movies; }
    
    public void setMovies(List<Movie> movies) { this.movies = movies; }
}