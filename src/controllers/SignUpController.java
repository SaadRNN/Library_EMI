package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import persistance.LibraryRepository;

import java.io.IOException;
import java.sql.SQLException;

public class SignUpController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    private final LibraryRepository repository = new LibraryRepository();

    @FXML
    private void handleSignUp(ActionEvent event) {
        String nom = lastNameField.getText();
        String prenom = firstNameField.getText();
        String email = emailField.getText();
        String mdp = passwordField.getText();
        String confirmation = confirmPasswordField.getText();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || mdp.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Champs vides", "Veuillez remplir tous les champs.");
            return;
        }

        if (!mdp.equals(confirmation)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Les mots de passe ne sont pas identiques.");
            return;
        }

        try {
            repository.addUser(prenom + " " + nom, email, mdp);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Inscription réussie pour " + prenom + " !");
            switchToLogin(event);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Base de données", "Impossible de créer le compte.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Pilote", "Pilote JDBC introuvable.");
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToLogin(ActionEvent event) {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/presentation/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement du login.fxml : " + e.getMessage());
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