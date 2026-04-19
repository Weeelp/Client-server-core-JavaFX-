package server.command.impl;

import server.manager.DatabaseManager;
import server.command.Command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.Response;


public class RegisterCommandImpl implements Command {
    private static final Logger log = LogManager.getLogger(RegisterCommandImpl.class.getName());
    private final DatabaseManager db;

    
    public RegisterCommandImpl(DatabaseManager db) {
        this.db = db;
    }

   @Override
    public Response execute(String[] args, Object data, String login) {
        if (args == null || args.length < 2) {
            return new Response("400", "Необходимо указать логин и пароль");
        }

       try{
        log.info(String.valueOf(args));
            db.createUser(args[1], args[0]);

            return new Response("201", "Пользователь успешно добавлен! login: " + args[1]);
        } catch (Exception e) {
            log.error("Ошибка: " + e.getMessage());
            return new Response("404", "Ошибка: " + e.getMessage());
        }
    }
}