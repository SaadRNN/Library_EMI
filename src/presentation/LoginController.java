package presentation;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import domainModel.User;
import persistance.LibraryRepository;
import java.sql.SQLException;
public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    private LibraryRepository repository = new LibraryRepository();
    @FXML
    private void handleLogin(){
        String username = usernameField.getText();
        String password = passwordField.getText();
        try{
            // On demande au "cerveau" (Repository) de chercher l'utilisateur
            User authenticatedUser = repository.findUser(username,password);
            if(authenticatedUser!=null) {
                System.out.println("Bienvenue" + authenticatedUser.getUsername());
                // Ici, on chargera la fenêtre suivante (Admin ou User) selon le rôle
                showSuccessAlert("Connexion réussie" , "Bienveue" + authenticatedUser.getRole());
            }
            else {
                showErrorAlert("Erreur","Identifiant incorrects !");
            }

        }
        catch(SQLException e){
            showErrorAlert("Erreur Base de donnée","Impossible de contacter la base de donnée");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void showErrorAlert(String title,String message){
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
}
    private void showSuccessAlert(String title,String message){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
