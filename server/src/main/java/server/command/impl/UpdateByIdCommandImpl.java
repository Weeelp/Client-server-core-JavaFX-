package server.command.impl;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import server.manager.CollectionManager;
import server.command.Command;
import common.Response;
import common.model.movie.Movie;
import common.model.movie.MovieData;

public class UpdateByIdCommandImpl implements Command {
    private static final Logger log = LogManager.getLogger(UpdateByIdCommandImpl.class.getName());

    private final CollectionManager collectionManager;
    
    public UpdateByIdCommandImpl(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String[] args, Object data) {
        ObjectMapper mapper = new ObjectMapper();

        MovieData movieData;
        try {
            movieData = mapper.convertValue(data, MovieData.class);
        } catch (Exception e) {
            log.info(String.valueOf(mapper.convertValue(data, MovieData.class)));
            log.error("400: Ошибка: неверный формат данных для команды add");
            return new Response("400", "Ошибка: неверный формат данных для команды add");
        }
 
        long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            log.info("> id должен быть числом");
            return new Response("400","id должен быть числом");
        }
        
        Movie movie = collectionManager.findById(id);
        if (movie == null) {
            log.info("Фильм с id " + id + " не найден");
            return new Response("404","Фильм с id " + id + " не найден");
        }
        
        try {
            log.info("Редактирование фильма ID " + id);
            log.info("Текущее название: " + movie.getName());
    
            movie.setName(movieData.name);
            movie.setCoordinates(movieData.coordinates);
            movie.setOscarsCount(movieData.oscarsCount);
            movie.setTotalBoxOffice(movieData.totalBoxOffice);
            movie.setUsaBoxOffice(movieData.usaBoxOffice);
            movie.setGenre(movieData.genre);
            movie.setScreenWriter(movieData.screenWriter);

            return new Response("200","Фильм успешно обновлен! ID: " + movie.getId());
        } catch (Exception e) {
            log.error("Ошибка: " + e.getMessage());
            return new Response("500","Ошибка: " + e.getMessage());
        }
    }
}