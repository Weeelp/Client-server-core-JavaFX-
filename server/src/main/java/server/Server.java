package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import common.*;
import server.manager.*;

public class Server {
    private static final Logger log = LogManager.getLogger(Server.class.getName());
    private static CollectionManager collectionManager;
    private static CommandManager commandManager;
    private static DatabaseManager databaseManager;

    private static final ForkJoinPool readerPool = new ForkJoinPool();
    private static final ForkJoinPool processorPool = new ForkJoinPool();
    private static final ExecutorService senderPool = Executors.newFixedThreadPool(10);

    private static Selector selector;

    public static void main(String[] args) throws IOException {
        collectionManager = new CollectionManager();
        try {
            databaseManager = new DatabaseManager(collectionManager);
            log.info("Подключение к БД: {}", databaseManager.getConnection());
        } catch (SQLException e) {
            log.error("Критическая ошибка БД: {}", e.getMessage());
            return;
        }
        commandManager = new CommandManager(collectionManager, databaseManager);

        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            serverChannel.bind(new InetSocketAddress(8000));
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            log.info("Сервер запущен, порт 8000...");

            while (true) {
                if (selector.select(100) > 0) {
                    var keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();

                        if (!key.isValid()) continue;

                        if (key.isAcceptable()) {
                            SocketChannel client = serverChannel.accept();
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ, new ClientAttachment());
                            log.info("Клиент подключился: {}", client.getRemoteAddress());
                        }

                        if (key.isReadable()) { key.interestOps(0); readerPool.execute(() -> handleRead(key)); }
                    }
                }
            }
        }
    }

    private static void handleRead(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        ClientAttachment attach = (ClientAttachment) key.attachment();

        try {
            if (attach.headerBuffer.hasRemaining()) {
                if (client.read(attach.headerBuffer) == -1) throw new IOException("Закрыто coединение: " + client.getRemoteAddress());
                if (!attach.headerBuffer.hasRemaining()) {
                    attach.headerBuffer.flip();
                    attach.dataBuffer = ByteBuffer.allocate(attach.headerBuffer.getInt());
                }
            }

            if (attach.dataBuffer != null) {
                if (client.read(attach.dataBuffer) == -1) throw new IOException("Закрыто coединение: " + client.getRemoteAddress());
                if (!attach.dataBuffer.hasRemaining()) {
                    attach.dataBuffer.flip();
                    byte[] bytes = new byte[attach.dataBuffer.remaining()];
                    attach.dataBuffer.get(bytes);
                    
                    attach.dataBuffer = null;
                    attach.headerBuffer.clear();

                    processorPool.execute(() -> handleClientRequest(client, bytes, key));
                    return;
                }
            }

            key.interestOps(SelectionKey.OP_READ);
            selector.wakeup();
        } catch (IOException e) {
            closeClient(key);
        }
    }

    private static void handleClientRequest(SocketChannel client, byte[] rawData, SelectionKey key) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Request request = mapper.readValue(new String(rawData, StandardCharsets.UTF_8), Request.class);
            log.info("Обработка команды: {}", request.getCommand());

            final Response response; 
            List<String> openCommands = Arrays.asList("register", "help", "exit");
            
            if (!openCommands.contains(request.getCommand())) {
                String login = request.getUser()[0];
                String pass = request.getUser()[1];

                if (!databaseManager.checkAuth(login, pass)) {
                    response = new Response("401", "Ошибка авторизации");
                } else if (request.getCommand().equals("update_permission")) {
                    Response tempResponse;
                    try {
                        if (request.getArgs() == null || request.getArgs().length == 0) {
                            tempResponse = new Response("400", "Missing movie ID");
                        } else {
                            long id = Long.parseLong(request.getArgs()[0]);
                            if (databaseManager.checkPermissionById(id, login)) {
                                tempResponse = new Response("200", "Access granted");
                            } else {
                                tempResponse = new Response("403", "Forbidden: You are not the owner");
                            }
                        }
                    } catch (NumberFormatException e) {
                        log.error("Permission warning: {}", e.getMessage());
                        tempResponse = new Response("400", "Invalid ID format");
                    }
                    response = tempResponse;
                } else {
                    response = (Response) commandManager.executeCommand(request.getCommand(), request.getArgs(), request.getData(), login);
                }
            } else {
                response = (Response) commandManager.executeCommand(request.getCommand(), request.getArgs(), request.getData(), "");
            }


            senderPool.execute(() -> handleWrite(client, response, key, mapper));

        } catch (Exception e) {
            log.error("Ошибка обработки: {}", e.getMessage());
            closeClient(key);
        }
    }

    private static void handleWrite(SocketChannel client, Response response, SelectionKey key, ObjectMapper mapper) {
        try {
            byte[] respBytes = mapper.writeValueAsString(response).getBytes(StandardCharsets.UTF_8);
            ByteBuffer respBuffer = ByteBuffer.allocate(4 + respBytes.length);
            respBuffer.putInt(respBytes.length);
            respBuffer.put(respBytes);
            respBuffer.flip();

            synchronized (client) {
                while (respBuffer.hasRemaining()) {
                    client.write(respBuffer);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка отправки: {}", e.getMessage());
        } finally {
            if (key.isValid()) {
                key.interestOps(SelectionKey.OP_READ);
                selector.wakeup();
            }
        }
    }

    private static void closeClient(SelectionKey key) {
        try {
            log.info("Клиент отключен" + ((SocketChannel) key.channel()).getRemoteAddress());
            key.channel().close();
            key.cancel();
        } catch (IOException ignored) {}
    }

    private static class ClientAttachment {
        ByteBuffer headerBuffer = ByteBuffer.allocate(4);
        ByteBuffer dataBuffer = null;
    }
}
