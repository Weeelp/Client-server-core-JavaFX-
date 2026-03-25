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
    
    public CommandManager(CollectionManager cm, FileManager fm) {
        commands = new HashMap<>();
        
        commands.put("add", new AddCommandImpl(cm));
        commands.put("add_if_max", new AddIfMaxCommandImpl(cm));
        commands.put("show", new ShowCommandImpl(cm));
        commands.put("info", new InfoCommandImpl(cm));
        commands.put("update_by_id", new UpdateByIdCommandImpl(cm));
        commands.put("remove_by_id", new RemovedByIdCommandImpl(cm));
        commands.put("clear", new ClearCommandImpl(cm));
        // commands.put("save", new SaveCommandImpl(cm, fm));
        commands.put("remove_last", new RemoveLastCommandImpl(cm));
        commands.put("remove_greater", new RemoveGreaterCommandImpl(cm));
        commands.put("max_by_oscars_count", new MaxByOscarsCountCommandImpl(cm));
        commands.put("print_ascending", new PrintAscendingCommandImpl(cm));
        commands.put("print_descending", new PrintDescendingCommandImpl(cm));
        commands.put("help", new HelpCommandImpl());
        commands.put("exit", new ExitCommandImpl());
        commands.put("update", new UpdateFileCommandImpl(cm, fm));
    }
    
    public Response executeCommand(String commandName, String[] args, Object data) {
        Command cmd = commands.get(commandName);
        if (cmd != null) {
            return (Response) cmd.execute(args, data);
        } else {
            log.info("Неизвестная команда: " + commandName);
            return new Response("400", "Not found command: " + commandName);
        }
    }
}