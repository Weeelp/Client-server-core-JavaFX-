package server.command.impl;

import server.manager.CollectionManager;
import server.command.Command;
import common.Response;
import common.model.movie.Movie;

public class MaxByOscarsCountCommandImpl implements Command {

    private final CollectionManager collectionManager;

    public MaxByOscarsCountCommandImpl(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String[] args, Object data) {
        if (collectionManager.isEmpty()) {
            return new Response("204", "Коллекция пуста");
        }

        Movie movie = collectionManager.getMaxByOscarsCount();

        return new Response("200", "Success", "Фильм с максимальным количеством Оскаров:" + "\n" +
               "  ID: " + movie.getId() + "\n" +
               "  Название: " + movie.getName() + "\n" +
               "  Количество Оскаров: " + movie.getOscarsCount() + "\n" +
               "  Жанр: " + movie.getGenre());

    }
}