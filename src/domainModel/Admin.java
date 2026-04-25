package domainModel;

public class Admin extends User{
    public Admin(int id, String username, String email, String password , String role) {
        super(id, username, email, password, role);
        this.setRole(User.Role.ADMIN);
    }

}

