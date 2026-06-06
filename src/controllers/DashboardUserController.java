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

public class DashboardUserController {

    // ===== SIDEBAR =====
    @FXML private Label sidebarUsername;
    @FXML private Label avatarLabel;
    @FXML private Label pageTitle;

    // ===== STATS =====
    @FXML private Label statTotalLivres;
    @FXML private Label statDisponibles;
    @FXML private Label statMesEmprunts;
    @FXML private Label statAmendes;

    // ===== LISTES DASHBOARD =====
    @FXML private ListView<String> listEmprunts;
    @FXML private ListView<String> listDisponibles;

    // ===== PAGES =====
    @FXML private ScrollPane paneDashboard;
    @FXML private ScrollPane paneCatalogue;
    @FXML private ScrollPane paneEmprunts;

    // ===== TABLE CATALOGUE =====
    @FXML private TableView<Book> tableCatalogue;
    @FXML private TableColumn<Book, String> colId;
    @FXML private TableColumn<Book, String> colTitre;
    @FXML private TableColumn<Book, String> colAuteur;
    @FXML private TableColumn<Book, String> colDispo;
    @FXML private TableColumn<Book, String> colAction;

    // ===== TABLE EMPRUNTS =====
    @FXML private TableView<Loan> tableEmprunts;
    @FXML private TableColumn<Loan, String> coleLivre;
    @FXML private TableColumn<Loan, String> coleDateEmprunt;
    @FXML private TableColumn<Loan, String> coleEcheance;
    @FXML private TableColumn<Loan, String> coleStatut;
    @FXML private TableColumn<Loan, String> coleAmende;
    @FXML private TableColumn<Loan, String> coleAction;

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

        // Afficher le nom dans la sidebar
        sidebarUsername.setText(user.getUsername());
        String initiales = user.getUsername().length() >= 2
                ? user.getUsername().substring(0, 2).toUpperCase()
                : user.getUsername().toUpperCase();
        avatarLabel.setText(initiales);

        // Charger les données
        loadStats();
        loadDashboardLists();
        setupCatalogueTable();
        setupEmpruntsTable();
    }

    // ===== STATS =====
    private void loadStats() {
        try {
            List<Book> allBooks = bookManager.getAllBooks();
            long disponibles = allBooks.stream().filter(Book::isAvailable).count();
            List<Loan> emprunts = loanManager.getActiveLoans(currentUser);
            float totalAmende = (float) emprunts.stream().mapToDouble(Loan::calculateFine).sum();

            statTotalLivres.setText(String.valueOf(allBooks.size()));
            statDisponibles.setText(String.valueOf(disponibles));
            statMesEmprunts.setText(emprunts.size() + " / 3");
            statAmendes.setText(String.valueOf((int) totalAmende));

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // ===== LISTES DASHBOARD =====
    private void loadDashboardLists() {
        try {
            // Emprunts actifs
            List<Loan> emprunts = loanManager.getActiveLoans(currentUser);
            List<String> empruntsStr = emprunts.stream()
                    .map(l -> (l.isOverdue() ? "⚠ " : "✓ ") + l.getBook().getTitle()
                            + " · Échéance : " + l.getDueDate())
                    .collect(Collectors.toList());
            listEmprunts.setItems(FXCollections.observableArrayList(empruntsStr));

            // Livres disponibles
            List<Book> dispo = bookManager.getAllBooks().stream()
                    .filter(Book::isAvailable)
                    .collect(Collectors.toList());
            List<String> dispoStr = dispo.stream()
                    .map(b -> "✓ " + b.getTitle() + " — " + b.getAuthor())
                    .collect(Collectors.toList());
            listDisponibles.setItems(FXCollections.observableArrayList(dispoStr));

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

        // Colonne bouton Emprunter
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Emprunter");
            {
                btn.setStyle("-fx-background-color: #007AFF; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 11; -fx-cursor: hand;");
                btn.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    handleEmprunter(book);
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Book book = getTableView().getItems().get(getIndex());
                btn.setDisable(!book.isAvailable());
                btn.setOpacity(book.isAvailable() ? 1.0 : 0.4);
                setGraphic(btn);
            }
        });

        loadCatalogueData(null);
    }

    private void loadCatalogueData(String filtre) {
        try {
            List<Book> books = bookManager.getAllBooks();
            if (filtre != null && !filtre.isEmpty()) {
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

    // ===== TABLE EMPRUNTS =====
    private void setupEmpruntsTable() {
        coleLivre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBook().getTitle()));
        coleDateEmprunt.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLoanDate().toString()));
        coleEcheance.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDueDate().toString()));
        coleStatut.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isOverdue() ? "⚠ En retard" : "✓ En cours"));
        coleAmende.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().calculateFine() > 0
                        ? (int) c.getValue().calculateFine() + " DH"
                        : "0 DH"));

        // Colonne bouton Rendre
        coleAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Rendre");
            {
                btn.setStyle("-fx-background-color: #EAF3DE; -fx-text-fill: #3B6D11; " +
                        "-fx-background-radius: 6; -fx-font-size: 11; -fx-cursor: hand;");
                btn.setOnAction(e -> {
                    Loan loan = getTableView().getItems().get(getIndex());
                    handleRendre(loan);
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        loadEmpruntsData();
    }

    private void loadEmpruntsData() {
        try {
            List<Loan> emprunts = loanManager.getActiveLoans(currentUser);
            tableEmprunts.setItems(FXCollections.observableArrayList(emprunts));
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // ===== ACTIONS =====
    private void handleEmprunter(Book book) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer l'emprunt");
        confirm.setHeaderText(null);
        confirm.setContentText("Emprunter \"" + book.getTitle() + "\" pour 15 jours ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    loanManager.borrowBook(currentUser, book.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Livre emprunté avec succès !");
                    // Rafraîchir tout
                    loadStats();
                    loadDashboardLists();
                    loadCatalogueData(null);
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    private void handleRendre(Loan loan) {
        float amende = loan.calculateFine();
        String msg = "Rendre \"" + loan.getBook().getTitle() + "\" ?"
                + (amende > 0 ? "\n⚠ Amende : " + (int) amende + " DH" : "");
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer le retour");
        confirm.setHeaderText(null);
        confirm.setContentText(msg);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    loanManager.returnBook(loan.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Livre rendu avec succès !");
                    // Rafraîchir tout
                    loadStats();
                    loadDashboardLists();
                    loadEmpruntsData();
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
        paneEmprunts.setVisible(false);
        loadDashboardLists();
    }

    @FXML private void showCatalogue() {
        pageTitle.setText("Catalogue des livres");
        paneDashboard.setVisible(false);
        paneCatalogue.setVisible(true);
        paneEmprunts.setVisible(false);
        loadCatalogueData(null);
    }

    @FXML private void showEmprunts() {
        pageTitle.setText("Mes emprunts");
        paneDashboard.setVisible(false);
        paneCatalogue.setVisible(false);
        paneEmprunts.setVisible(true);
        loadEmpruntsData();
    }

    @FXML private void handleSearch() {
        String q = searchField.getText().trim();
        showCatalogue();
        loadCatalogueData(q);
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