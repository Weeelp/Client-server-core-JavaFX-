package server.command.impl;

import java.util.LinkedList;

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
            return new Response("204","Коллекция пуста.", true);
        }

        LinkedList<Movie> movies = collectionManager.getAll();
        
        return new Response("200","Success", movies, true);
    }
}