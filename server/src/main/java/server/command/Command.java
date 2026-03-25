package server.command;

import common.Response;

public interface Command {
        Response execute(String[] args, Object data);
}