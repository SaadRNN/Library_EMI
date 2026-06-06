package controllers;

import businessLogic.BookManager;
import businessLogic.LoanManager;
import businessLogic.UserManager;
import domainModel.Book;
import domainModel.Loan;
import domainModel.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import persistance.LibraryRepository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardAdminController {

    // ===== SIDEBAR =====
    @FXML private Label sidebarUsername;
    @FXML private Label avatarLabel;
    @FXML private Label pageTitle;

    // ===== STATS =====
    @FXML private Label statTotalLivres;
    @FXML private Label statDisponibles;
    @FXML private Label statEmpruntsActifs;
    @FXML private Label statUsers;

    // ===== LISTE DASHBOARD =====
    @FXML private ListView<String> listLivres;

    // ===== PAGES =====
    @FXML private ScrollPane paneDashboard;
    @FXML private ScrollPane paneCatalogue;
    @FXML private ScrollPane paneAjouterLivre;
    @FXML private ScrollPane paneGestionUsers;

    // ===== TABLE CATALOGUE =====
    @FXML private TableView<Book> tableCatalogue;
    @FXML private TableColumn<Book, String> colId;
    @FXML private TableColumn<Book, String> colTitre;
    @FXML private TableColumn<Book, String> colAuteur;
    @FXML private TableColumn<Book, String> colDispo;

    // ===== FORMULAIRE AJOUT LIVRE =====
    @FXML private TextField fieldTitre;
    @FXML private TextField fieldAuteur;
    @FXML private TableView<Book> tableAdmin;
    @FXML private TableColumn<Book, String> aColId;
    @FXML private TableColumn<Book, String> aColTitre;
    @FXML private TableColumn<Book, String> aColAuteur;
    @FXML private TableColumn<Book, String> aColDispo;

    // ===== TABLE USERS =====
    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, String> uColId;
    @FXML private TableColumn<User, String> uColNom;
    @FXML private TableColumn<User, String> uColEmail;
    @FXML private TableColumn<User, String> uColRole;
    @FXML private TableColumn<User, String> uColAction;

    // ===== SEARCH =====
    @FXML private TextField searchField;

    // ===== LOGIQUE METIER =====
    private final LibraryRepository repository = new LibraryRepository();
    private final BookManager bookManager = new BookManager(repository);
    private final UserManager userManager = new UserManager(repository);
    private final LoanManager loanManager = new LoanManager(repository, userManager);

    private User currentUser;

    // ===== INITIALISATION =====
    public void initData(User user) {
        this.currentUser = user;

        sidebarUsername.setText(user.getUsername());
        String initiales = user.getUsername().length() >= 2
                ? user.getUsername().substring(0, 2).toUpperCase()
                : user.getUsername().toUpperCase();
        avatarLabel.setText(initiales);

        loadStats();
        loadDashboardList();
        setupCatalogueTable();
        setupAdminTable();
        setupUsersTable();
    }

    // ===== STATS =====
    private void loadStats() {
        try {
            List<Book> allBooks = bookManager.getAllBooks();
            long disponibles = allBooks.stream().filter(Book::isAvailable).count();
            List<Loan> allLoans = loanManager.getAllLoans(currentUser);
            long empruntsActifs = allLoans.stream()
                    .filter(l -> l.getReturnDate() == null).count();
            List<User> allUsers = userManager.getAllUsers();

            statTotalLivres.setText(String.valueOf(allBooks.size()));
            statDisponibles.setText(String.valueOf(disponibles));
            statEmpruntsActifs.setText(String.valueOf(empruntsActifs));
            statUsers.setText(String.valueOf(allUsers.size()));

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // ===== LISTE DASHBOARD =====
    private void loadDashboardList() {
        try {
            List<Book> books = bookManager.getAllBooks();
            List<String> booksStr = books.stream()
                    .map(b -> (b.isAvailable() ? "✓ " : "✗ ")
                            + b.getTitle() + " — " + b.getAuthor())
                    .collect(Collectors.toList());
            listLivres.setItems(FXCollections.observableArrayList(booksStr));
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // ===== TABLE CATALOGUE =====
    private void setupCatalogueTable() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colTitre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colAuteur.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAuthor()));
        colDispo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isAvailable() ? "✓ Disponible" : "✗ Indisponible"));
        loadCatalogueData();
    }

    private void loadCatalogueData() {
        try {
            String filtre = searchField != null ? searchField.getText().trim() : "";
            List<Book> books = bookManager.getAllBooks();
            if (!filtre.isEmpty()) {
                books = books.stream()
                        .filter(b -> b.getTitle().toLowerCase().contains(filtre.toLowerCase())
                                || b.getAuthor().toLowerCase().contains(filtre.toLowerCase()))
                        .collect(Collectors.toList());
            }
            tableCatalogue.setItems(FXCollections.observableArrayList(books));
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // ===== TABLE ADMIN (AJOUTER LIVRE) =====
    private void setupAdminTable() {
        aColId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        aColTitre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        aColAuteur.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAuthor()));
        aColDispo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isAvailable() ? "✓ Disponible" : "✗ Indisponible"));
        loadAdminTableData();
    }

    private void loadAdminTableData() {
        try {
            List<Book> books = bookManager.getAllBooks();
            tableAdmin.setItems(FXCollections.observableArrayList(books));
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // ===== TABLE USERS =====
    private void setupUsersTable() {
        uColId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        uColNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        uColEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        uColRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole().name()));

        // Colonne actions : Promouvoir ADMIN + Supprimer
        uColAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnRole = new Button("→ ADMIN");
            private final Button btnSupp = new Button("Supprimer");
            private final javafx.scene.layout.HBox box =
                    new javafx.scene.layout.HBox(6, btnRole, btnSupp);
            {
                btnRole.setStyle("-fx-background-color: #243447; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 10; -fx-cursor: hand;");
                btnSupp.setStyle("-fx-background-color: #FCEBEB; -fx-text-fill: #A32D2D; " +
                        "-fx-background-radius: 6; -fx-font-size: 10; -fx-cursor: hand;");

                btnRole.setOnAction(e -> {
                    User target = getTableView().getItems().get(getIndex());
                    handleChangerRole(target);
                });
                btnSupp.setOnAction(e -> {
                    User target = getTableView().getItems().get(getIndex());
                    handleSupprimerUser(target);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                User target = getTableView().getItems().get(getIndex());
                // Cacher le bouton ADMIN si l'utilisateur est déjà ADMIN
                btnRole.setVisible(target.getRole() != User.Role.ADMIN);
                // Empêcher de se supprimer soi-même
                btnSupp.setDisable(target.getId() == currentUser.getId());
                setGraphic(box);
            }
        });

        loadUsersData();
    }

    private void loadUsersData() {
        try {
            List<User> users = userManager.getAllUsers();
            tableUsers.setItems(FXCollections.observableArrayList(users));
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // ===== ACTIONS =====
    @FXML
    private void handleAjouterLivre() {
        String titre = fieldTitre.getText().trim();
        String auteur = fieldAuteur.getText().trim();

        if (titre.isEmpty() || auteur.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Champs vides", "Veuillez remplir le titre et l'auteur.");
            return;
        }

        try {
            bookManager.addBook(currentUser, titre, auteur);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "\"" + titre + "\" ajouté au catalogue !");
            fieldTitre.clear();
            fieldAuteur.clear();
            loadAdminTableData();
            loadDashboardList();
            loadStats();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void handleChangerRole(User target) {
        if (target.getRole() == User.Role.ADMIN) {
            showAlert(Alert.AlertType.INFORMATION, "Info",
                    target.getUsername() + " est déjà administrateur.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Changer le rôle");
        confirm.setHeaderText(null);
        confirm.setContentText("Promouvoir \"" + target.getUsername() + "\" en ADMIN ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userManager.changerUserRole(currentUser, target.getId(), User.Role.ADMIN);
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            target.getUsername() + " est maintenant ADMIN.");
                    loadUsersData();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    private void handleSupprimerUser(User target) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer l'utilisateur");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer \"" + target.getUsername() + "\" définitivement ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userManager.deleteUser(currentUser, target.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            "Utilisateur supprimé.");
                    loadUsersData();
                    loadStats();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    // ===== NAVIGATION =====
    @FXML private void showDashboard() {
        pageTitle.setText("Tableau de bord");
        paneDashboard.setVisible(true);
        paneCatalogue.setVisible(false);
        paneAjouterLivre.setVisible(false);
        paneGestionUsers.setVisible(false);
        loadDashboardList();
        loadStats();
    }

    @FXML private void showCatalogue() {
        pageTitle.setText("Catalogue des livres");
        paneDashboard.setVisible(false);
        paneCatalogue.setVisible(true);
        paneAjouterLivre.setVisible(false);
        paneGestionUsers.setVisible(false);
        loadCatalogueData();
    }

    @FXML private void showAjouterLivre() {
        pageTitle.setText("Ajouter un livre");
        paneDashboard.setVisible(false);
        paneCatalogue.setVisible(false);
        paneAjouterLivre.setVisible(true);
        paneGestionUsers.setVisible(false);
        loadAdminTableData();
    }

    @FXML private void showGestionUsers() {
        pageTitle.setText("Gestion des utilisateurs");
        paneDashboard.setVisible(false);
        paneCatalogue.setVisible(false);
        paneAjouterLivre.setVisible(false);
        paneGestionUsers.setVisible(true);
        loadUsersData();
    }

    @FXML private void handleSearch() {
        showCatalogue();
        loadCatalogueData();
    }

    @FXML private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/presentation/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EMIBook - Connexion");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}