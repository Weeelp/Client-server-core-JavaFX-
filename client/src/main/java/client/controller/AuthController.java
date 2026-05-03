package client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedList;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.type.TypeReference;

import client.guiApp.GuiApp;
import client.manager.AuthManager;
import client.network.NetworkWorker;
import common.Request;
import common.model.movie.Movie;


public class AuthController {
    private GuiApp guiApp;
    private Supplier<String> loginSupplier;
    private Supplier<String> passSupplier;
    private AuthManager authManager;

    public AuthController(GuiApp guiApp, AuthManager authManager, Supplier<String> loginSupplier, Supplier<String> passSupplier) {
        this.authManager = authManager;
        this.guiApp = guiApp;
        this.loginSupplier = loginSupplier;
        this.passSupplier = passSupplier;
    }

    public void handleAuth(String event) {
        String login = loginSupplier.get();
        String pass = passSupplier.get();

        if (guiApp.getWorker() == null) {
            guiApp.startNetworkWorker();
        }

        NetworkWorker worker  = guiApp.getWorker();
     
        if (login.isEmpty() || pass.isEmpty()) {
            guiApp.showError("Введите логин и пароль!");
            return;
        }
     
        authManager.setLogin(login);
        authManager.setPassword(pass);
        String[] authData = {login, pass};

        if (worker == null) {
            guiApp.startNetworkWorker();
        }

        Request authReq = new Request(event, authData, null, authData);
        
        worker.addTask(authReq, resp -> {
            if (resp.getStatus().equals("200") || resp.getStatus().equals("201")) {
                Request showReq = new Request("show", null, null, authData);
                worker.addTask(showReq, showResp -> {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    LinkedList<Movie> movies = mapper.convertValue(
                        showResp.getData(), 
                        new TypeReference<LinkedList<Movie>>() {}
                    );

                    guiApp.runOnUIThread(() -> {
                        guiApp.showMainWindow(movies);
                    });
                });
            } else {
                guiApp.showError("Ошибка: " + resp.getMessage());
            }
        });
    }
}

