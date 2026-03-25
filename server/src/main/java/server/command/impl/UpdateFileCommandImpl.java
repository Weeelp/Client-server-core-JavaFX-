package server.command.impl;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.Response;
import server.manager.CollectionManager;
import server.manager.FileManager;
import server.command.Command;

public class UpdateFileCommandImpl implements Command {
    private static final Logger log = LogManager.getLogger(UpdateFileCommandImpl.class.getName());

    private final CollectionManager collectionManager;
    private final FileManager fileManager;

    public UpdateFileCommandImpl(CollectionManager collectionManager, FileManager fileManager) {
        this.collectionManager = collectionManager;
        this.fileManager = fileManager;
    }

    @Override
    public Response execute(String[] args, Object data) {
        try {
            collectionManager.setMovies(fileManager.loadFromFile(fileManager.getCurrentFilePath()));
            return new Response("204","Файл обновлен успешно.");
        } catch (Exception e) {
            log.error("Ошибка загрузки файла!");
            return new Response("500","Ошибка загрузки файла!");
        }
    }
}