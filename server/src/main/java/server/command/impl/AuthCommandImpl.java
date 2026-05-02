package server.command.impl;

import server.manager.DatabaseManager;
import server.command.Command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.Response;


public class AuthCommandImpl implements Command {
    private static final Logger log = LogManager.getLogger(RegisterCommandImpl.class.getName());
    private final DatabaseManager db;
    
    public AuthCommandImpl(DatabaseManager db) {
        this.db = db;
    }

   @Override
    public Response execute(String[] args, Object data, String login) {
        if (args == null || args.length < 2) {
            return new Response("400", "Необходимо указать логин и пароль", false);
        }

       try{
        log.info(String.valueOf(args));
         if (db.checkAuth(args[1], args[0])){
            return new Response("201", "Пользователь успешно авторизовался login: " + args[1], false);
         } else return new Response("400", "Пользователь не найден: " + args[1], false);

            
        } catch (Exception e) {
            log.error("Ошибка: " + e.getMessage());
            return new Response("404", "Ошибка: " + e.getMessage(), false);
        }
    }
}