package server.io;

import server.utils.XMLParser;
import common.model.movie.Movie;
import java.util.LinkedList;

public class FileReader {
    
    public LinkedList<Movie> loadFromFile(String fileName) throws Exception {
        XMLParser parser = new XMLParser();
        LinkedList<Movie> parsedMovies = parser.parse(fileName);
        parsedMovies.sort((m1, m2) -> Long.compare(m1.getId(), m2.getId()));
        return parsedMovies;
    }
}