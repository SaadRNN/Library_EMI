package service;

import domainModel.User;

public class AuthService {

    public boolean login(User user, String email, String password){
        return user != null &&
                user.getEmail().equals(email) &&
                user.getPassword().equals(password);
    }
}