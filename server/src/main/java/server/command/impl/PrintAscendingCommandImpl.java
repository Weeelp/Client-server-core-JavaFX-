package server.command.impl;

import java.util.LinkedList;
import java.util.stream.Collectors;

import server.manager.CollectionManager;
import server.command.Command;
import common.Response;
import common.model.movie.Movie;

public class PrintAscendingCommandImpl implements Command {
    private final CollectionManager collectionManager;

    public PrintAscendingCommandImpl(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String[] args, Object data) {
        if (collectionManager.isEmpty()) {
            return new Response("204", "Коллекция пуста");
        }

        collectionManager.sortByIdAscending();

        LinkedList<Movie> movies = collectionManager.getAll();

        return new Response("200", "Success", "Фильмы в порядке возрастания ID:" + "\n" +
               "  Всего фильмов: " + movies.size() + "\n" +
               "  " + "-".repeat(50) + "\n" +
               collectionManager.getAll().stream()
                .map(Movie::toString)
                .collect(Collectors.joining("\n")) + "\n" +        
               "  " + "-".repeat(50));
    }
}