package client.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import common.Request;
import common.Response;
import common.utils.Config;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException; // Нужно добавить импорт
import java.nio.charset.StandardCharsets;

public class NetworkService {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public Socket getSocket() { return this.socket; }

    public boolean tryConnect() {
        try {
            // Упрощаем проверку: если сокет создан, не закрыт и подключен - работаем
            if (socket != null && !socket.isClosed() && socket.isConnected()) {
                return true;
            }
            this.socket = new Socket();
            // Подключаемся с таймаутом из конфига
            this.socket.connect(new InetSocketAddress(Config.getHost(), Config.getPort()), Config.getTimeout());
            
            // Устанавливаем дефолтный таймаут на чтение (чтобы readInt не висел вечно)
            this.socket.setSoTimeout(Config.getTimeout()); 
            
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
            return true;
        } catch (IOException e) {
            this.close(); // Если не вышло - чистим за собой
            return false;
        }
    }

    public void sendRequest(Request request) throws IOException {
        // Убираем здесь tryConnect(), воркер должен сам следить за связью
        if (socket == null || socket.isClosed()) throw new IOException("Нет соединения");
        
        byte[] bytes = mapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
        out.flush();
    }

    // Добавляем возможность менять таймаут на лету
    public Response receiveResponse(int timeout) throws IOException {
        if (socket == null || socket.isClosed()) throw new IOException("Нет соединения");
        
        try {
            socket.setSoTimeout(timeout); // Ставим нужный таймаут (напр. 300мс для обновлений)
            
            int length = in.readInt();
            byte[] bytes = new byte[length];
            in.readFully(bytes);
            
            return mapper.readValue(new String(bytes, StandardCharsets.UTF_8), Response.class);
        } catch (SocketTimeoutException e) {
            // Возвращаем null, чтобы воркер понял: данных просто пока нет
            return null; 
        }
    }
    
    // Старый метод для совместимости (использует дефолтный таймаут)
    public Response receiveResponse() throws IOException {
        return receiveResponse(Config.getTimeout());
    }
    
    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        } finally {
            this.socket = null;
            this.in = null;
            this.out = null;
        }
    }
}
