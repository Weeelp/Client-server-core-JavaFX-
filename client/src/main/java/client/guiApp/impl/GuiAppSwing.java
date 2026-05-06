package client.guiApp.impl;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import java.awt.event.*;
import java.awt.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatClientProperties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import client.controller.AuthController;
import client.controller.MovieDataController;
import client.guiApp.GuiApp;
import client.manager.AuthManager;
import client.network.*;
import client.swing.components.actionBtn.ActionCell;
import common.Request;
import common.model.movie.*;
import common.model.person.*;
import common.utils.validator.MovieValidator;

public class GuiAppSwing extends JFrame implements GuiApp {
    private static final Logger log = LogManager.getLogger(GuiAppSwing.class.getName());

    private NetworkService networkService;
    private NetworkWorker worker;
    private AuthManager authManager = new AuthManager(null, null);
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private LinkedList<Movie> safeList = new LinkedList<>();

    private ResourceBundle bundle;
    private Locale currentLocale = new Locale("en");

    private JTable mainTable;
    private DefaultTableModel tableModel;
    
    private JLabel statusLabel = new JLabel();
    private JLabel ownerLabel = new JLabel();
    private JButton addBtn = new JButton();
    private JButton addIfMaxBtn = new JButton();
    private JButton logoutBtn = new JButton();
    private JComboBox<String> sortOptions, filterColumn, langChooser;

    private JTextField filterInput;

    public GuiAppSwing () {
        this.bundle = ResourceBundle.getBundle("lang.gui", currentLocale);
        this.networkService = new NetworkService();
        networkService.tryConnect();
        startNetworkWorker();

        setSize(400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        showAuthWindow();

        if (networkService.tryConnect() && networkService.getSocket() != null) {
            log.info("Соединение установлено. Порт: " + networkService.getSocket().getLocalPort());
        } else {
            log.warn("Не удалось подключиться к серверу. Работа в оффлайн режиме.");
        }

        setVisible(true); 
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
    public void updateStatus(boolean isOnline) {
        if (isOnline) {
            statusLabel.setText(bundle.getString("status_online"));
            statusLabel.setForeground(Color.GREEN);
        } else {
            statusLabel.setText(bundle.getString("status_offline"));
            statusLabel.setForeground(Color.RED);
        }
    }

    private void updateTexts() {
        ownerLabel.setText(bundle.getString("user_label") + ": " + (authManager.getLogin() != null ? authManager.getLogin() : "Unknown"));
        addBtn.setText(bundle.getString("btn_add"));
        addIfMaxBtn.setText(bundle.getString("btn_add_if_max"));
        logoutBtn.setText(bundle.getString("btn_logout"));

        filterInput.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, bundle.getString("search_prompt"));

        if (mainTable != null) {
            String[] columnKeys = {
                "col_id", "col_name", "col_date", "col_oscars", 
                "col_total_box", "col_usa_box", "col_genre", "col_owner", "col_actions"
            };
            for (int i = 0; i < columnKeys.length; i++) {
                mainTable.getColumnModel().getColumn(i).setHeaderValue(bundle.getString(columnKeys[i]));
            }
            mainTable.getTableHeader().repaint();
            updateTableData(safeList);
        }

        sortOptions.setModel(new DefaultComboBoxModel<>(new String[]{
            bundle.getString("sort_id"), bundle.getString("sort_name"), bundle.getString("sort_x"),
            bundle.getString("sort_y"), bundle.getString("sort_date"), bundle.getString("sort_oscars"),
            bundle.getString("sort_total_box"), bundle.getString("sort_usa_box"),
            bundle.getString("sort_genre"), bundle.getString("sort_owner")
        }));

        filterColumn.setModel(new DefaultComboBoxModel<>(new String[]{
            bundle.getString("filter_name"), bundle.getString("filter_id"),
            bundle.getString("filter_oscars"), bundle.getString("filter_owner"),
            bundle.getString("filter_genre"), bundle.getString("filter_total_box")
        }));
        filterColumn.setSelectedItem(bundle.getString("filter_name"));

        langChooser.setModel(new DefaultComboBoxModel<>(new String[]{"Русский", "English", "Srpski", "Ελληνικά", "Español (DO)"}));

        String language = currentLocale.getLanguage();
        String country = currentLocale.getCountry();

        if (language.equals("ru")) {
            langChooser.setSelectedItem("Русский");
        } else if (language.equals("sr")) {
            langChooser.setSelectedItem("Srpski");
        } else if (language.equals("el")) {
            langChooser.setSelectedItem("Ελληνικά");
        } else if (language.equals("es") && country.equals("DO")) {
            langChooser.setSelectedItem("Español (DO)");
        } else {
            langChooser.setSelectedItem("English");
        }
    }

    private void showAuthWindow() {
        JPanel layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        layout.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        JLabel label = new JLabel(bundle.getString("auth_title"));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField loginField = new JTextField();
        loginField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, bundle.getString("auth_login_prompt"));
        JPasswordField passField = new JPasswordField();
        passField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, bundle.getString("auth_pass_prompt"));

