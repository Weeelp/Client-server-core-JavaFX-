package server.command.impl;

import server.manager.CollectionManager;
import common.Response;
import server.command.Command;

public class RemoveGreaterCommandImpl implements Command {
    private final CollectionManager collectionManager;

    public RemoveGreaterCommandImpl(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String[] args, Object data) {
        if (collectionManager.isEmpty()) {
            return new Response("204","Коллекция пуста. Нечего удалять.");
        }

        if (args == null || args.length == 0) {
            return new Response("400","Не указано id фильма!");
        }

        if (args.length > 1) {
            return new Response("400","Слишком много аргументов, неверный ввод");
        } 
        
        long id;
        try {
            id = Long.parseLong(args[0]);
            if (id <= 0) {
                return new Response("400","Ошибка: id должен быть положительным числом.");
            }
        } catch (NumberFormatException e) {
            return new Response("400","Ошибка: id должен быть целым числом.");
        }

        int oldSize = collectionManager.size();
        boolean removed = collectionManager.removeGreaterThan(id);
        
        int newSize = collectionManager.size();
        int removedCount = oldSize - newSize;

        if (removed && removedCount > 0) {
            return new Response("204", "> Удалено фильмов с id больше " + id + ": " + removedCount + "\n" +
                   "> В коллекции осталось фильмов: " + newSize + "\n");
        } else {
            return new Response("404", "Фильмов с id больше " + id + " не найдено.");
        }
    }
}