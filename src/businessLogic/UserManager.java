package businessLogic;
import persistance.LibraryRepository;
import domainModel.*;

import java.sql.SQLException;
import java.util.List;

public class UserManager {
    private LibraryRepository repository;
    private static final int MAX_LOANS=3;

    public LibraryRepository getRepository() {return repository;}
    public void setRepository(LibraryRepository repository) {this.repository = repository;}

    public UserManager(LibraryRepository repository) {
        this.repository = repository;
    }

    public User validateUserCredentials(String Username, String Password) throws SQLException, ClassNotFoundException {
        if (Username ==null || Username.isBlank()){
            throw new IllegalArgumentException("Username is either null or blank");
        }
        if (Password ==null || Password.isBlank()){
            throw new IllegalArgumentException("Password is either null or blank");
        }
       return repository.findUser(Username, Password);
        }


    public boolean canBorrow(User user) throws SQLException, ClassNotFoundException {
        List<Loan> activeLoans = repository.getActiveLoansByUser(user.getId());
        if (activeLoans.size() >= MAX_LOANS) {
            System.out.println("Alert : Too many active loans");
            return false;
        }
        for (Loan loan : activeLoans) {
            if (loan.isOverdue()) {
                System.out.println("Alert : A book is overdue");
                return false;
            }
        }
        return true;
    }
    public List<User> getAllUsers()
            throws SQLException, ClassNotFoundException {

        return repository.getAllUsers();
    }
    public void deleteUser(User currentUser, int targetUserId) throws SQLException, ClassNotFoundException {
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("You are not allowed to delete users !");
        }
        repository.deleteUser(targetUserId);
    }

    public void changerUserRole(User currentUser, int targetUserId, User.Role newRole) throws SQLException, ClassNotFoundException {
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Seul un admin peut changer les rôles !");
        }
        repository.updateUserRole(targetUserId, newRole);
    }
}