        JButton loginBtn = new JButton(bundle.getString("btn_login"));
        JButton toRegBtn = new JButton(bundle.getString("btn_to_reg"));

        JPanel buttonBox = new JPanel();
        buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.Y_AXIS));
        buttonBox.setOpaque(false);
        
        loginBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        toRegBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        buttonBox.add(loginBtn);
        buttonBox.add(Box.createVerticalStrut(10));
        buttonBox.add(toRegBtn);

        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passField.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        layout.add(statusLabel);
        layout.add(Box.createVerticalStrut(15));
        layout.add(label);
        layout.add(Box.createVerticalStrut(15));
        layout.add(loginField);
        layout.add(Box.createVerticalStrut(15));
        layout.add(passField);
        layout.add(Box.createVerticalStrut(15));
        layout.add(buttonBox);

        AuthController controller = new AuthController(
            this,
            authManager, 
            () -> loginField.getText(), 
            () -> new String(passField.getPassword())
        );
        toRegBtn.addActionListener(e -> showRegistrationWindow());
        loginBtn.addActionListener(event -> controller.handleAuth("auth"));

        this.setContentPane(layout);
        this.setSize(400, 350);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void showRegistrationWindow() {
        JPanel layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        layout.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        JLabel label = new JLabel(bundle.getString("reg_title"));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField loginField = new JTextField();
        loginField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, bundle.getString("auth_login_prompt"));
        JPasswordField passField = new JPasswordField();
        passField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, bundle.getString("auth_pass_prompt"));

        JButton loginBtn = new JButton(bundle.getString("btn_login"));
        JButton toRegBtn = new JButton(bundle.getString("btn_to_reg"));

        JPanel buttonBox = new JPanel();
        buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.Y_AXIS));
        buttonBox.setOpaque(false);
        
        loginBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        toRegBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        buttonBox.add(loginBtn);
        buttonBox.add(Box.createVerticalStrut(10));
        buttonBox.add(toRegBtn);

        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passField.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        layout.add(statusLabel);
        layout.add(Box.createVerticalStrut(15));
        layout.add(label);
        layout.add(Box.createVerticalStrut(15));
        layout.add(loginField);
        layout.add(Box.createVerticalStrut(15));
        layout.add(passField);
        layout.add(Box.createVerticalStrut(15));
        layout.add(buttonBox);

        AuthController controller = new AuthController(
            this,
            authManager, 
            () -> loginField.getText(), 
            () -> new String(passField.getPassword())
        );
        toRegBtn.addActionListener(e -> showAuthWindow());
        loginBtn.addActionListener(event -> controller.handleAuth("register"));

        this.setContentPane(layout);
        this.setSize(400, 350);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    @Override
    public void updateTableData(LinkedList<Movie> newList) {
        runOnUIThread(() -> {
            this.safeList = newList;
            tableModel.setRowCount(0);

            NumberFormat nf = NumberFormat.getInstance(currentLocale);
            DateTimeFormatter df = DateTimeFormatter
            .ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)
            .withLocale(currentLocale);

            for (Movie m : newList) {
                if (m == null) continue;
                tableModel.addRow(new Object[]{
                    m.getId(),
                    m.getName(),
                    m.getCreationDate() != null ? m.getCreationDate().format(df) : "",
                    m.getOscarsCount(),
                    m.getTotalBoxOffice() != null ? nf.format(m.getTotalBoxOffice()) : "",
                    m.getUsaBoxOffice() != null ? nf.format(m.getUsaBoxOffice()) : "",
                    m.getGenre(),
                    m.getOwner_login()
                });
            }
        });
    }

    public JTable getMainTable() {
        return this.mainTable;
    }

    @Override
    public void showMainWindow(LinkedList<Movie> movies) {
        getContentPane().removeAll();

        JPanel layout = new JPanel(new BorderLayout(0, 15));
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        layout.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sortOptions = new JComboBox<>();
        filterColumn = new JComboBox<>();
        langChooser = new JComboBox<>();
        filterInput = new JTextField();
        ownerLabel = new JLabel(authManager.getLogin());

        this.safeList = (movies == null) ? new LinkedList<>() : movies;

        String[] columnNames = { "ID", "Name", "Date", "Oscars","Total box", "USA box", "Genre", "Owner", "Action" };
        tableModel = new DefaultTableModel(columnNames, 0);

        mainTable = new JTable(tableModel){
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == 8) {
                    try {
                        Movie movie = safeList.get(mainTable.convertRowIndexToModel(row));
                        String currentUser = authManager.getLogin();

                        return currentUser != null && currentUser.equals(movie.getOwner_login());
                    } catch (Exception e) {
                        return false;
                    }
                }
                return false;
            }

            
        };

        mainTable.getColumnModel().getColumn(8).setCellRenderer(new ActionCell(this, authManager, bundle, mapper));
        mainTable.getColumnModel().getColumn(8).setCellEditor(new ActionCell(this, authManager, bundle, mapper));
        mainTable.setCellSelectionEnabled(true);
        mainTable.setRowHeight(35); 
        
        JPanel topBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topBox.add(filterInput);
        topBox.add(filterColumn);
        topBox.add(sortOptions);
        topBox.add(langChooser);
        topBox.add(ownerLabel);

        JPanel bottomBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        bottomBox.add(logoutBtn);
        bottomBox.add(addBtn);
        bottomBox.add(addIfMaxBtn);
       
        updateTexts();
        updateStatus(networkService.tryConnect());
        updateTableData(safeList);

        layout.add(statusLabel);
        layout.add(topBox, BorderLayout.NORTH); 
        layout.add(Box.createVerticalStrut(15));
        layout.add(new JScrollPane(mainTable), BorderLayout.CENTER);
        layout.add(Box.createVerticalStrut(15));
        layout.add(bottomBox);

        this.setContentPane(layout);
        this.setSize(1400, 800);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);


        mainTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int viewRow = mainTable.rowAtPoint(e.getPoint());
                if (viewRow == -1) return;
                int modelRow = mainTable.convertRowIndexToModel(viewRow);

                if (modelRow >= 0 && modelRow < safeList.size()) {
                    Movie movie = safeList.get(modelRow);
                    int col = mainTable.columnAtPoint(e.getPoint());
                    if (col != 8) {
                        openMovieCard(movie);
                    }
                }
            }
        });

        sortOptions.addActionListener(e -> {
            String selected = (String) sortOptions.getSelectedItem();
            if (selected == null) return;

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
            
            updateTableData(new LinkedList<>(sorted));
        });

        filterInput.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void insertUpdate(DocumentEvent e) { filter(); }

            private void filter() {
                String newVal = filterInput.getText();
                String col = (String) filterColumn.getSelectedItem();
                if (col == null) return;

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
                updateTableData(new LinkedList<>(filtered));
            }
        });


        langChooser.addActionListener(e -> {
            String selected = (String) langChooser.getSelectedItem();
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

        addBtn.addActionListener(e -> showMovieForm(null, 1));
        addIfMaxBtn.addActionListener(e -> showMovieForm(null, 2));
        logoutBtn.addActionListener(e -> {
            authManager.setLogin(null);
            showAuthWindow();
        });

    }

    private void openMovieCard(Movie movie) {
        JDialog dialog = new JDialog(this, bundle.getString("movie_card_title"), true);
        dialog.setUndecorated(true); 

        dialog.setOpacity(0.0f); 

        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(java.awt.Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());

                long id = movie.getId();
                int r = (int) (id * 31) % 256;
                int gp = (int) (id * 17) % 256;
                int b = (int) (id * 53) % 256;
                g2.setColor(new java.awt.Color(r, gp, b));

                int rectWidth = 80 + (int) (Math.abs(movie.getCoordinates().getX()) % 120);
                int rectHeight = 80 + (int) (Math.abs(movie.getCoordinates().getY()) % 120);
                g2.fillRect(50, 150, rectWidth, rectHeight);

                g2.setColor(java.awt.Color.BLACK);
                g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 30));
                g2.drawString(movie.getName(), 50, 50);

                g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 18));
                g2.drawString(bundle.getString("genre_label") + ": " + movie.getGenre(), 50, 90);
                g2.drawString(bundle.getString("oscars_label") + ": " + movie.getOscarsCount(), 50, 120);

                drawManualButtons(g2, getWidth(), movie.getOwner_login());
            }
        };

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                int w = canvas.getWidth();

                if (x >= w - 60 && x <= w - 20 && y >= 20 && y <= 60) {
                    dialog.dispose();
                }
                String currentUser = authManager.getLogin();
                if (currentUser != null && currentUser.equals(movie.getOwner_login())) {
                    if (x >= w - 120 && x <= w - 70 && y >= 20 && y <= 60) {
                        dialog.dispose();
                        showMovieForm(movie, 0);
                    }
                }
            }
        });

        canvas.setPreferredSize(new Dimension(600, 400));
        dialog.add(canvas);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        Timer timer = new Timer(10, null);
        timer.addActionListener(e -> {
            float nextOpacity = dialog.getOpacity() + 0.05f;
            if (nextOpacity >= 1.0f) {
                dialog.setOpacity(1.0f);
                timer.stop();
            } else {
                dialog.setOpacity(nextOpacity);
            }
        });

        SwingUtilities.invokeLater(() -> {
            timer.start();
        });

        dialog.setVisible(true);
    }

    private void drawManualButtons(Graphics2D g2, int w, String owner) {
        g2.setColor(java.awt.Color.RED);
        g2.fillRoundRect(w - 60, 20, 40, 40, 10, 10);
        g2.setColor(java.awt.Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawLine(w - 50, 30, w - 30, 50);
        g2.drawLine(w - 30, 30, w - 50, 50);

        String currentUser = authManager.getLogin();
        boolean isOwner = currentUser != null && currentUser.equals(owner);
        g2.setColor(isOwner ? new java.awt.Color(0, 0, 150) : java.awt.Color.LIGHT_GRAY);
        g2.fillRoundRect(w - 120, 20, 50, 40, 10, 10);
        g2.setColor(java.awt.Color.WHITE);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        g2.drawString(bundle.getString("btn_edit"), w - 115, 45);
    }

    public void showMovieForm(Movie movieToEdit, int level) {
        JDialog dialog = new JDialog(this, 
            movieToEdit == null ? bundle.getString("form_add_title") : bundle.getString("form_edit_title"), 
            true);
        
        JPanel grid = new JPanel(new GridLayout(0, 2, 10, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField();
        JTextField xField = new JTextField();
        JTextField yField = new JTextField();
        JTextField oscarsField = new JTextField();
        JTextField totalBoxOfficeField = new JTextField();
        JTextField usaBoxOfficeField = new JTextField();
        JComboBox<Genre> genreCombo = new JComboBox<>(Genre.values());

        JTextField personNameField = new JTextField();
        JTextField personHeightField = new JTextField();
        JComboBox<EyeColor> eyeColorCombo = new JComboBox<>(EyeColor.values());
        JComboBox<HairColor> hairColorCombo = new JComboBox<>(HairColor.values());
        JComboBox<Country> countryCombo = new JComboBox<>(Country.values());

        if (movieToEdit != null) {
            nameField.setText(movieToEdit.getName());
            xField.setText(String.valueOf(movieToEdit.getCoordinates().getX()));
            yField.setText(String.valueOf(movieToEdit.getCoordinates().getY()));
            oscarsField.setText(String.valueOf(movieToEdit.getOscarsCount()));
            totalBoxOfficeField.setText(String.valueOf(movieToEdit.getTotalBoxOffice()));
            usaBoxOfficeField.setText(String.valueOf(movieToEdit.getUsaBoxOffice()));
            genreCombo.setSelectedItem(movieToEdit.getGenre());

            Person p = movieToEdit.getScreenWriter();
            personNameField.setText(p.getName());
            personHeightField.setText(String.valueOf(p.getHeight()));
            eyeColorCombo.setSelectedItem(p.getEyeColor());
            hairColorCombo.setSelectedItem(p.getHairColor());
            countryCombo.setSelectedItem(p.getNationality());
        }

        grid.add(new JLabel(bundle.getString("field_name"))); grid.add(nameField);
        grid.add(new JLabel(bundle.getString("field_coord_x"))); grid.add(xField);
        grid.add(new JLabel(bundle.getString("field_coord_y"))); grid.add(yField);
        grid.add(new JLabel(bundle.getString("field_oscars"))); grid.add(oscarsField);
        grid.add(new JLabel(bundle.getString("field_total_box"))); grid.add(totalBoxOfficeField);
        grid.add(new JLabel(bundle.getString("field_usa_box"))); grid.add(usaBoxOfficeField);
        grid.add(new JLabel(bundle.getString("field_genre"))); grid.add(genreCombo);
        grid.add(new JSeparator()); grid.add(new JSeparator());
        grid.add(new JLabel(bundle.getString("field_person_name"))); grid.add(personNameField);
        grid.add(new JLabel(bundle.getString("field_person_height"))); grid.add(personHeightField);
        grid.add(new JLabel(bundle.getString("field_eye_color"))); grid.add(eyeColorCombo);
        grid.add(new JLabel(bundle.getString("field_hair_color"))); grid.add(hairColorCombo);
        grid.add(new JLabel(bundle.getString("field_country"))); grid.add(countryCombo);

        JButton saveBtn = new JButton(bundle.getString("btn_save"));
        saveBtn.addActionListener(e -> {
            try {
                MovieData data = new MovieData(
                    MovieValidator.validateName(nameField.getText()),
                    new Coordinates(MovieValidator.validateX(xField.getText()), MovieValidator.validateY(yField.getText())),
                    MovieValidator.validateOscarsCount(oscarsField.getText()),
                    MovieValidator.validateTotalBoxOffice(totalBoxOfficeField.getText()),
                    MovieValidator.validateUsaBoxOffice(usaBoxOfficeField.getText()),
                    (Genre) genreCombo.getSelectedItem(),
                    new Person(
                        MovieValidator.validatePersonName(personNameField.getText()),
                        MovieValidator.validatePersonHeight(personHeightField.getText()),
                        (EyeColor) eyeColorCombo.getSelectedItem(),
                        (HairColor) hairColorCombo.getSelectedItem(),
                        (Country) countryCombo.getSelectedItem()
                    )
                );

                String cmd = (movieToEdit == null) ? (level == 1 ? "add" : "add_if_max") : "update_by_id";
                String[] args = (movieToEdit == null) ? new String[0] : new String[]{String.valueOf(movieToEdit.getId())};

                Request req = new Request(cmd, args, data, new String[]{authManager.getLogin(), authManager.getPassword()});

                worker.addTask(req, resp -> {
                    if (resp.getStatus().equals("200") || resp.getStatus().equals("201")) {
                        dialog.dispose();
                        worker.addTask(new Request("show", null, null, new String[]{authManager.getLogin(), authManager.getPassword()}), showResp -> {
                            LinkedList<Movie> newList = mapper.convertValue(showResp.getData(), new com.fasterxml.jackson.core.type.TypeReference<LinkedList<Movie>>() {});
                            updateTableData(newList);
                        });
                    } else {
                        showError(resp.getMessage());
                    }
                });
            } catch (Exception ex) {
                showError(bundle.getString("error_validation") + ": " + ex.getMessage());
            }
        });

        JPanel mainLayout = new JPanel(new BorderLayout(0, 10));
        mainLayout.add(new JScrollPane(grid), BorderLayout.CENTER);
        mainLayout.add(saveBtn, BorderLayout.SOUTH);

        dialog.setContentPane(mainLayout);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    @Override
    public void runOnUIThread(Runnable action) {
        SwingUtilities.invokeLater(action);
    }

    public LinkedList<Movie> getSafeList () {
        return safeList;
    }

    @Override
    public void showError(String message) {
        runOnUIThread(() -> {
            JOptionPane.showMessageDialog(
                this,                            
                message,
                bundle.getString("error_title"),
                JOptionPane.ERROR_MESSAGE
            );
        });
    }

    public static void main(String[] args) { 
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> { new GuiAppSwing(); });
     }
}