package client.network;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

import client.guiApp.GuiApp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.*;
import common.model.movie.Movie;

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
                guiApp.runOnUIThread(() -> guiApp.updateStatus(connected));

                if (connected) {
                    RequestTask task = queue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                    if (task != null) {
                        try {
                            networkService.sendRequest(task.request);
                            Response resp = networkService.receiveResponse(); 
                            if (resp != null) {
                                guiApp.runOnUIThread(() -> task.callback.accept(resp));
                                log.info(resp.getStatus() + ": " + resp.getMessage());
                            }
                        } catch (Exception e) {
                            log.error("Ошибка запроса: " + e.getMessage());
                            networkService.close();
                        }
                    }

                    try {
                        Response updateResp = networkService.receiveResponse(300); 
                        
                        if (updateResp != null && updateResp.getUpdate()) {
                            handleUpdate(updateResp);
                        }
                    } catch (java.net.SocketTimeoutException ignored) {
                    } catch (Exception e) {
                        log.warn("Ошибка фонового чтения: " + e.getMessage());
                        networkService.close();
                    }
                } else {
                    Thread.sleep(2000); 
                }
            } catch (InterruptedException e) {
                running = false;
            } catch (Exception e) {
                String msg = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
                System.err.println("Ошибка в цикле воркера: " + msg);
                guiApp.runOnUIThread(() -> guiApp.updateStatus(false));
                
                try { Thread.sleep(2000); } catch (InterruptedException ex) { running = false; }
            }
        }
    }


    private void handleUpdate(Response resp) {
        guiApp.runOnUIThread(() -> {
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
