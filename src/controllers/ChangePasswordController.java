package controllers;

import domainModel.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import persistance.LibraryRepository;

import java.io.IOException;

public class ChangePasswordController {

    @FXML private Label labelUsername;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private final LibraryRepository repository = new LibraryRepository();
    private User targetUser;
    private User adminUser;

    public void initData(User admin, User target) {
        this.adminUser = admin;
        this.targetUser = target;
        labelUsername.setText("Utilisateur : " + target.getUsername());
    }

    @FXML
    private void handleConfirmer(ActionEvent event) {
        String newPassword = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirm.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Champs vides", "Veuillez remplir tous les champs.");
            return;
        }

        if (!newPassword.equals(confirm)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Les mots de passe ne sont pas identiques.");
            return;
        }

        if (newPassword.length() < 8) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le mot de passe doit contenir au moins 8 caractères.");
            return;
        }

        try {
            repository.updateUserPassword(targetUser.getId(), newPassword);
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Mot de passe de \"" + targetUser.getUsername() + "\" modifié avec succès !");
            handleAnnuler(event);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/presentation/dashboard_admin.fxml"));
            Parent root = loader.load();
            DashboardAdminController controller = loader.getController();
            controller.initData(adminUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EMIBook - Dashboard Admin");
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