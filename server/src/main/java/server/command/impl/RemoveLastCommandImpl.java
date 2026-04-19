package server.command.impl;

import java.sql.SQLException;

import server.manager.CollectionManager;
import server.manager.DatabaseManager;
import server.command.Command;
import common.Response;

public class RemoveLastCommandImpl implements Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager db;

    public RemoveLastCommandImpl(CollectionManager collectionManager, DatabaseManager db) {
        this.collectionManager = collectionManager;
        this.db = db;
    }

    @Override
    public Response execute(String[] args, Object data, String login) {
        if (collectionManager.isEmpty()) {
            return new Response("204","Коллекция пуста. Нечего удалять.");
        }
        
        boolean removed;
        try {
        removed = db.deleteMovieById(collectionManager.getMaxId(), login);
        } catch (SQLException e){
            return new Response("500" , "Ошибка удаления");
        }
        removed&=collectionManager.removeLast();
        
        if (removed) {
            return new Response("400","> Последний фильм успешно удалён.");

        } else {
            return new Response("404","Не удалось удалить последний фильм. У вас нет прав");
        }
    }
}