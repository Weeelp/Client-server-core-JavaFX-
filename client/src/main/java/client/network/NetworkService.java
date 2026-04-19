package client.network;

import com.fasterxml.jackson.databind.ObjectMapper;

import common.Request;
import common.Response;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


public class NetworkService {
    private final ObjectMapper mapper = new ObjectMapper();

    public void sendRequest(Socket socket, Request request) throws IOException {
        byte[] bytes = mapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        out.writeInt(bytes.length);
        out.write(bytes);
        out.flush(); 
    }

    public Response receiveResponse(Socket socket) throws IOException, ClassNotFoundException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        int length = in.readInt();
        
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        
        return mapper.readValue(new String(bytes, StandardCharsets.UTF_8), Response.class);
    }
}