package server.io;

import server.utils.XMLParser;
import common.model.movie.Movie;
import java.io.IOException;
import java.util.List;

public class FileWriter {
    
    public void saveToFile(String fileName, List<Movie> movies) throws IOException {
        XMLParser parser = new XMLParser();
        parser.save(fileName, movies);
    }
}