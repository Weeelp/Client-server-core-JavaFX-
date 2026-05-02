package server.manager;

import server.command.Command;
import server.command.impl.*;

import common.Response;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandManager {
    private static final Logger log = LogManager.getLogger(CommandManager.class.getName());

    private Map<String, Command> commands;
    
    public CommandManager(CollectionManager cm, DatabaseManager db) {
        commands = new HashMap<>();
        
        commands.put("add", new AddCommandImpl(cm, db));
        commands.put("add_if_max", new AddIfMaxCommandImpl(cm, db));
        commands.put("show", new ShowCommandImpl(cm));
        commands.put("update_by_id", new UpdateByIdCommandImpl(cm, db));
        commands.put("remove_by_id", new RemovedByIdCommandImpl(cm, db));
        commands.put("remove_greater", new RemoveGreaterCommandImpl(cm, db));
        commands.put("register", new RegisterCommandImpl(db));
        commands.put("auth", new AuthCommandImpl(db));
    }
    
    public Response executeCommand(String commandName, String[] args, Object data, String login) {
        Command cmd = commands.get(commandName);
        if (cmd != null) {
            return (Response) cmd.execute(args, data, login);
        } else {
            log.info("Неизвестная команда: " + commandName);
            return new Response("400", "Not found command: " + commandName, false);
        }
    }
}