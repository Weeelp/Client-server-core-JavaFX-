package server.command.impl;

import java.time.LocalDate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import server.manager.CollectionManager;
import server.command.Command;
import common.Response;
import common.model.movie.Movie;
import common.model.movie.MovieData;

public class AddIfMaxCommandImpl implements Command {
    private static final Logger log = LogManager.getLogger(AddIfMaxCommandImpl.class.getName());

    private final CollectionManager collectionManager;

    public AddIfMaxCommandImpl(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }
    @Override
    public Response execute(String[] args, Object data) {
        Movie maxMovie = collectionManager.getMaxByOscarsCount();
        int maxOscars = (maxMovie == null) ? 0 : maxMovie.getOscarsCount();
        
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
            if (movieData.oscarsCount > maxOscars) {
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
        } else {
                return new Response("400", "Фильм НЕ добавлен: количество Оскаров (" + movieData.oscarsCount +
                        ") не превышает текущий максимум (" + maxOscars + ").");
            }
        } catch (Exception e) {
            log.error(">> Непредвиденная ошибка: " + e.getMessage());
            return new Response("404", "Непредвиденная ошибка: " + e.getMessage());
        }
    }
}