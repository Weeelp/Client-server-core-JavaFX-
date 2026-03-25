package server.command.impl;

import server.manager.CollectionManager;
import common.Response;
import server.command.Command;

public class RemoveLastCommandImpl implements Command {
    private final CollectionManager collectionManager;

    public RemoveLastCommandImpl(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String[] args, Object data) {
        if (collectionManager.isEmpty()) {
            return new Response("204","Коллекция пуста. Нечего удалять.");
        }
        
        boolean removed = collectionManager.removeLast();
        
        if (removed) {
            return new Response("400","> Последний фильм успешно удалён.");

        } else {
            return new Response("404","Не удалось удалить последний фильм.");
        }
    }
}