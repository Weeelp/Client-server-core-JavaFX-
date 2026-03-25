package server.command.impl;

import java.util.stream.Collectors;
import java.util.LinkedList;

import server.manager.CollectionManager;
import server.command.Command;
import common.Response;
import common.model.movie.Movie;

public class PrintDescendingCommandImpl implements Command {
    private final CollectionManager collectionManager;

    public PrintDescendingCommandImpl(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String[] args, Object data) {
        if (collectionManager.isEmpty()) {
            return new Response("204", "Коллекция пуста. Нечего выводить.");
        }

        collectionManager.sortByIdDescending();

        LinkedList<Movie> movies = collectionManager.getAll();

        return new Response("200", "Success", "Фильмы в порядке убывания ID:" + "\n" +
               "  Всего фильмов: " + movies.size() + "\n" +
               "  " + "-".repeat(50) + "\n" +
               collectionManager.getAll().stream()
                .map(Movie::toString)
                .collect(Collectors.joining("\n")) + "\n" +        
               "  " + "-".repeat(50));
    }
}