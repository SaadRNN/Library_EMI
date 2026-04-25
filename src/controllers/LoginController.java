package controllers;

import businessLogic.UserManager;
import domainModel.User;
import persistance.LibraryRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    // Initialisation de la logique métier
    private final LibraryRepository repository = new LibraryRepository();
    private final UserManager userManager = new UserManager(repository);

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validation des champs vides avant l'appel SQL
        if (username.isEmpty() || password.isEmpty()) {
            showErrorAlert("Champs requis", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            User authenticatedUser = userManager.validateUserCredentials(username, password);

            if (authenticatedUser != null) {
                System.out.println("Bienvenue " + authenticatedUser.getUsername());
                showSuccessAlert("Connexion réussie", "Bienvenue, rôle : " + authenticatedUser.getRole());

                // Ici vous pourrez appeler une méthode pour charger le dashboard
                // loadDashboard(authenticatedUser);
            } else {
                showErrorAlert("Échec de connexion", "Identifiants incorrects !");
            }

        } catch (SQLException e) {
            showErrorAlert("Erreur Base de données", "Impossible de contacter le serveur MySQL.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            showErrorAlert("Erreur Pilote", "Pilote JDBC introuvable.");
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToSignUp(ActionEvent event) {
        try {
            // Chargement de la vue Inscription
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("/presentation/signup.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(signUpRoot));
            stage.setTitle("EMIBook - Inscription");
            stage.centerOnScreen(); // Optionnel : recentrer la fenêtre
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement de signup.fxml : " + e.getMessage());
            showErrorAlert("Erreur de navigation", "Impossible d'ouvrir la page d'inscription.");
        }
    }

    // --- Méthodes utilitaires pour les alertes ---

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}