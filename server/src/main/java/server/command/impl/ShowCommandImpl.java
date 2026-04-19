package server.command.impl;

import java.util.LinkedList;
import java.util.stream.Collectors;

import server.manager.CollectionManager;
import server.command.Command;
import common.Response;
import common.model.movie.Movie;

public class ShowCommandImpl implements Command {
    private final  CollectionManager collectionManager;

    public ShowCommandImpl(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String[] args, Object data, String login) {
        if (collectionManager.isEmpty()) {
            return new Response("204","Коллекция пуста.");
        }

        LinkedList<Movie> movies = collectionManager.getAll();
        
        return new Response("200","Success", "Содержимое коллекции (всего фильмов: " + movies.size() + "):\n" + 
                "  " + "-".repeat(60)+"\n" +
                collectionManager.getAll().stream()
                .map(Movie::toString)
                .collect(Collectors.joining("\n")) +
                "\n" +
                "  " + "-".repeat(60));
    }
}