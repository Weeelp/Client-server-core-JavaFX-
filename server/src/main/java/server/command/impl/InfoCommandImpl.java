package server.command.impl;

import java.util.LinkedList;

import server.manager.CollectionManager;
import server.command.Command;
import common.Response;
import common.model.movie.Movie;

public class InfoCommandImpl implements Command {

    private final CollectionManager collectionManager;

    public InfoCommandImpl(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String[] args, Object data, String login) {

        LinkedList<Movie> movies = collectionManager.getAll();
        if (movies.isEmpty()) {
                    return new Response("204", "Коллекция пуста.");
        } else {
            return new Response("200", "Success", "Информация о коллекции:" + "\n" +
                "  Дата инициализации: " + collectionManager.getInitializationDate() + "\n" +
                "  Тип коллекции: " + movies.getClass().getSimpleName() + "\n" +
                "  Количество элементов: " + movies.size() + "\n" +
                "  Минимальный ID: " + movies.getFirst().getId() + "\n" +
                "  Максимальный ID: " + movies.getLast().getId());
        }
    }
}