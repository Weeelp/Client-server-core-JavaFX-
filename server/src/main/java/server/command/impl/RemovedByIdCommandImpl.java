package server.command.impl;

import server.manager.CollectionManager;
import common.Response;
import server.command.Command;

public class RemovedByIdCommandImpl implements Command {
    private final CollectionManager collectionManager;

    public RemovedByIdCommandImpl(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String[] args, Object data) {

        if (args == null || args.length == 0) {
            return new Response("400", "Не указано id фильма!");
        }

        if (args.length > 1) {
            return new Response("400","Слишком много аргументов, неверный ввод");
        } 
 
        long id;

        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            return new Response("400","Ошибка: id должен быть целым числом.");
        }

        if (collectionManager.findById(id) == null) {
            return new Response("404","Фильм с id " + id + " не найден.");
        }

        boolean removed = collectionManager.remove(id);

        if (removed) {
            return new Response("204", "Фильм с id " + id + " успешно удалён." + "\n" +
                   "> В коллекции осталось фильмов: " + collectionManager.size());
        } else {
            return new Response("400", "Не удалось удалить фильм с id " + id + ".");
        }
    }
}