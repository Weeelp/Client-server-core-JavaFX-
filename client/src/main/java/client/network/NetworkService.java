package client.network;

import com.fasterxml.jackson.databind.ObjectMapper;

import common.Request;
import common.Response;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;


public class NetworkService {
    private final ObjectMapper mapper = new ObjectMapper();

    public void sendRequest(SocketChannel socket, Request request) throws IOException {
        byte[] bytes = mapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8);
        ByteBuffer byffer = ByteBuffer.allocate(4 + bytes.length);
        byffer.putInt(bytes.length);
        byffer.put(bytes);
        byffer.flip();

        while(byffer.hasRemaining()) socket.write(byffer);
    }

    public Response receiveResponse(SocketChannel socket) throws IOException, ClassNotFoundException {
        ByteBuffer header = ByteBuffer.allocate(4);
        while (header.hasRemaining()) socket.read(header);
        header.flip();

        ByteBuffer data = ByteBuffer.allocate(header.getInt());
        while (data.hasRemaining()) socket.read(data);
        data.flip();

        return mapper.readValue(new String(data.array(), StandardCharsets.UTF_8), Response.class);
    }
}