package client.network;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException; 
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import common.*;
import common.utils.Config;

public class NetworkService {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public Socket getSocket() { return this.socket; }

    public boolean tryConnect() {
        try {
            if (socket != null && !socket.isClosed() && socket.isConnected()) {
                return true;
            }
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(Config.getHost(), Config.getPort()), Config.getTimeout());
            
            this.socket.setSoTimeout(Config.getTimeout()); 
            
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
            return true;
        } catch (IOException e) {
            this.close();
            return false;
        }
    }

    public void sendRequest(Request request) throws IOException {
        if (socket == null || socket.isClosed()) throw new IOException("Нет соединения");
        
        byte[] bytes = mapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
        out.flush();
    }

    public Response receiveResponse(int timeout) throws IOException {
        if (socket == null || socket.isClosed()) throw new IOException("Нет соединения");
        
        try {
            socket.setSoTimeout(timeout);
            
            int length = in.readInt();
            byte[] bytes = new byte[length];
            in.readFully(bytes);
            
            return mapper.readValue(new String(bytes, StandardCharsets.UTF_8), Response.class);
        } catch (SocketTimeoutException e) {
            return null; 
        }
    }
    
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
