package service;
import domainModel.User;
public class UserService {
    public void changerUserRole(User targetUser, User CurrentUser){
        if (CurrentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Seul l'admine peut modifier");
        }
        targetUser.setRole(User.Role.ADMIN);
        }
    }

