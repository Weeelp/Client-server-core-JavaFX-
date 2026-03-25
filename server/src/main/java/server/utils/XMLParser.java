package server.utils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import common.model.movie.Movie;
import common.model.MoviesWrapper;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class XMLParser {
    
    public LinkedList<Movie> parse(String fileName) throws IOException {
        try {
            JAXBContext context = JAXBContext.newInstance(MoviesWrapper.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            
            try (Reader reader = new InputStreamReader(new FileInputStream(fileName), "UTF-8")) {
                MoviesWrapper wrapper = (MoviesWrapper) unmarshaller.unmarshal(reader);
                return wrapper.getMovies() == null ? 
                       new LinkedList<>() : new LinkedList<>(wrapper.getMovies());
            }
        } catch (JAXBException e) {
            throw new IOException("Ошибка парсинга XML: " + e.getMessage(), e);
        }
    }
    
    public void save(String fileName, List<Movie> movies) throws IOException {
        try {
            JAXBContext context = JAXBContext.newInstance(MoviesWrapper.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            
            MoviesWrapper wrapper = new MoviesWrapper(movies);
            
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8")) {
                marshaller.marshal(wrapper, writer);
            }
        } catch (JAXBException e) {
            throw new IOException("Ошибка сохранения XML: " + e.getMessage(), e);
        }
    }
}