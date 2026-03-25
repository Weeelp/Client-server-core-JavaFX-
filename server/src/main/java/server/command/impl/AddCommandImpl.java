package server.command.impl;

import server.manager.CollectionManager;

import java.time.LocalDate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.command.Command;
import common.Response;
import common.model.movie.Movie;
import common.model.movie.MovieData;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AddCommandImpl implements Command {
    private static final Logger log = LogManager.getLogger(AddCommandImpl.class.getName());
    private final CollectionManager collectionManager;
    
    public AddCommandImpl(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

   @Override
    public Response execute(String[] args, Object data) {
        ObjectMapper mapper = new ObjectMapper();

        log.info(String.valueOf(data));
        MovieData movieData;
        try {
            movieData = mapper.convertValue(data, MovieData.class);
        } catch (Exception e) {
            log.info(String.valueOf(mapper.convertValue(data, MovieData.class)));
            log.error("400: Ошибка: неверный формат данных для команды add");
            return new Response("400", "Ошибка: неверный формат данных для команды add");
        }

        try {
            Movie movie = new Movie(
                collectionManager.generateId(),
                movieData.name,
                movieData.coordinates,
                LocalDate.now(),
                movieData.oscarsCount,
                movieData.totalBoxOffice,
                movieData.usaBoxOffice,
                movieData.genre,
                movieData.screenWriter
            );
            collectionManager.add(movie);
            return new Response("201", "Фильм успешно добавлен! ID: " + movie.getId());
        } catch (Exception e) {
            log.error("Ошибка: " + e.getMessage());
            return new Response("404", "Ошибка: " + e.getMessage());
        }
    }
}