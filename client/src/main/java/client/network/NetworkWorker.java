package client.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

import client.GuiApp;
import common.*;
import common.model.movie.Movie;
import javafx.application.Platform;

public class NetworkWorker extends Thread {
    private static final Logger log = LogManager.getLogger(NetworkWorker.class.getName());

    private final BlockingQueue<RequestTask> queue = new LinkedBlockingQueue<>();
    private final NetworkService networkService;
    private final GuiApp guiApp;
    private final ObjectMapper mapper;
    private boolean running = true;

    public NetworkWorker(NetworkService networkService, GuiApp guiApp, ObjectMapper mapper) {
        this.networkService = networkService;
        this.guiApp = guiApp;
        this.mapper = mapper;
        setDaemon(true);
    }

    public void addTask(Request request, Consumer<Response> onResult) {
        queue.add(new RequestTask(request, onResult));
    }

    @Override
    public void run() {
        while (running) {
            try {
                boolean connected = networkService.tryConnect();
                Platform.runLater(() -> guiApp.updateStatus(connected));

                if (connected) {
                    RequestTask task = queue.poll(500, java.util.concurrent.TimeUnit.MILLISECONDS);
                    if (task != null) {
                        networkService.sendRequest(task.request);
                        Response resp = networkService.receiveResponse();
                        Platform.runLater(() -> task.callback.accept(resp));
                        
                        log.info(resp.getStatus() + ": " + resp.getMessage() );
                    }
                    try {
                        networkService.getSocket().setSoTimeout(300);
                        Response updateResp = networkService.receiveResponse();
                        if (updateResp.getUpdate() && updateResp != null) {
                            handleUpdate(updateResp);
                        }
                    } catch (java.net.SocketTimeoutException ignored) {}
                } else {
                    System.err.println("Ошибка: Попытка отправить запрос без соединения.");
                }
            } catch (InterruptedException e) {
                running = false;
                break;
            } catch (Exception e) {
                System.err.println("Ошибка в цикле воркера: " + e.getMessage());
                Platform.runLater(() -> guiApp.updateStatus(false));
            }
        }
    }

    private void handleUpdate(Response resp) {
        Platform.runLater(() -> {
            try {
                LinkedList<Movie> newList = mapper.convertValue(
                    resp.getData(), 
                    new com.fasterxml.jackson.core.type.TypeReference<LinkedList<Movie>>() {}
                );
                guiApp.updateTableData(newList);
            } catch (Exception e) {
                System.err.println("Ошибка парсинга обновления: " + e.getMessage());
            }
        });
    }

    private static class RequestTask {
        Request request;
        Consumer<Response> callback;
        RequestTask(Request r, Consumer<Response> c) { 
            this.request = r; 
            this.callback = c; 
        }
    }
}
