package client.manager;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import client.network.NetworkService;
import client.utils.MovieInputReader;
import common.Request;
import common.Response;
import common.exceptions.*;

public class CommandManager {
    private static final Logger log = LogManager.getLogger(CommandManager.class.getName());
    private final NetworkService networkService;
    private static final Set<String> executingScripts = new HashSet<>();

    public CommandManager(NetworkService nw) {
        this.networkService = nw;
    }

    private static final Set<String> NO_ARGS_COMMANDS = Set.of(
        "show", "info", "clear", "remove_last", "max_by_oscars_count",
        "print_ascending", "print_descending", "help", "exit", "update", "register"
    );

    private static final Set<String> ONE_NUM_ARG_COMMANDS = Set.of(
        "update_by_id", "remove_by_id", "remove_greater", "execute_script"
    );

    public Object prepareData(String command, String[] args, Scanner sc) {
        if (NO_ARGS_COMMANDS.contains(command)) {
            if (args.length > 0) {
                return "Ошибка: команда " + command + " не принимает аргументов";
            }
            return "-";
        }

        if (ONE_NUM_ARG_COMMANDS.contains(command) && !command.equals("execute_script")) {
            if (args.length != 1) {
                return "Ошибка: команда " + command + " требует ровно один аргумент (id)";
            }
            try {
                Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                return "Ошибка: id должен быть целым числом";
            }
            if (command.equals("update_by_id")) {
                MovieInputReader mir = new MovieInputReader(sc);
                try {
                    return mir.readMovieData();
                } catch (StopInputException e) {
                    return "stop";
                } catch (ValidationException e) {
                    log.error("Ошибка валидации: " + e.getMessage());
                    return "Ошибка: " + e.getMessage();
                }
            }
            return "-";
        }

        if (command.equals("add") || command.equals("add_if_max")) {
            if (args.length > 0) {
                return "Ошибка: команда " + command + " не принимает аргументов";
            }
            MovieInputReader mir = new MovieInputReader(sc);
            try {
                return mir.readMovieData();
            } catch (StopInputException e) {
                return "stop";
            } catch (ValidationException e) {
                log.error("Ошибка валидации: " + e.getMessage());
                return "Ошибка: " + e.getMessage();
            }
        }

        return "Неизвестная команда: " + command;
    }

    public void executeScript(String filename, Socket socket, String login, String password) throws ScriptRecursionException, StopInputException, IOException {
        if (filename == null || filename.isEmpty()) {
            log.info("Укажите имя файла");
            return;
        }

        if (executingScripts.contains(filename)) {
            log.error("Ошибка: рекурсивный вызов скрипта запрещён!");
            throw new ScriptRecursionException("Рекурсивный вызов скрипта: " + filename);
        }

        executingScripts.add(filename);

        try (Scanner scriptScanner = new Scanner(new File(filename))) {
            int lineCount = 0;
            log.info("Выполнение скрипта: " + filename);

            while (scriptScanner.hasNextLine()) {
                String line = scriptScanner.nextLine().trim();
                lineCount++;
                
                if (line.isEmpty()) continue;

                log.info("[" + lineCount + "] Команда: " + line);

                String[] parts = line.split("\\s+");
                String command = parts[0].toLowerCase();
                String[] args = (parts.length > 1) ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

                if (command.equals("execute_script")) {
                    if (args.length == 0) {
                        log.error("Нет имени файла для execute_script");
                    } else {
                        executeScript(args[0], socket, login, password);
                    }
                    continue;
                }

                try {
                    Object data = prepareData(command, args, scriptScanner);

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

                    Request request = new Request(command, args, data, new String[]{login, password});
                    networkService.sendRequest(socket, request);
                    Response response = networkService.receiveResponse(socket);

                    log.info(response.getStatus() + ": " + response.getData());

                } catch (ExitException e) {
                    log.error("Ошибка в данных скрипта (" + filename + "): " + e.getMessage());
                    log.error("Пропускаем команду...");
                } catch (IOException | ClassNotFoundException e) {
                    log.error("Ошибка связи с сервером: " + e.getMessage());
                    break;
                }}
                log.info("Скрипт выполнен: " + filename + ", итераций: " + lineCount);

        } catch (FileNotFoundException e) {
            log.error("Файл не найден: " + filename);
        } catch (IOException e) {
            log.error("Ошибка чтения файла: " + e.getMessage());
        } finally {
            executingScripts.remove(filename);
        }
    } 
}