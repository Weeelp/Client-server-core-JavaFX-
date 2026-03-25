package client;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Scanner;

import common.*;
import common.exceptions.ScriptRecursionException;
import common.exceptions.StopInputException;
import client.manager.*;
import client.network.NetworkService;

public class Client {
    private static final Logger log = LogManager.getLogger(Client.class.getName());
    private static final int RECONNECT_DELAY_MS = 5000;

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        NetworkService network = new NetworkService();
        CommandManager cm = new CommandManager(network);

        while (true) {
            try (SocketChannel socket = SocketChannel.open()) {
                log.info("Подключение к серверу localhost:8000...");
                socket.connect(new InetSocketAddress("localhost", 8000));
                socket.configureBlocking(true);
                log.info("Подключено к серверу. Введите команды (exit для выхода):");

                while (true) {
                    System.out.print("> ");
                    String line = sc.nextLine();
                    if (line.equalsIgnoreCase("exit")) {
                        Request exitRequest = new Request("exit", new String[0], null);
                        network.sendRequest(socket, exitRequest);
                        socket.close();
                        return;
                    }
                    if (line.isEmpty()) continue;

                    String[] parts = line.trim().split("\\s+");
                    String command = parts[0].toLowerCase();
                    String[] arg = new String[parts.length - 1];
                    System.arraycopy(parts, 1, arg, 0, parts.length - 1);
                    if (arg.length == 1 && arg[0].isEmpty()) {
                        arg = new String[0];
                    }

                    if (command.equals("execute_script")) {
                        try {
                            cm.executeScript(arg[0], socket);
                        } catch (ScriptRecursionException | StopInputException e) {
                            log.error(e.getMessage());
                        }
                        continue;
                    }
                     Object data = cm.prepareData(command, arg, sc);
                    if (data instanceof String) {
                        String msg = (String) data;
                        if (msg.equals("stop")) {
                            log.info("Ввод прерван командой stop.");
                            continue;
                        }
                        if (!msg.equals("-")) {    
                            log.error(msg);
                            continue;
                        }
                    }

                    Request request = new Request(command, arg, data);

                    network.sendRequest(socket, request);
                    Response response = network.receiveResponse(socket);

                    if(response.getData() == null){
                        log.info(String.valueOf(response.getStatus())+ ": " + String.valueOf(response.getMessage()));
                    } else {
                        log.info(String.valueOf(response.getStatus())+ ": " + String.valueOf(response.getMessage()));
                        System.out.prinln(response.getData());
                    }
                }
            } catch (IOException e) {
                log.error("Сервер недоступен: " + e.getMessage());
                log.info("Повторная попытка через " + RECONNECT_DELAY_MS / 1000 + " секунд...");
                try {
                    Thread.sleep(RECONNECT_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            } catch (ClassNotFoundException e) {
                log.error("Ошибка десериализации: " + e.getMessage());
                return;
            }
        }
    }
}