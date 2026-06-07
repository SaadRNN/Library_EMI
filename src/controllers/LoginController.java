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

    private final LibraryRepository repository = new LibraryRepository();
    private final UserManager userManager = new UserManager(repository);

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showErrorAlert("Champs requis", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            User authenticatedUser = userManager.validateUserCredentials(username, password);

            if (authenticatedUser != null) {

                String fxml = authenticatedUser.getRole() == User.Role.ADMIN
                        ? "/presentation/dashboard_admin.fxml"
                        : "/presentation/dashboard_user.fxml";

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                Parent root = loader.load();

                if (authenticatedUser.getRole() == User.Role.ADMIN) {
                    DashboardAdminController controller = loader.getController();
                    controller.initData(authenticatedUser);
                } else {
                    DashboardUserController controller = loader.getController();
                    controller.initData(authenticatedUser);
                }

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("EMIBook - Dashboard");
                stage.centerOnScreen();
                stage.show();

            } else {
                showErrorAlert("Échec de connexion", "Identifiants incorrects !");
            }

        } catch (SQLException e) {
            showErrorAlert("Erreur Base de données", "Impossible de contacter le serveur MySQL.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            showErrorAlert("Erreur Pilote", "Pilote JDBC introuvable.");
            e.printStackTrace();
        } catch (IOException e) {
            showErrorAlert("Erreur Navigation", "Impossible de charger le dashboard.");
            e.printStackTrace();
        }
    }
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/presentation/forgot_password.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EMIBook - Mot de passe oublié");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void switchToSignUp(ActionEvent event) {
        try {
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("/presentation/signup.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(signUpRoot));
            stage.setTitle("EMIBook - Inscription");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur de navigation", "Impossible d'ouvrir la page d'inscription.");
            e.printStackTrace();
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}