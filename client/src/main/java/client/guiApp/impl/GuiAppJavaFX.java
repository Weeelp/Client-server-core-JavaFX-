package client.guiApp.impl;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import client.controller.AuthController;
import client.controller.MovieDataController;
import client.guiApp.GuiApp;
import client.manager.*;
import client.network.*;
import common.Request;
import common.model.movie.*;
import common.model.person.*;
import common.utils.validator.MovieValidator;

public class GuiAppJavaFX extends Application implements GuiApp{
    private static final Logger log = LogManager.getLogger(GuiApp.class.getName());

    private NetworkService networkService;
    private NetworkWorker worker;
    private AuthManager authManager = new AuthManager(null, null);
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private ResourceBundle bundle;
    private Locale currentLocale = new Locale("ru");

    private Stage primaryStage;
    private VBox layout;
    private TableView<Movie> mainTable;

    private Label statusLabel = new Label();
    private Label ownerLabel = new Label();
    private Button addBtn = new Button();
    private Button addIfMaxBtn = new Button();
    private Button logoutBtn = new Button();
    private TableColumn<Movie, Long> idCol;
    private TableColumn<Movie, String> nameCol, genreCol, ownerCol;
    private TableColumn<Movie, String> dateCol;
    private TableColumn<Movie, Integer> oscarsCol;
    private TableColumn<Movie, Double> totalBoxOfficeCol;
    private TableColumn<Movie, Long> usaBoxOfficeCol;
    private TableColumn<Movie, Void> actionCol;
    private ComboBox<String> sortOptions, filterColumn, langChooser;
    private TextField filterInput;

    private LinkedList<Movie> safeList = new LinkedList<>();

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        this.bundle = ResourceBundle.getBundle("lang.gui", currentLocale);
        this.networkService = new NetworkService();
        networkService.tryConnect();
        startNetworkWorker();

        if (networkService.tryConnect() && networkService.getSocket() != null) {
            log.info("Соединение установлено. Порт: " + networkService.getSocket().getLocalPort());
        } else {
            log.warn("Не удалось подключиться к серверу. Работа в оффлайн режиме.");
        }

