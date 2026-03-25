package server.command.impl;

import common.Response;
import server.command.Command;

public class ExitCommandImpl implements Command{
    @Override
    public Response execute (String[] args, Object data){
        return new Response("204", "Программа завершена");
    }
    
}