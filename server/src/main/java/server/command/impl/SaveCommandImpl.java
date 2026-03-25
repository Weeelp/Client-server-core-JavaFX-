package server.command.impl;

import server.manager.CollectionManager;
import server.manager.FileManager;
import server.command.Command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.Response;

public class SaveCommandImpl implements Command {
    private static final Logger log = LogManager.getLogger(SaveCommandImpl.class.getName());
    private final CollectionManager collectionManager;
    private final FileManager fileManager;

    public SaveCommandImpl(CollectionManager collectionManager, FileManager fileManager) {
        this.collectionManager = collectionManager;
        this.fileManager = fileManager;
    }

    @Override
    public Response execute(String[] args, Object data) {
        try {
            fileManager.saveToFile(collectionManager);
            return new Response("204","Коллекция успешно сохранена.");
        } catch (Exception e) {
            log.error("Ошибка сохранения: " + e.getMessage());
            return new Response("400", "Ошибка сохранения: " + e.getMessage());
        }
    }
}