package controllers; // Nouveau package

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
import java.io.IOException;

public class SignUpController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML
    private void handleSignUp(ActionEvent event) {
        // Extraction des données
        String nom = lastNameField.getText();
        String prenom = firstNameField.getText();
        String email = emailField.getText();
        String mdp = passwordField.getText();
        String confirmation = confirmPasswordField.getText();

        // Validation rapide
        if (email.isEmpty() || mdp.isEmpty() || nom.isEmpty()) {
            showAlert("Champs vides", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        if (!mdp.equals(confirmation)) {
            showAlert("Erreur Mot de Passe", "Les mots de passe ne sont pas identiques.");
            return;
        }

        // Ici, vous ferez appel à votre package 'persistance' plus tard
        System.out.println("Utilisateur prêt à être enregistré : " + prenom + " " + nom);

        showAlert("Succès", "Inscription réussie pour " + prenom);
        switchToLogin(event);
    }

    @FXML
    private void switchToLogin(ActionEvent event) {
        try {
            // Attention au chemin : si le FXML est dans 'presentation', on remonte d'un cran
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/presentation/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement du login.fxml : " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}