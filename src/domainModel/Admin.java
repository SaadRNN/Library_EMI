package domainModel;

public class Admin extends User{
    public Admin(int id, String username, String email, String password) {
        super(id, username, email, password);
        this.setRole(User.Role.ADMIN);
    }

}

