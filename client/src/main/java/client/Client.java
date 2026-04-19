package client;

import java.io.*;
import java.net.Socket;
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
        AuthManager am = new AuthManager(null, null);

        while (true) {
            try (Socket socket = new Socket("localhost", 8000)) {
                log.info("Подключено к серверу localhost:8000");

                while (true) {
                    if (am.getLogin() == null || am.getPassword() == null) {
                        log.info("Требуется авторизация. Введите 'register' или login '[логин] [пароль]':");
                        log.info("auth > ");
                        String authLine = sc.nextLine().trim();
                        
                        if (authLine.equalsIgnoreCase("exit")) return;
                        
                        String[] authParts = authLine.split("\\s+");
                        
                        if (authParts[0].equalsIgnoreCase("register")) {
                            log.info("Регистрация. Введите новый логин и пароль:\n> ");
                            String[] regData = sc.nextLine().trim().split("\\s+");
                            if (regData.length == 2) {
                                network.sendRequest(socket, new Request("register", regData, null, regData));
                                Response resp = network.receiveResponse(socket);
                                
                                if (resp.getStatus().equals("201")) {
                                    am.setLogin(regData[0]);
                                    am.setPassword(regData[1]);
                                }
                                log.info(resp.getStatus() + ": " + resp.getMessage());
                            } else {
                                log.warn("Нужно ввести два слова!");
                            }
                            continue;
                        }

                        if (authParts.length == 2) {
                            am.setLogin(authParts[0]);
                            am.setPassword(authParts[1]);
                            log.info("Введите команду.");
                        } else {
                            log.warn("Введите логин и пароль через пробел!");
                            continue;
                        }
                    }

                    System.out.print(am.getLogin() + "> ");
                    String line = sc.nextLine().trim();
                    if (line.isEmpty()) continue;

                    if (line.equalsIgnoreCase("exit")) {
                        network.sendRequest(socket, new Request("exit", new String[0], null, new String[]{am.getLogin(), am.getPassword()}));
                        return;
                    }
                    if (line.equalsIgnoreCase("set")) {
                        am.setLogin(null);
                        am.setPassword(null);
                        log.info("Данные авторизации сброшены. Введите новые данные.");
                        continue;
                    }                    

                    String[] parts = line.split("\\s+");
                    String command = parts[0].toLowerCase();
                    String[] arg = new String[parts.length - 1];
                    System.arraycopy(parts, 1, arg, 0, parts.length - 1);

                    if (command.equals("update_by_id")) {
                        network.sendRequest(socket, new Request("update_permission", arg, null, new String[]{am.getLogin(), am.getPassword()}));
                        Response resp = network.receiveResponse(socket);

                        if (!resp.getStatus().equals("200")) {
                            System.out.println("У вас нет прав на изменение этого объекта (или он не существует).");
                            continue;
                        }
                        log.info("Доступ подтвержден. Введите новые данные для объекта.");
                    }

                    if (command.equals("execute_script")) {
                        try {
                            cm.executeScript(arg[0], socket, am.getLogin(), am.getPassword());
                        } catch (ScriptRecursionException | StopInputException e) {
                            log.error(e.getMessage());
                        }
                        continue;
                    }

                    Object data = cm.prepareData(command, arg, sc);
                    if (data instanceof String) {
                        String msg = (String) data;
                        if (msg.equals("stop")) continue;
                        if (!msg.equals("-")) { log.error(msg); continue; }
                    }

                    Request request = new Request(command, arg, data, new String[]{am.getLogin(), am.getPassword()});
                    network.sendRequest(socket, request);
                    Response response = network.receiveResponse(socket);

                    if (response.getMessage().toLowerCase().contains("Oшибка авторизации") || response.getMessage().toLowerCase().contains("неверный")) {
                        log.error("Ошибка: Данные неверны. Попробуйте снова.");
                        am.setLogin(null); 
                        am.setPassword(null);
                    } else {
                        log.info(response.getStatus() + ": " + response.getMessage());
                        if (response.getData() != null) {
                            log.info(response.getData());
                        }
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
