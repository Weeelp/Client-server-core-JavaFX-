package client.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import common.Request;
import common.Response;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class NetworkService {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public Socket getSocket (){ return this.socket; }

    public boolean tryConnect() {
        try {
            if (socket != null && !socket.isClosed() && socket.isConnected()) {
                socket.sendUrgentData(0xFF);
                return true;
            }
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress("localhost", 8000), 2000);
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void sendRequest(Request request) throws IOException {
        if (!tryConnect()) throw new IOException("Нет связи с сервером");
        
        byte[] bytes = mapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
        out.flush();
    }

    public Response receiveResponse() throws IOException {
        if (!tryConnect()) throw new IOException("Нет связи с сервером");
        
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        
        return mapper.readValue(new String(bytes, StandardCharsets.UTF_8), Response.class);
    }
    
    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}
