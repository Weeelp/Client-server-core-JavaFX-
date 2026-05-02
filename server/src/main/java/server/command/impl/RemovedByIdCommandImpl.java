package server.command.impl;

import server.manager.CollectionManager;
import server.manager.DatabaseManager;
import java.sql.SQLException;
import java.util.LinkedList;

import common.model.movie.Movie;
import common.Response;
import server.command.Command;

public class RemovedByIdCommandImpl implements Command {
    private final CollectionManager cm;
    private final DatabaseManager db;


    public RemovedByIdCommandImpl(CollectionManager cm, DatabaseManager db) {
        this.cm = cm;
        this.db = db;
    }

    @Override
    public Response execute(String[] args, Object data, String login) {

        if (args == null || args.length == 0) {
            return new Response("400", "Не указано id фильма!", false);
        }

        if (args.length > 1) {
            return new Response("400","Слишком много аргументов, неверный ввод", false);
        } 
 
        long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            return new Response("400","Ошибка: id должен быть целым числом.", false);
        }

        if (cm.findById(id) == null) {
            return new Response("404","Фильм с id " + id + " не найден.", false);
        }

        boolean removed;
        try {
        removed = db.deleteMovieById(id, login);
        } catch (SQLException e){
            return new Response("500" , "Ошибка удаления", false);
        }
        removed&=cm.remove(id);

        if (removed) {
            LinkedList<Movie> movies = cm.getAll();
            return new Response("200", "Фильм с id " + id + " успешно удалён." + "\n" +
                   "> В коллекции осталось фильмов: " + cm.size(), movies, true);
        } else {
            return new Response("400", "Не удалось удалить фильм с id " + id + ". У вас нет прав", false);
        }
    }
}