package server;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import common.*;
import server.manager.*;

public class Server {
    private static final Logger log = LogManager.getLogger(Server.class.getName());
    private static CollectionManager collectionManager;
    private static FileManager fileManager;
    private static CommandManager commandManager;

    public static void main(String[] args) throws IOException {

        collectionManager = new CollectionManager();
        fileManager = new FileManager(collectionManager);
        commandManager = new CommandManager(collectionManager, fileManager);

        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            serverChannel.bind(new InetSocketAddress(8000));
            serverChannel.configureBlocking(false);
            log.info("Сервер запущен...");

            Selector selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);    

            while (true) {
                if (selector.select(100) > 0) {
                    var keys = selector.selectedKeys().iterator();

                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();
                        
                        if (key.isAcceptable()) {
                            SocketChannel client = serverChannel.accept();
                            client.configureBlocking(false);
                            log.info("Клиент подключился: " + client.getRemoteAddress());

                            client.register(selector, SelectionKey.OP_READ, new ClientAttachment());
                        }

                        if (key.isReadable()) {
                            SocketChannel client = (SocketChannel) key.channel();
                            ClientAttachment attach = (ClientAttachment) key.attachment();

                            try {
                                if (attach.headerBuffer.hasRemaining()) {
                                    if (client.read(attach.headerBuffer) == -1) { 
                                        throw new IOException("Клиент закрыл соединение"); 
                                    }
                                    
                                    if (!attach.headerBuffer.hasRemaining()) {
                                        attach.headerBuffer.flip();
                                        int size = attach.headerBuffer.getInt();
                                        attach.dataBuffer = ByteBuffer.allocate(size);
                                    }
                                }

                                if (attach.dataBuffer != null) {
                                    int read = client.read(attach.dataBuffer);
                                    
                                    if (read == -1) {
                                        throw new IOException("Клиент отключился во время передачи данных");
                                    }

                                    if (!attach.dataBuffer.hasRemaining()) {
                                        attach.dataBuffer.flip();
                                        byte[] bytes = new byte[attach.dataBuffer.remaining()];
                                        attach.dataBuffer.get(bytes);

                                        ObjectMapper mapper = new ObjectMapper();
                                        Request request = mapper.readValue(new String(bytes, StandardCharsets.UTF_8), Request.class);
                                        log.info(String.valueOf(client.getRemoteAddress()) + ": " + request.getCommand());                                           
                                        
                                        Response response;
                                        try {
                                            response = (Response) commandManager.executeCommand(request.getCommand(), request.getArgs(), request.getData());  
                                        } catch (Exception e) {
                                            response = new Response("404", "Ошибка: " + e.getMessage());
                                        }

                                        byte[] respBytes = mapper.writeValueAsString(response).getBytes(StandardCharsets.UTF_8);
                                        ByteBuffer respBuffer = ByteBuffer.allocate(4 + respBytes.length);
                                        respBuffer.putInt(respBytes.length);
                                        respBuffer.put(respBytes);
                                        respBuffer.flip();
                                        while (respBuffer.hasRemaining()) client.write(respBuffer);

                                        attach.dataBuffer = null;
                                        attach.headerBuffer.clear();
                                    }
                                }
                            } catch (IOException e) {
                                log.error("КЛИЕНТ ОТКЛЮЧИЛСЯ: " + e.getMessage());
                                fileManager.saveToFile(collectionManager);
                                log.info("Коллекция сохранена при отключении клиента.");
                                key.cancel();
                                try { client.close(); } catch (IOException ex) {}
                            }
                        }
                    }
                }
                if (System.in.available() > 0) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    String line = reader.readLine();
                    if (line != null) {
                        log.info("Консольная команда: " + line);
                        if (line.equalsIgnoreCase("save")) fileManager.saveToFile(collectionManager);
                    }
                }
            } 
        
        } finally {
            fileManager.saveToFile(collectionManager);
            log.info("Коллекция сохранена при завершении сервера.");

        } 
    }

    private static class ClientAttachment {
        ByteBuffer headerBuffer = ByteBuffer.allocate(4);
        ByteBuffer dataBuffer = null;
    }
}