        showAuthWindow(primaryStage);
    }

    @Override
    public void startNetworkWorker() {
        if (this.worker == null) {
            this.worker = new NetworkWorker(this.networkService, this, mapper);
            this.worker.start();
        }
    }

    @Override
    public NetworkWorker getWorker() { return worker; }

    @Override
    public void runOnUIThread(Runnable action) {
        Platform.runLater(action);
    }

    private void updateTexts() {
        ownerLabel.setText(bundle.getString("user_label") + ": " + (authManager.getLogin() != null ? authManager.getLogin() : "Unknown"));
        addBtn.setText(bundle.getString("btn_add"));
        addIfMaxBtn.setText(bundle.getString("btn_add_if_max"));
        logoutBtn.setText(bundle.getString("btn_logout"));
        filterInput.setPromptText(bundle.getString("search_prompt"));
        sortOptions.setPromptText(bundle.getString("sort_prompt"));
        filterColumn.setPromptText(bundle.getString("filter_prompt"));

        idCol.setText(bundle.getString("col_id"));
        nameCol.setText(bundle.getString("col_name"));
        dateCol.setText(bundle.getString("col_date"));
        oscarsCol.setText(bundle.getString("col_oscars"));
        totalBoxOfficeCol.setText(bundle.getString("col_total_box"));
        usaBoxOfficeCol.setText(bundle.getString("col_usa_box"));
        genreCol.setText(bundle.getString("col_genre"));
        ownerCol.setText(bundle.getString("col_owner"));
        actionCol.setText(bundle.getString("col_actions"));

        sortOptions.setItems(FXCollections.observableArrayList(
            bundle.getString("sort_id"), bundle.getString("sort_name"), bundle.getString("sort_x"),
            bundle.getString("sort_y"), bundle.getString("sort_date"), bundle.getString("sort_oscars"),
            bundle.getString("sort_total_box"), bundle.getString("sort_usa_box"),
            bundle.getString("sort_genre"), bundle.getString("sort_owner")
        ));

        filterColumn.setItems(FXCollections.observableArrayList(
            bundle.getString("filter_name"),
            bundle.getString("filter_id"),
            bundle.getString("filter_oscars"),
            bundle.getString("filter_owner"),
            bundle.getString("filter_genre"),
            bundle.getString("filter_total_box")
        ));
        filterColumn.setValue(bundle.getString("filter_name"));

        langChooser.setItems(FXCollections.observableArrayList("Русский", "English", "Srpski", "Ελληνικά", "Español (DO)"));
        String language = currentLocale.getLanguage();
        String country = currentLocale.getCountry();
        if (language.equals("ru")) {
            langChooser.setValue("Русский");
        } else if (language.equals("sr")) {
            langChooser.setValue("Srpski");
        } else if (language.equals("el")) {
            langChooser.setValue("Ελληνικά");
        } else if (language.equals("es") && country.equals("DO")) {
            langChooser.setValue("Español (DO)");
        } else {
            langChooser.setValue("English");
        }
    }
    
    @Override
    public void updateTableData(LinkedList<Movie> newList) {
        if (newList == null) return;
        this.safeList = newList;
        if (mainTable != null) {
            Platform.runLater(() -> {
                mainTable.setItems(FXCollections.observableArrayList(safeList));
                mainTable.refresh();
            });
        }
    }

    @Override
    public void updateStatus(boolean isOnline) {
        if (isOnline) {
            statusLabel.setText(bundle.getString("status_online"));
            statusLabel.setStyle("-fx-text-fill: green;");
        } else {
            statusLabel.setText(bundle.getString("status_offline"));
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    public void showMainWindow(LinkedList<Movie> movies){
        showMainWindow(primaryStage, movies);
    }

    public void showMainWindow(Stage stage, LinkedList<Movie> movies) {
        this.safeList = (movies == null) ? new LinkedList<>() : movies;
        this.mainTable = new TableView<>(FXCollections.observableArrayList(safeList));

        idCol = new TableColumn<>();
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        nameCol = new TableColumn<>();
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        dateCol = new TableColumn<>();
        dateCol.setCellValueFactory(cell -> javafx.beans.binding.Bindings.createObjectBinding(() ->
                cell.getValue().getCreationDate().toString()));

        oscarsCol = new TableColumn<>();
        oscarsCol.setCellValueFactory(new PropertyValueFactory<>("oscarsCount"));

        totalBoxOfficeCol = new TableColumn<>();
        totalBoxOfficeCol.setCellValueFactory(new PropertyValueFactory<>("totalBoxOffice"));

        usaBoxOfficeCol = new TableColumn<>();
        usaBoxOfficeCol.setCellValueFactory(new PropertyValueFactory<>("usaBoxOffice"));

        genreCol = new TableColumn<>();
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));

        ownerCol = new TableColumn<>();
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("owner_login"));

        actionCol = new TableColumn<>();
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();
            private final HBox container = new HBox(5, editBtn, deleteBtn);
            {
                editBtn.setOnAction(event -> showMovieForm(getTableView().getItems().get(getIndex()), stage, 0));
                deleteBtn.setOnAction(event -> {
                    Movie m = getTableView().getItems().get(getIndex());
                    Request req = new Request("remove_by_id", new String[]{String.valueOf(m.getId())}, null, new String[]{authManager.getLogin(), authManager.getPassword()});
                    worker.addTask(req, resp -> {});
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Movie m = getTableView().getItems().get(getIndex());
                    editBtn.setText(bundle.getString("btn_edit"));
                    deleteBtn.setText(bundle.getString("btn_del"));
                    container.setDisable(!m.getOwner_login().equals(authManager.getLogin()));
                    setGraphic(container);
                }
            }
        });

        mainTable.getColumns().addAll(idCol, nameCol, dateCol, oscarsCol, totalBoxOfficeCol, usaBoxOfficeCol, genreCol, ownerCol, actionCol);

        mainTable.setRowFactory(tv -> {
            TableRow<Movie> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    Node target = (Node) event.getTarget();
                    if (!(target instanceof Button || target.getParent() instanceof Button)) {
                        openMovieCard(row.getItem());
                    }
                }
            });
            return row;
        });

        filterInput = new TextField();
        filterColumn = new ComboBox<>();
        sortOptions = new ComboBox<>();
        langChooser = new ComboBox<>();

        sortOptions.setOnAction(e -> {
            String selected = sortOptions.getValue();
            String key = "id";
            if (selected.equals(bundle.getString("sort_name"))) key = "name";
            else if (selected.equals(bundle.getString("sort_x"))) key = "x";
            else if (selected.equals(bundle.getString("sort_y"))) key = "y";
            else if (selected.equals(bundle.getString("sort_date"))) key = "date";
            else if (selected.equals(bundle.getString("sort_oscars"))) key = "oscars";
            else if (selected.equals(bundle.getString("sort_total_box"))) key = "totalBoxOffice";
            else if (selected.equals(bundle.getString("sort_usa_box"))) key = "usaBoxOffice";
            else if (selected.equals(bundle.getString("sort_genre"))) key = "genre";
            else if (selected.equals(bundle.getString("sort_owner"))) key = "owner";
            List<Movie> sorted = MovieDataController.sort(safeList, key);
            mainTable.setItems(FXCollections.observableArrayList(sorted));
        });

        filterInput.textProperty().addListener((obs, oldVal, newVal) -> {
            String col = filterColumn.getValue();
            String field = "name";
            if (col.equals(bundle.getString("filter_id"))) {
                field = "id";
            } else if (col.equals(bundle.getString("filter_oscars"))) {
                field = "oscars";
            } else if (col.equals(bundle.getString("filter_owner"))) {
                field = "owner";
            } else if (col.equals(bundle.getString("filter_genre"))) {
                field = "genre";
            } else if (col.equals(bundle.getString("filter_total_box"))) {
                field = "totalBoxOffice";
            }
            List<Movie> filtered = MovieDataController.filter(safeList, field, newVal);
            mainTable.setItems(FXCollections.observableArrayList(filtered));
        });

        langChooser.setOnAction(e -> {
            String selected = langChooser.getValue();
            if (selected == null) return;
            switch (selected) {
                case "Русский":
                    currentLocale = new Locale("ru");
                    break;
                case "English":
                    currentLocale = new Locale("en");
                    break;
                case "Srpski":
                    currentLocale = new Locale("sr");
                    break;
                case "Ελληνικά":
                    currentLocale = new Locale("el");
                    break;
                case "Español (DO)":
                    currentLocale = new Locale("es", "DO");
                    break;
                default:
                    currentLocale = new Locale("en");
                    break;
            }
            bundle = ResourceBundle.getBundle("lang.gui", currentLocale);
            updateTexts();
            updateStatus(networkService.tryConnect());
        });

        addBtn.setOnAction(e -> showMovieForm(null, stage, 1));
        addIfMaxBtn.setOnAction(e -> showMovieForm(null, stage, 2));
        logoutBtn.setOnAction(e -> {
            authManager.setLogin(null);
            showAuthWindow(stage);
        });

        HBox controls = new HBox(10, filterInput, filterColumn, sortOptions, langChooser, ownerLabel);
        HBox bottom = new HBox(15, logoutBtn, addBtn, addIfMaxBtn);
        this.layout = new VBox(15, statusLabel, controls, mainTable, bottom);
        this.layout.setStyle("-fx-padding: 20;");

        updateTexts();
        updateStatus(networkService.tryConnect());

        stage.setScene(new Scene(layout, 1200, 700));
        stage.show();
    }

    public void showAuthWindow(Stage stage) {
        Label label = new Label(bundle.getString("auth_title"));
        TextField loginField = new TextField();
        loginField.setPromptText(bundle.getString("auth_login_prompt"));
        PasswordField passField = new PasswordField();
        passField.setPromptText(bundle.getString("auth_pass_prompt"));
        Button loginBtn = new Button(bundle.getString("btn_login"));
        Button toRegBtn = new Button(bundle.getString("btn_to_reg"));
        VBox buttonBox = new VBox(10, loginBtn, toRegBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        AuthController controller = new AuthController(
            this,
            authManager, 
            () -> loginField.getText(), 
            () -> passField.getText()
        );

        toRegBtn.setOnAction(e -> showRegistrationWindow(stage));
        loginBtn.setOnAction(event -> controller.handleAuth("auth"));

        VBox layout = new VBox(15, statusLabel, label, loginField, passField, buttonBox);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 40;");
        stage.setScene(new Scene(layout, 400, 350));
        stage.show();
    }

    public void showRegistrationWindow(Stage stage) {
        Label label = new Label(bundle.getString("reg_title"));
        TextField loginField = new TextField();
        loginField.setPromptText(bundle.getString("reg_login_prompt"));
        PasswordField passField = new PasswordField();
        passField.setPromptText(bundle.getString("reg_pass_prompt"));
        Button regBtn = new Button(bundle.getString("btn_register"));
        Button toAuthBtn = new Button(bundle.getString("btn_to_auth"));
        VBox buttonBox = new VBox(10, regBtn, toAuthBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        AuthController controller = new AuthController(
            this,
            authManager, 
            () -> loginField.getText(), 
            () -> passField.getText()
        );
        toAuthBtn.setOnAction(e -> showAuthWindow(stage));
        regBtn.setOnAction(event -> controller.handleAuth("register"));

        VBox layout = new VBox(15, statusLabel, label, loginField, passField, buttonBox);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 40;");
        stage.setScene(new Scene(layout, 400, 350));
        stage.show();
    }

    private void openMovieCard(Movie movie) {
        if (movie == null) return;

        Stage cardStage = new Stage();
        cardStage.initModality(Modality.WINDOW_MODAL);
        cardStage.initOwner(mainTable.getScene().getWindow());
        cardStage.setTitle(bundle.getString("movie_card_title"));

        String currentUser = authManager.getLogin();
        boolean isOwner = currentUser != null && currentUser.equals(movie.getOwner_login());

        final double canvasWidth = 600;
        final double canvasHeight = 400;

        Canvas cardCanvas = new Canvas(canvasWidth, canvasHeight);
        GraphicsContext gc = cardCanvas.getGraphicsContext2D();

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        long id = movie.getId();
        int red   = (int) (id * 31) % 256;
        int green = (int) (id * 17) % 256;
        int blue  = (int) (id * 53) % 256;
        long coordX = movie.getCoordinates().getX();
        float coordY = movie.getCoordinates().getY();
        int rectWidth = 80 + (int) (Math.abs(coordX) % 120);
        int rectHeight = 80 + (int) (Math.abs(coordY) % 120);
        gc.setFill(Color.rgb(red, green, blue));
        gc.fillRect(50, 150, rectWidth, rectHeight);

        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 30));
        gc.fillText(movie.getName(), 50, 50);

        gc.setFont(new Font("Arial", 18));
        gc.fillText(bundle.getString("genre_label") + ": " + movie.getGenre(), 50, 90);
        gc.fillText(bundle.getString("oscars_label") + ": " + movie.getOscarsCount(), 50, 120);

        drawCloseButton(gc, canvasWidth);
        drawEditButton(gc, canvasWidth, isOwner ? Color.DARKBLUE : Color.LIGHTGRAY, bundle.getString("btn_edit"));

        cardCanvas.setOnMouseClicked(event -> {
            double x = event.getX();
            double y = event.getY();
            if (x >= canvasWidth - 60 && x <= canvasWidth - 20 && y >= 20 && y <= 60) {
                cardStage.close();
            }
            else if (isOwner && x >= canvasWidth - 120 && x <= canvasWidth - 70 && y >= 20 && y <= 60) {
                cardStage.close();
                showMovieForm(movie, primaryStage, 0);
            }
        });

        StackPane root = new StackPane(cardCanvas);
        root.setStyle("-fx-background-color: white;");
        Scene scene = new Scene(root, canvasWidth, canvasHeight);
        cardStage.setScene(scene);
        cardStage.show();
    }

    private void drawCloseButton(GraphicsContext gc, double canvasWidth) {
        double btnX = canvasWidth - 60;
        double btnY = 20;
        gc.setFill(Color.RED);
        gc.fillRoundRect(btnX, btnY, 40, 40, 10, 10);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeLine(btnX + 10, btnY + 10, btnX + 30, btnY + 30);
        gc.strokeLine(btnX + 30, btnY + 10, btnX + 10, btnY + 30);
    }

    private void drawEditButton(GraphicsContext gc, double canvasWidth, Color buttonColor, String text) {
        double btnX = canvasWidth - 120;
        double btnY = 20;
        gc.setFill(buttonColor);
        gc.fillRoundRect(btnX, btnY, 50, 40, 10, 10);
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 14));
        gc.fillText(text, btnX + 8, btnY + 25);
    }

    public void showMovieForm(Movie movieToEdit, Stage stage, int level) {
        Stage formStage = new Stage();
        formStage.initModality(Modality.APPLICATION_MODAL);
        formStage.initOwner(stage);
        formStage.setTitle(movieToEdit == null ? bundle.getString("form_add_title") : bundle.getString("form_edit_title"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextField xField = new TextField();
        TextField yField = new TextField();
        TextField oscarsField = new TextField();
        TextField totalBoxOfficeField = new TextField();
        TextField usaBoxOfficeField = new TextField();
        ComboBox<Genre> genreCombo = new ComboBox<>(FXCollections.observableArrayList(Genre.values()));

        TextField personNameField = new TextField();
        TextField personHeightField = new TextField();
        ComboBox<EyeColor> eyeColorCombo = new ComboBox<>(FXCollections.observableArrayList(EyeColor.values()));
        ComboBox<HairColor> hairColorCombo = new ComboBox<>(FXCollections.observableArrayList(HairColor.values()));
        ComboBox<Country> countryCombo = new ComboBox<>(FXCollections.observableArrayList(Country.values()));

        if (movieToEdit != null) {
            nameField.setText(movieToEdit.getName());
            xField.setText(String.valueOf(movieToEdit.getCoordinates().getX()));
            yField.setText(String.valueOf(movieToEdit.getCoordinates().getY()));
            oscarsField.setText(String.valueOf(movieToEdit.getOscarsCount()));
            totalBoxOfficeField.setText(String.valueOf(movieToEdit.getTotalBoxOffice()));
            usaBoxOfficeField.setText(String.valueOf(movieToEdit.getUsaBoxOffice()));
            genreCombo.setValue(movieToEdit.getGenre());

            Person p = movieToEdit.getScreenWriter();
            personNameField.setText(p.getName());
            personHeightField.setText(String.valueOf(p.getHeight()));
            eyeColorCombo.setValue(p.getEyeColor());
            hairColorCombo.setValue(p.getHairColor());
            countryCombo.setValue(p.getNationality());
        }

        grid.add(new Label(bundle.getString("field_name")), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label(bundle.getString("field_coord_x")), 0, 1); grid.add(xField, 1, 1);
        grid.add(new Label(bundle.getString("field_coord_y")), 0, 2); grid.add(yField, 1, 2);
        grid.add(new Label(bundle.getString("field_oscars")), 0, 3); grid.add(oscarsField, 1, 3);
        grid.add(new Label(bundle.getString("field_total_box")), 0, 4); grid.add(totalBoxOfficeField, 1, 4);
        grid.add(new Label(bundle.getString("field_usa_box")), 0, 5); grid.add(usaBoxOfficeField, 1, 5);
        grid.add(new Label(bundle.getString("field_genre")), 0, 6); grid.add(genreCombo, 1, 6);

        grid.add(new Separator(), 0, 7, 2, 1);
        grid.add(new Label(bundle.getString("field_person_name")), 0, 8); grid.add(personNameField, 1, 8);
        grid.add(new Label(bundle.getString("field_person_height")), 0, 9); grid.add(personHeightField, 1, 9);
        grid.add(new Label(bundle.getString("field_eye_color")), 0, 10); grid.add(eyeColorCombo, 1, 10);
        grid.add(new Label(bundle.getString("field_hair_color")), 0, 11); grid.add(hairColorCombo, 1, 11);
        grid.add(new Label(bundle.getString("field_country")), 0, 12); grid.add(countryCombo, 1, 12);

        Button saveBtn = new Button(bundle.getString("btn_save"));
        saveBtn.setOnAction(e -> {
            try {
                MovieData data = new MovieData(
                    MovieValidator.validateName(nameField.getText()),
                    new Coordinates(MovieValidator.validateX(xField.getText()), MovieValidator.validateY(yField.getText())),
                    MovieValidator.validateOscarsCount(oscarsField.getText()),
                    MovieValidator.validateTotalBoxOffice(totalBoxOfficeField.getText()),
                    MovieValidator.validateUsaBoxOffice(usaBoxOfficeField.getText()),
                    genreCombo.getValue(),
                    new Person(
                        MovieValidator.validatePersonName(personNameField.getText()),
                        MovieValidator.validatePersonHeight(personHeightField.getText()),
                        eyeColorCombo.getValue(),
                        hairColorCombo.getValue(),
                        countryCombo.getValue()
                    )
                );

                String cmd = (movieToEdit == null) ? ((level == 1) ? "add" : "add_if_max") : "update_by_id";

                String[] args = (movieToEdit == null) ? new String[0] : new String[]{String.valueOf(movieToEdit.getId())};

                Request req = new Request(cmd, args, data, new String[]{authManager.getLogin(), authManager.getPassword()});

                worker.addTask(req, resp -> {
                    if (resp.getStatus().equals("200") || resp.getStatus().equals("201")) {
                        formStage.close();
                        worker.addTask(new Request("show", null, null, new String[]{authManager.getLogin(), authManager.getPassword()}), showResp -> {
                            LinkedList<Movie> newList = mapper.convertValue(showResp.getData(), new TypeReference<LinkedList<Movie>>() {});
                            showMainWindow(stage, newList);
                        });
                    } else {
                        showError(resp.getMessage());
                    }
                });

            } catch (Exception ex) {
                showError(bundle.getString("error_validation") + ": " + ex.getMessage());
            }
        });

        VBox layout = new VBox(10, grid, saveBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10));

        formStage.setScene(new Scene(layout));
        formStage.show();
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("error_title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}