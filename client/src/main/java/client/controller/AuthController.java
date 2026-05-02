package client.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedList;

import com.fasterxml.jackson.core.type.TypeReference;

import client.GuiApp;
import client.manager.AuthManager;
import client.network.NetworkWorker;
import common.Request;
import common.model.movie.Movie;


public class AuthController {
    private GuiApp guiApp;
    private Stage stage;
    private TextField loginField;
    private PasswordField passField;
    private AuthManager authManager;

    public AuthController(GuiApp guiApp, Stage stage, AuthManager authManager, TextField loginField, PasswordField passField) {
        this.authManager = authManager;
        this.guiApp = guiApp;
        this.stage = stage;
        this.loginField = loginField;
        this.passField = passField;
    }

    public void handleAuth(String event) {
        String login = loginField.getText();
        String pass = passField.getText();

        if (guiApp.getWorker() == null) {
            guiApp.startNetworkWorker();
        }

        NetworkWorker worker  = guiApp.getWorker();
     
        if (login.isEmpty() || pass.isEmpty()) {
            showError("Введите логин и пароль!");
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
                    Platform.runLater(() -> {
                    System.out.println("Открываю главное окно..."); 
                    guiApp.showMainWindow(stage, movies);
                });
                });
            } else {
                showError("Ошибка: " + resp.getMessage());
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

