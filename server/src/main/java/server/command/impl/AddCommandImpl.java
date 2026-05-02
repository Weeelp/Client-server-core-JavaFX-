package server.command.impl;

import server.manager.CollectionManager;
import server.manager.DatabaseManager;
import server.command.Command;

import java.time.LocalDate;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.Response;
import common.model.movie.Movie;
import common.model.movie.MovieData;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AddCommandImpl implements Command {
    private static final Logger log = LogManager.getLogger(AddCommandImpl.class.getName());
    private final DatabaseManager db;
    private final CollectionManager cm;

    
    public AddCommandImpl(CollectionManager cm, DatabaseManager db) {
        this.cm = cm;
        this.db = db;
    }

   @Override
    public Response execute(String[] args, Object data, String login) {
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
            if (newId == -1) throw new Exception("БД не вернула ID");
            movie.setId(newId);
            cm.add(movie);

            LinkedList<Movie> movies = cm.getAll();
            return new Response("201", "Фильм успешно добавлен! ID: " + movie.getId(), movies, true);
        } catch (Exception e) {
            log.error("Ошибка: " + e.getMessage());
            return new Response("404", "Ошибка: " + e.getMessage(), false);
        }
    }
}