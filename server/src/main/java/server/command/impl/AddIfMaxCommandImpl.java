package server.command.impl;

import java.time.LocalDate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import server.manager.CollectionManager;
import server.manager.DatabaseManager;
import server.command.Command;
import common.Response;
import common.model.movie.Movie;
import common.model.movie.MovieData;

public class AddIfMaxCommandImpl implements Command {
    private static final Logger log = LogManager.getLogger(AddIfMaxCommandImpl.class.getName());

    private final CollectionManager cm;
    private final DatabaseManager db;

    public AddIfMaxCommandImpl(CollectionManager cm, DatabaseManager db) {
        this.cm = cm;
        this.db = db;
    }
    @Override
    public Response execute(String[] args, Object data, String login) {
        Movie maxMovie = cm.getMaxByOscarsCount();
        int maxOscars = (maxMovie == null) ? 0 : maxMovie.getOscarsCount();
        
        ObjectMapper mapper = new ObjectMapper();

        log.info(String.valueOf(data));
        MovieData movieData;
        try {
            movieData = mapper.convertValue(data, MovieData.class);
        } catch (Exception e) {
            log.info(String.valueOf(mapper.convertValue(data, MovieData.class)));
            log.error("400: Ошибка: неверный формат данных для команды add");
            return new Response("400", "Ошибка: неверный формат данных для команды add", false);
        }
        
        try {
            if (movieData.oscarsCount > maxOscars) {
            Movie movie = new Movie(
                -1,
                movieData.name,
                movieData.coordinates,
                LocalDate.now(),
                movieData.oscarsCount,
                movieData.totalBoxOffice,
                movieData.usaBoxOffice,
                movieData.genre,
                movieData.screenWriter,
                login
            );
            
            long newId = db.insertMovieToDb(movie, login);
            movie.setId(newId);
            cm.add(movie);

            return new Response("201", "Фильм успешно добавлен! ID: " + movie.getId(), true);
        } else {
                return new Response("400", "Фильм НЕ добавлен: количество Оскаров (" + movieData.oscarsCount +
                        ") не превышает текущий максимум (" + maxOscars + ").", false);
            }
        } catch (Exception e) {
            log.error(">> Непредвиденная ошибка: " + e.getMessage());
            return new Response("404", "Непредвиденная ошибка: " + e.getMessage(), false);
        }
    }
}