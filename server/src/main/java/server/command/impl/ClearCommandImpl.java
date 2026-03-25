package server.command.impl;

import server.manager.CollectionManager;
import common.Response;
import server.command.Command;

public class ClearCommandImpl implements Command {
    private final CollectionManager collectionManager;

    public ClearCommandImpl(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(String[] args, Object data) {
        collectionManager.clear();
        return new Response("204", "Коллекция очищена.");
    }
}